package org.millenaire.village;

import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.millenaire.MillCulture;
import org.millenaire.MillCulture.VillageType;
import org.millenaire.VillageGeography;
import org.millenaire.VillageTracker;
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
    private VillageGeography geo;
    private VillageType type;
    private MillCulture culture;
    private World world;
    private BuildingLocation[] buildings;

    private Village (BlockPos b, World worldIn, VillageType typeIn, MillCulture cultureIn) {
        this.setPos(b);
        this.uuid = UUID.randomUUID();
        this.world = worldIn;
        this.type = typeIn;
        this.culture = cultureIn;
        this.geo = new VillageGeography();
        BuildingLocation loc = new BuildingLocation(1, 1, 1, mainBlock, EnumFacing.NORTH);

        ArrayList<BuildingLocation> bl = new ArrayList<>();
        //bl.add(loc); //TODO: It doesn't matter if the village stone is overwritten, right?

        this.geo.update(world, bl, null, mainBlock, world.getHeight(b).getY());
    }

    /**
     * FOR USE BY VILLAGE TRACKER ONLY
     */
    @Deprecated()
    public Village () {

    }

    public static Village createVillage (BlockPos VSPos, World world, VillageType typeIn, MillCulture cultureIn) {
        return new Village(VSPos, world, typeIn, cultureIn);
    }

    public VillageType getType () { return type; }

    public UUID getUUID () { return uuid; }

    public BlockPos getPos () { return mainBlock; }

    /**
     * FOR USE BY VILLAGE TRACKER ONLY
     */
    @Deprecated
    public void setPos (BlockPos pos) { mainBlock = pos; }

    public boolean setupVillage () {
        try {
            EntityMillVillager v = new EntityMillVillager(world, 100100, culture);
            v.setPosition(mainBlock.getX(), mainBlock.getY(), mainBlock.getZ());
            v.setTypeAndGender(MillCulture.normanCulture.getVillagerType("normanKnight"), 1);

            world.spawnEntityInWorld(v);

            for (BuildingProject proj : type.startingBuildings) {
                BuildingPlan p = PlanIO.loadSchematic(PlanIO.getBuildingTag(ResourceLocationUtil.getRL(proj.ID).getResourcePath(), culture, true), culture, proj.lvl);

                BuildingLocation loc = p.findBuildingLocation(geo, new MillPathNavigate(v, world), mainBlock, 64, new Random(), p.buildingOrientation);

                if (loc == null) {
                    System.out.println("Failed to find a suitable location for the " + proj.ID + "!");
                    return false;
                }

                System.out.println("Generating " + proj.ID + " at " + loc.position);

                PlanIO.flattenTerrainForBuilding(p, loc, geo);
                PlanIO.placeBuilding(p, loc, world);

                if (BuildingTypes.getTypeFromProject(proj).isTownHall) {
                    MainLoop:
                    for (int x = 0; x < p.width; x++) {
                        for (int y = 0; y < p.height; y++) {
                            for (int z = 0; z < p.length; z++) {
                                BlockPos pos = new BlockPos(x + loc.position.getX(), y + loc.position.getY() + p.depth, z + loc.position.getZ());

                                if (world.getBlockState(pos).getBlock() == MillBlocks.storedPosition) {
                                    if (world.getBlockState(pos).getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.PATHPOS) {
                                        pos = world.getHeight(pos).add(0, 1, 0);
                                        System.out.println("Moved villager to path start for town hall at " + pos);
                                        v.setPosition(pos.getX(), pos.getY(), pos.getZ());
                                        v.onGround = true; //Dodgy hack?
                                        break MainLoop;
                                    }
                                }
                            }
                        }
                    }
                    p.rebuildPath = false;
                } else {
                    p.rebuildPath = true;
                }

                geo.registerBuilding(p, loc);
            }
            genPaths(new MillPathNavigate(v, world));
            VillageTracker.get(world).registerVillage(getUUID(), this);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void genPaths (MillPathNavigate pathing) {
        BuildingLoop:
        for (BuildingLocation loc : geo.buildingLocations.keySet()) {
            try {
                BuildingPlan plan = geo.buildingLocations.get(loc);

                if (!plan.rebuildPath) continue;

                System.out.println("Pathfinding to " + plan.nativeName);

                for (int x = 0; x < plan.width; x++) {
                    for (int y = 0; y < plan.height; y++) {
                        for (int z = 0; z < plan.length; z++) {
                            BlockPos p = new BlockPos(x + loc.position.getX(), y + loc.position.getY() + plan.depth, z + loc.position.getZ());

                            if (world.getBlockState(p).getBlock() == MillBlocks.storedPosition) {
                                if (world.getBlockState(p).getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.PATHPOS) {
                                    //This is a path
                                    PathEntity path = pathing.getPathToPos(p);

                                    while (!path.isFinished()) {
                                        path.incrementPathIndex();
                                        PathPoint point = path.getPathPointFromIndex(path.getCurrentPathIndex());

                                        BlockPos pathPos = new BlockPos(point.xCoord, point.yCoord, point.zCoord);

                                        world.setBlockState(pathPos, MillBlocks.blockMillPath.getStateFromMeta(BlockMillPath.EnumType.DIRT.getMetadata()));
                                    }
                                    continue BuildingLoop;
                                }
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                //Most likely cause is that we couldn't navigate
                System.out.println("Failed to pathfind! Skipping!");
            }
        }
    }
}