package org.millenaire.village;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import org.millenaire.MillConfig;
import org.millenaire.MillCulture;
import org.millenaire.MillCulture.VillageType;
import org.millenaire.blocks.BlockMillPath;
import org.millenaire.blocks.MillBlocks;
import org.millenaire.blocks.StoredPosition;
import org.millenaire.building.*;
import org.millenaire.entities.EntityMillVillager;
import org.millenaire.entities.TileEntityVillageStone;
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
    private String villageName;

    /**
     * Constructor used by {@link Village#createVillage(BlockPos, World, VillageType, MillCulture, String)}
     *
     * @param b           The position of the village stone
     * @param worldIn     The world the village is in
     * @param typeIn      The {@link VillageType type} of village this is.
     * @param cultureIn   The {@link MillCulture culture of the village}
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
     * @param world The world this village is in.
     */
    Village(World world) {
        this.world = world;
    }

    /**
     * Creates a village. Note that it will not be spawned until {@link Village#setupVillage()} is called.
     *
     * @param VSPos       The position of the {@link org.millenaire.entities.TileEntityVillageStone Village Stone}
     * @param world       The world this village is in.
     * @param typeIn      The {@link VillageType type} of village this is (or will be)
     * @param cultureIn   The {@link MillCulture culture} of the village.
     * @param villageName The name of the village, as shown to the player.
     * @return The instantiated Village object.
     */
    public static Village createVillage(BlockPos VSPos, World world, VillageType typeIn, MillCulture cultureIn, String villageName) {
        return new Village(VSPos, world, typeIn, cultureIn, villageName);
    }

    /**
     * Get the {@link VillageType} of this village.
     *
     * @return The type
     */
    public VillageType getType() {
        return type;
    }

    /**
     * Sets the {@link VillageType} of this village. Should in theory only be used when loading a village from a save.
     * Will log if the type is overwritten.
     *
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
     *
     * @return The {@link UUID} of this vilage.
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Sets the unique identifier of this village. Should in theory only be used when loading a village from a save.
     *
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
     *
     * @return The {@link BlockPos position} of the stone.
     */
    public BlockPos getPos() {
        return mainBlock;
    }

    /**
     * Set the position of the Village Stone for this village
     *
     * @param pos The position of the Village Stone
     */
    void setPos(BlockPos pos) {
        mainBlock = pos;
    }

    public void readDataFromTE() {
        BlockPos pos = getPos();

        if (world.getTileEntity(pos) == null || !(world.getTileEntity(pos) instanceof TileEntityVillageStone)) {
            System.err.println("Village#setPos: Block at " + pos + " is not a TEVS! Unable to load village data!");
            return;
        }

        System.out.println("Reading village information from TEVS...");

        TileEntityVillageStone te = (TileEntityVillageStone) world.getTileEntity(pos);
        villageName = te.villageName;
        type = te.villageType;
        culture = MillCulture.getCulture(te.culture);
        geography = new VillageGeography();
        this.geography.update(world, null, null, pos, world.getHeight(pos).getY());

        for (int i = 0; i < te.buildings.tagCount(); i++) {
            NBTTagCompound buildingTag = te.buildings.getCompoundTagAt(i);

            Building building = PlanIO.loadSchematic(PlanIO.getBuildingTag(
                    ResourceLocationUtil.getRL(buildingTag.getString("ID")).getResourcePath(), culture, true),
                    culture, buildingTag.getInteger("Level"), buildingTag.getString("ID"));

            if(building == null) {
                System.err.println("Village " + villageName + " was saved with building " + buildingTag.getString("ID") +
                        " level " + buildingTag.getInteger("Level") + " but that building, at that level, could not be found.");
                continue;
            }

            BuildingLocation location = new BuildingLocation(building, BlockPos.fromLong(buildingTag.getLong("StartingPos")), EnumFacing.byName(buildingTag.getString("Orientation")));
            System.out.println("Loaded Building " + building.nativeName + " at location " + location + " from NBT");
            geography.registerBuilding(building, location);
        }

        geography.validateAllBuildings();

        System.out.println("Name: " + villageName + "; Type: " + type + "; Culture: " + culture.cultureName + "; Buildings: " + geography.buildingLocations.size() + ";");
    }

    /**
     * Generates starting buildings and paths.
     */
    public void setupVillage() {
        try {
            EntityMillVillager v = new EntityMillVillager(world, 100100, culture, this);
            v.setPosition(mainBlock.getX(), mainBlock.getY(), mainBlock.getZ());
            v.setTypeAndGender(MillCulture.normanCulture.getVillagerTypeByID("normanKnight"), 1);
            MillPathNavigate mpn = new MillPathNavigate(v, world);

            world.spawnEntityInWorld(v);

            for (BuildingRecord startingBuilding : type.startingBuildings) {
                Building building = PlanIO.loadSchematic(PlanIO.getBuildingTag(ResourceLocationUtil.getRL(startingBuilding.ID).getResourcePath(), culture, true), culture, startingBuilding.lvl, startingBuilding.ID);

                if (building == null) {
                    System.err.println("Building " + startingBuilding.ID + ", specified in the starting buildings for village type " + type.id + ", could not be loaded!");
                    continue;
                }

                System.out.println("Finding a place to put a " + startingBuilding.ID);

                BuildingLocation loc = building.findBuildingLocation(geography, mpn, mainBlock, 64, new Random(), building.buildingOrientation);

                if (loc == null) {
                    throw new Exception("Failed to find a suitable location for the " + startingBuilding.ID + "!");
                }

                System.out.println("Generating " + startingBuilding.ID + " at " + loc.position);

                geography.registerBuilding(building, loc);

                PlanIO.flattenTerrainForBuilding(building, loc, geography);
                PlanIO.placeBuilding(building, loc, world);

                geography.update(world, new ArrayList<>(geography.buildingLocations.keySet()), loc, mainBlock, world.getHeight(getPos()).getY());
                mpn.invalidateConnections();

                if (BuildingTypes.getTypeFromProject(startingBuilding).isTownHall) {
                    building.isCenter = true;

                    MainLoop:
                    for (int x = loc.minX; x < loc.maxX; x++) {
                        for (int y = loc.minY; y < loc.maxY; y++) {
                            for (int z = loc.minZ; z < loc.maxZ; z++) {
                                BlockPos pos = new BlockPos(x, y, z);
                                if (world.getBlockState(pos).getBlock() == MillBlocks.storedPosition) {
                                    if (world.getBlockState(pos).getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.PATHPOS) {
                                        pos = world.getHeight(pos).add(0, 1, 0);
                                        v.setPosition(pos.getX() - 0.5D, pos.getY(), pos.getZ() - 0.5D);
                                        System.out.println("Moved villager to path start for town hall at " + pos);
                                        v.onGround = true; //Dodgy hack?
                                        break MainLoop;
                                    }
                                }
                            }
                        }
                    }
                    building.rebuildPath = false;
                } else
                    building.rebuildPath = true;
            }

            geography.update(world, new ArrayList<>(geography.buildingLocations.keySet()), null, mainBlock, world.getHeight(mainBlock).getY());
            v.useNewPathingAtThisPoint = true;
            genPaths(new MillPathNavigate(v, world));

            VillageTracker.get(world).registerVillage(getUUID(), this);

            if (MillConfig.villageAnnouncement) {
                for (int i = 0; i < world.playerEntities.size(); i++)
                    world.playerEntities.get(i).addChatMessage(new ChatComponentText(culture + " village " + villageName + " discovered at " + mainBlock.getX() + ", " + mainBlock.getY() + ", " + mainBlock.getZ()));
            }

            System.out.println(culture + " village " + villageName + " created at " + mainBlock.getX() + ", " + mainBlock.getY() + ", " + mainBlock.getZ());

        } catch (Exception e) {
            if (!e.getMessage().contains("Failed to find a suitable location"))
                e.printStackTrace();
            else
                System.out.println(e.getMessage());

            System.out.println("Village failed to create. Not registering!");
            VillageTracker.get(world).unregisterVillagePos(mainBlock);
            world.setBlockToAir(mainBlock);
        }
    }

    /**
     * Builds all the paths in the village at generation time.
     *
     * @param pathing The {@link MillPathNavigate} to use to generate paths.
     */
    private void genPaths(MillPathNavigate pathing) {
        System.out.println("Generating Village Paths...");
        pathing.villager.useNewPathingAtThisPoint = true;

        pathing.setSearchRange(400);

        for (BuildingLocation loc : geography.buildingLocations.keySet()) {
            try {
                Building plan = geography.buildingLocations.get(loc);

                if (!plan.rebuildPath) continue;

                System.out.println("Pathfinding to " + plan.nativeName);

                //System.out.println("Looking for path marker...");
                boolean foundAnyMarker = false;

                for (int x = loc.minX; x < loc.maxX; x++) {
                    for (int y = loc.minY; y < loc.maxY; y++) {
                        for (int z = loc.minZ; z < loc.maxZ; z++) {
                            BlockPos p = new BlockPos(x, y, z);

                            if (world.getBlockState(p).getBlock() == MillBlocks.storedPosition) {
                                if (world.getBlockState(p).getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.PATHPOS) {
                                    //System.out.println("Found a path marker at " + p);
                                    //This is a path
                                    foundAnyMarker = true;
                                    IBlockState oldState = world.getBlockState(p);
                                    world.setBlockToAir(p);
                                    PathEntity path = pathing.getPathToPos(p.down());
                                    world.setBlockState(p, oldState);

                                    while (!path.isFinished()) {
                                        PathPoint point = path.getPathPointFromIndex(path.getCurrentPathIndex());

                                        BlockPos pathPos = new BlockPos(point.xCoord, point.yCoord, point.zCoord);

                                        BlockPos above = pathPos.up();
                                        Block b = world.getBlockState(above).getBlock();

                                        IBlockState fillerBlock = world.getBiomeGenForCoords(above).fillerBlock
                                                .getBlock() == Blocks.sand ? Blocks.sandstone.getDefaultState()
                                                : world.getBiomeGenForCoords(above).fillerBlock;

                                        while (b instanceof BlockFluidBase || b instanceof BlockLiquid) {
                                            //we're underwater, build a bridge
                                            world.setBlockState(above, fillerBlock);
                                            pathPos = pathPos.up();
                                            above = above.up();
                                            b = world.getBlockState(above).getBlock();
                                        }

                                        if (VillageGeography.isBlockIdGround(world.getBlockState(pathPos).getBlock()))
                                            world.setBlockState(pathPos, MillBlocks.blockMillPath.getStateFromMeta(BlockMillPath.EnumType.DIRT.getMetadata()));

                                        path.incrementPathIndex();
                                    }
                                    //System.out.println("Built a path: " + path);
                                }
                            }
                        }
                    }
                }

                if (!foundAnyMarker)
                    System.out.println("Failed to find a path marker for " + plan.nativeName + " at " + loc.position);
            } catch (NullPointerException e) {
                //Most likely cause is that we couldn't navigate
                System.out.println("Failed to pathfind! Skipping!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}