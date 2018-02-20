package org.millenaire.village;

import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.millenaire.*;
import org.millenaire.MillCulture.VillageType;
import org.millenaire.blocks.BlockMillPath;
import org.millenaire.blocks.MillBlocks;
import org.millenaire.blocks.StoredPosition;
import org.millenaire.building.*;
import org.millenaire.entities.EntityMillVillager;
import org.millenaire.pathing.MillPathNavigate;
import org.millenaire.util.ResourceLocationUtil;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Village {
    private UUID uuid;
    private BlockPos mainBlock;
    public VillageGeography geography;
    private VillageType type;
    private MillCulture culture;
    private World world;
    private BuildingLocation[] buildings;
    private String villageName;

    /**
     * Constructor used by {@link Village#createVillage(BlockPos, World, VillageType, MillCulture, String)}
     * @param b The position of the village stone
     * @param worldIn The world the village is in
     * @param typeIn The {@link VillageType type} of village this is.
     * @param cultureIn The {@link MillCulture culture of the village}
     * @param villageName The name of the village, as displayed to the user.
     */
    private Village(BlockPos b, World worldIn, VillageType typeIn, MillCulture cultureIn, String villageName) {
        this.setPos(b);
        this.uuid = UUID.randomUUID();
        this.world = worldIn;
        this.type = typeIn;
        this.culture = cultureIn;
        this.geography = new VillageGeography();
        this.villageName = villageName;
        BuildingLocation loc = new BuildingLocation(1, 1, 1, mainBlock, EnumFacing.NORTH);

        ArrayList<BuildingLocation> bl = new ArrayList<>();
        bl.add(loc);

        System.out.println("Constructing map of area...");
        this.geography.update(world, bl, null, mainBlock, world.getHeight(b).getY());
    }

    /**
     * @deprecated FOR USE BY VILLAGE TRACKER ONLY
     */
    @Deprecated()
    public Village() {

    }

    /**
     * Creates a village. Note that it will not be spawned until {@link Village#setupVillage()} is called.
     * @param VSPos The position of the {@link org.millenaire.entities.TileEntityVillageStone Village Stone}
     * @param world The world this village is in.
     * @param typeIn The {@link VillageType type} of village this is (or will be)
     * @param cultureIn The {@link MillCulture culture} of the village.
     * @param villageName The name of the village, as shown to the player.
     * @return The instantiated Village object.
     */
    public static Village createVillage(BlockPos VSPos, World world, VillageType typeIn, MillCulture cultureIn, String villageName) {
        return new Village(VSPos, world, typeIn, cultureIn, villageName);
    }

    /**
     * Get the {@link VillageType} of this village.
     * @return The type
     */
    public VillageType getType() {
        return type;
    }

    /**
     * Sets the {@link VillageType} of this village. Should in theory only be used when loading a village from a save.
     * Will log if the type is overwritten.
     * @param t The type to set.
     */
    public void setType(VillageType t) {
        if (type != null) {
            System.out.println("Warning: Type being overwritten for village at " + getPos());
        }
        this.type = t;
    }

    /**
     * Get the unique identifier of this village.
     * @return The {@link UUID} of this vilage.
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Sets the unique identifier of this village. Should in theory only be used when loading a village from a save.
     * @param u The UUID to set.
     */
    public void setUUID(UUID u) {
        if (uuid != null) {
            System.out.println("Warning: UUID Being overwritten for village at " + getPos());
        }
        uuid = u;
    }

    /**
     * Gets the position of the {@link org.millenaire.entities.TileEntityVillageStone village stone} for this village.
     * @return The {@link BlockPos position} of the stone.
     */
    public BlockPos getPos() {
        return mainBlock;
    }

    /**
     * @deprecated FOR USE BY VILLAGE TRACKER ONLY
     */
    @Deprecated
    public void setPos(BlockPos pos) {
        mainBlock = pos;
    }

    /**
     * Generates starting buildings and paths. Runs on a separate thread to the server thread.
     */
    public void setupVillage() {
        new Thread(() -> {
            try {
                EntityMillVillager v = new EntityMillVillager(world, 100100, culture, this);
                v.setPosition(mainBlock.getX(), mainBlock.getY(), mainBlock.getZ());
                v.setTypeAndGender(MillCulture.normanCulture.getVillagerTypeByID("normanKnight"), 1);
                MillPathNavigate mpn = new MillPathNavigate(v, world);

                world.spawnEntityInWorld(v);

                for (BuildingRecord proj : type.startingBuildings) {
                    Building p = PlanIO.loadSchematic(PlanIO.getBuildingTag(ResourceLocationUtil.getRL(proj.ID).getResourcePath(), culture, true), culture, proj.lvl);

                    System.out.println("Finding a place to put a " + proj.ID);

                    BuildingLocation loc = p.findBuildingLocation(geography, mpn, mainBlock, 64, new Random(), p.buildingOrientation);
                    geography.registerBuilding(p, loc);

                    if (loc == null) {
                        throw new Exception("Failed to find a suitable location for the " + proj.ID + "!");
                    }

                    Millenaire.proxy.scheduleTask(() -> {
                        System.out.println("Generating " + proj.ID + " at " + loc.position);
                        PlanIO.flattenTerrainForBuilding(p, loc, geography);
                        PlanIO.placeBuilding(p, loc, world);

                        if (BuildingTypes.getTypeFromProject(proj).isTownHall) {
                            p.isCenter = true;

                            MainLoop:
                            for (int x = 0; x < p.width; x++) {
                                for (int y = 0; y < p.height; y++) {
                                    for (int z = 0; z < p.length; z++) {
                                        BlockPos pos = new BlockPos(x + loc.position.getX(), y + loc.position.getY() + p.depth, z + loc.position.getZ());
                                        if (world.getBlockState(pos).getBlock() == MillBlocks.storedPosition) {
                                            if (world.getBlockState(pos).getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.PATHPOS) {
                                                pos = world.getHeight(pos).add(0, 1, 0);
                                                v.setPosition(pos.getX(), pos.getY(), pos.getZ());
                                                System.out.println("Moved villager to path start for town hall at " + pos);
                                                v.onGround = true; //Dodgy hack?
                                                v.useNewPathingAtThisPoint = true;
                                                break MainLoop;
                                            }
                                        }
                                    }
                                }
                            }
                            p.rebuildPath = false;
                        } else
                            p.rebuildPath = true;

                        mpn.invalidateConnections();
                    });
                }

                Millenaire.proxy.scheduleTask(() -> {
                    System.out.println("Updating Geography");
                    geography.update(world, new ArrayList<>(geography.buildingLocations.keySet()), null, mainBlock, world.getHeight(mainBlock).getY());
                });

                v.useNewPathingAtThisPoint = true;
                Millenaire.proxy.scheduleTask(() -> genPaths(new MillPathNavigate(v, world)));
                VillageTracker.get(world).registerVillage(getUUID(), this);

                if (MillConfig.villageAnnouncement) {
                    for (int i = 0; i < world.playerEntities.size(); i++)
                        world.playerEntities.get(i).addChatMessage(new ChatComponentText(culture + " village " + villageName + " discovered at " + mainBlock.getX() + ", " + mainBlock.getY() + ", " + mainBlock.getZ()));
                }

                System.out.println(culture + " village " + villageName + " created at " + mainBlock.getX() + ", " + mainBlock.getY() + ", " + mainBlock.getZ());

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Village failed to create. Not registering!");
                VillageTracker.get(world).unregisterVillagePos(mainBlock);
            }
        }).start();
    }

    /**
     * Builds all the paths in the village at generation time.
     * @param pathing The {@link MillPathNavigate} to use to generate paths.
     */
    private void genPaths(MillPathNavigate pathing) {
        pathing.villager.useNewPathingAtThisPoint = true;

        pathing.setSearchRange(400);

        BuildingLoop:
        for (BuildingLocation loc : geography.buildingLocations.keySet()) {
            try {
                Building plan = geography.buildingLocations.get(loc);

                if (!plan.rebuildPath) continue;

                System.out.println("Pathfinding to " + plan.nativeName);

                //System.out.println("Looking for path marker...");

                for (int x = 0; x < plan.width; x++) {
                    for (int y = 0; y < plan.height; y++) {
                        for (int z = 0; z < plan.length; z++) {
                            BlockPos p = new BlockPos(x + loc.position.getX(), y + loc.position.getY() + plan.depth, z + loc.position.getZ());

                            if (world.getBlockState(p).getBlock() == MillBlocks.storedPosition) {
                                if (world.getBlockState(p).getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.PATHPOS) {
                                    //System.out.println("Found a path marker at " + p);
                                    //This is a path
                                    PathEntity path = pathing.getPathToPos(p);

                                    while (!path.isFinished()) {
                                        PathPoint point = path.getPathPointFromIndex(path.getCurrentPathIndex());

                                        BlockPos pathPos = new BlockPos(point.xCoord, point.yCoord, point.zCoord).down();

                                        world.setBlockState(pathPos, MillBlocks.blockMillPath.getStateFromMeta(BlockMillPath.EnumType.DIRT.getMetadata()));

                                        path.incrementPathIndex();
                                    }
                                    System.out.println("Built a path: " + path);
                                    continue BuildingLoop;
                                }
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                //Most likely cause is that we couldn't navigate
                System.out.println("Failed to pathfind! Skipping!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}