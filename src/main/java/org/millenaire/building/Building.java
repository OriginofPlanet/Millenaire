package org.millenaire.building;

import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.millenaire.CommonUtilities;
import org.millenaire.MillCulture;
import org.millenaire.VillageGeography;
import org.millenaire.blocks.*;
import org.millenaire.pathing.MillPathNavigate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.millenaire.blocks.BlockDecorativeEarth.EnumType;
import static org.millenaire.blocks.BlockDecorativeEarth.VARIANT;

/**
 * Represents a building itself, or the schematic for it.
 */
public class Building {
    /**
     * The length of this building.
     */
    public int length;
    /**
     * The width of this building
     */
    public int width;
    /**
     * The height of this building
     */
    public int height;
    /**
     * TODO
     */
    public int lengthOffset;
    /**
     * TODO
     */
    public int widthOffset;
    /**
     * How deep into the ground this building is recessed (in the case of a negative number) or (in the case of a positive
     * number) how far above the surface it is. E.g. -1 puts the bottom layer (often the floor) of the building level with the ground.
     */
    public int depth;
    /**
     * How far around this building to clear.
     */
    public int areaToClear;

    /**
     * When finding a building location, represents the distance from the central point where we can test for a valid
     * build.
     */
    public float minDistance, maxDistance;

    /**
     * Whether this is the central building in the village or not.
     */
    public boolean isCenter = false;
    /**
     * Whether this is an upgrade to an existing building
     */
    public boolean isUpgrade = false;
    /**
     * Whether this is a sub-building of another building.
     */
    public boolean isSubBuilding = false;

    /**
     * The cost of this building in terms of resources
     */
    public List<ResourceCost> resCost;
    /**
     * Which way this building is facing
     */
    public EnumFacing buildingOrientation;
    /**
     * What the natives call this building
     */
    public String nativeName;
    /**
     * All valid male villager types
     */
    public String[] maleVillagerType;
    /**
     * All valid female villager types
     */
    public String[] femaleVillagerType;
    /**
     * All the sub-buildings for this building.
     */
    public List<String> subBuildings;
    /**
     * The relative position of the path - i.e. positive numbers are above the floor, negative below.
     */
    public int pathLevel = 0;
    /**
     * How wide paths should be.
     */
    public int pathWidth = 2;
    /**
     * Whether existing paths should be rebuilt.
     */
    public boolean rebuildPath = false;
    /**
     * All the blocks in this building.
     */
    IBlockState[][][] blocksInBuilding;

    public Building(MillCulture cultureIn, int level) {

        //computeCost();
    }

    public Building(int level, int pathLevelIn, Building parent) {
        length = parent.length;
        width = parent.width;
        height = parent.height;
        lengthOffset = parent.lengthOffset;
        widthOffset = parent.widthOffset;
        depth = parent.depth;
        areaToClear = parent.areaToClear;
        minDistance = parent.minDistance;
        maxDistance = parent.maxDistance;
        isUpgrade = true;
        isSubBuilding = parent.isSubBuilding;
        buildingOrientation = parent.buildingOrientation;
        nativeName = parent.nativeName;
        maleVillagerType = parent.maleVillagerType;
        femaleVillagerType = parent.femaleVillagerType;
        pathWidth = parent.pathWidth;

        pathLevel = pathLevelIn;
        if (pathLevel != parent.pathLevel) {
            rebuildPath = true;
        }

        //computeCost();
    }

    /**
     * Set the dimensions of this building.
     *
     * @param lenIn The length of the building.
     * @param widIn The width of the building.
     * @return This, for chaining.
     */
    public Building setLengthWidth(int lenIn, int widIn) {
        this.length = lenIn;
        this.width = widIn;

        lengthOffset = (int) Math.floor(length * 0.5);
        widthOffset = (int) Math.floor(width * 0.5);

        return this;
    }

    /**
     * Set the dimensions of this building.
     *
     * @param hiIn  The height of the building
     * @param depIn How far this building is above/below the ground. 0 is one block above the existing floor.
     * @return This, for chaining.
     */
    public Building setHeightDepth(int hiIn, int depIn) {
        this.height = hiIn;
        this.depth = depIn;

        return this;
    }

    /**
     * Sets the area to clear around this building.
     *
     * @param areaIn The radius of the area to clear.
     * @return This, for chaining.
     */
    public Building setArea(int areaIn) {
        this.areaToClear = areaIn;

        return this;
    }

    /**
     * Sets the min and max distance from the desired location this building can generate.
     *
     * @param minIn The min distance
     * @param maxIn The max distance
     * @return This, for chaining.
     */
    public Building setDistance(float minIn, float maxIn) {
        this.minDistance = minIn;
        this.maxDistance = maxIn;

        return this;
    }

    /**
     * Sets whether this is a sub-building
     *
     * @param subIn Whether this is a sub-building.
     * @return This, for chaining.
     */
    public Building setSubBuilding(boolean subIn) {
        this.isSubBuilding = subIn;

        return this;
    }

    /**
     * Sets which way this building is facing.
     *
     * @param orientIn The direction this is facing.
     * @return This, for chaining.
     */
    public Building setOrientation(EnumFacing orientIn) {
        this.buildingOrientation = orientIn;

        return this;
    }

    /**
     * Sets the name of the building, and which male/female villagers it contains.
     *
     * @param nameIn   The name of the building, in the native language.
     * @param maleIn   The male villagers it contains
     * @param femaleIn The female villagers it contains.
     * @return This, for chaining.
     */
    public Building setNameAndType(String nameIn, String[] maleIn, String[] femaleIn) {
        this.nativeName = nameIn;
        this.maleVillagerType = maleIn;
        this.femaleVillagerType = femaleIn;

        return this;
    }

    /**
     * Sets the blocks this building is made of.
     *
     * @param arrayIn An array of block states <b>in the format [y][z][x]</b>
     * @return This, for chaining.
     */
    public Building setActualContents(IBlockState[][][] arrayIn) {
        this.blocksInBuilding = arrayIn;
        computeCost();

        return this;
    }

    /**
     * Attempts to find somewhere for this building to spawn, between {@link Building#minDistance} and {@link Building#maxDistance}
     * from the given point.
     *
     * @param geo              The geography of the village this building is in, used to determine if the terrain is suitable.
     * @param pathing          An instance of {@link MillPathNavigate}, for determining whether a location is reachable
     * @param center           The central location to search around.
     * @param radiusMultiplier A multiplier on the min and max distances from the central location.
     * @param random           A random
     * @param orientation      Which way this building is or should be facing.
     * @return A {@link BuildingLocation}, which contains a location if a valid placement was found, or doesn't if one wasn't.
     */
    public BuildingLocation findBuildingLocation(VillageGeography geo, MillPathNavigate pathing, BlockPos center, int radiusMultiplier, Random random, EnumFacing orientation) {
        final int ci = center.getX() - geo.mapStartX;
        final int cj = center.getZ() - geo.mapStartZ;

        int radius = (int) (radiusMultiplier * minDistance);
        radiusMultiplier = (int) (radiusMultiplier * maxDistance);

        for (int i = 0; i < geo.length; i++) {
            for (int j = 0; j < geo.width; j++) {
                geo.buildTested[i][j] = false;
            }
        }

        while (radius < radiusMultiplier) {
            final int minxX = Math.max(0, ci - radius);
            final int maxX = Math.min(geo.length - 1, ci + radius);
            final int minZ = Math.max(0, cj - radius);
            final int maxZ = Math.min(geo.width - 1, cj + radius);

            //noinspection Duplicates
            for (int i = minxX; i < maxX; i++) {
                if (cj - radius == minZ) {
                    final LocationReturn lr = testLocation(geo, center, i, minZ, orientation, pathing);

                    if (lr.location != null)
                        return lr.location;
                }
                if (cj + radius == maxZ) {
                    final LocationReturn lr = testLocation(geo, center, i, minZ, orientation, pathing);

                    if (lr.location != null) {
                        return lr.location;
                    }
                }
            }

            //noinspection Duplicates
            for (int j = minZ; j < maxZ; j++) {
                if (ci - radius == minxX) {
                    final LocationReturn lr = testLocation(geo, center, j, minZ, orientation, pathing);

                    if (lr.location != null) {
                        return lr.location;
                    }
                }
                if (ci + radius == maxX) {
                    final LocationReturn lr = testLocation(geo, center, j, minZ, orientation, pathing);

                    if (lr.location != null) {
                        return lr.location;
                    }
                }
            }

            radius++;
        }

        System.out.println("building search unsuccessful");
        return null;
    }

    /**
     * Adds the given amount of the given stack to the costs of this building.
     *
     * @param stack  The resource to add - the amount doesn't matter, but the item and meta does.
     * @param amount The amount of the given resource to add to the cost.
     */
    private void addToCost(ItemStack stack, int amount) {
        for (int i = 0; i < resCost.size(); i++) {
            if (stack.getItem().equals(resCost.get(i).getStack().getItem())) {
                resCost.get(i).add(amount);
            } else {
                resCost.add(new ResourceCost(stack, amount));
            }
        }
    }

    /**
     * Adds the given amount of the given stack to the costs of this building.
     *
     * @param odString The OreDictionary name of the resource to add.
     * @param amount   The amount of the given resource to add to the cost.
     */
    private void addToCost(String odString, int amount) {
        for (int i = 0; i < resCost.size(); i++) {
            if (odString.matches(resCost.get(i).getOreDictionaryName())) {
                resCost.get(i).add(amount);
            } else {
                resCost.add(new ResourceCost(odString, amount));
            }
        }
    }

    /**
     * Checks whether the given block can (or rather, SHOULD) be replaced or if we shouldn't replace this block, for
     * example if it's player-placed or part of another, existing, building.
     *
     * @param state The BlockState of the block to check.
     * @return True if we can and should replace this block, else false.
     */
    private boolean freeBuild(IBlockState state) {
        return state.getBlock() == Blocks.dirt || state.getBlock() == Blocks.water || state.getBlock() == Blocks.leaves || state.getBlock() == Blocks.leaves2 || state.getBlock() == Blocks.grass || state.getBlock() == Blocks.tallgrass || state.getBlock() == Blocks.red_flower || state.getBlock() == Blocks.yellow_flower || state.getBlock() == Blocks.double_plant || state.getBlock() == Blocks.deadbush
                || state.getBlock() == MillBlocks.blockMillPath || state.getBlock() == MillBlocks.blockMillPathSlab || state.equals(MillBlocks.blockDecorativeEarth.getDefaultState().withProperty(VARIANT, EnumType.DIRTWALL));
    }

    /**
     * Populates the resource cost of this building based on the blocks in it.
     */
    private void computeCost() {
        resCost = new ArrayList<>();

        int plankCost = 0, plankOakCost = 0, plankSpruceCost = 0, plankBirchCost = 0, plankJungleCost = 0, plankAcaciaCost = 0, plankDarkCost = 0, glassPaneCost = 0, byzBricksHalf = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < width; k++) {
                    final IBlockState state = blocksInBuilding[i][j][k];

                    Block block = state.getBlock();

                    if (state == null) {
                        System.err.println("BlockState is null at " + i + "/" + j + "/" + k);
                    }

                    if (block == Blocks.log) {
                        switch (state.getValue(BlockOldLog.VARIANT)) {
                            case OAK:
                                plankOakCost += 4;
                                break;
                            case SPRUCE:
                                plankSpruceCost += 4;
                                break;
                            case BIRCH:
                                plankBirchCost += 4;
                                break;
                            case JUNGLE:
                                plankJungleCost += 4;
                                break;
                            case ACACIA:
                                plankAcaciaCost += 4;
                                break;
                            case DARK_OAK:
                                plankJungleCost += 4;
                                break;
                        }
                    } else if (block == Blocks.planks || block == Blocks.wooden_slab) {
                        switch (state.getValue(BlockPlanks.VARIANT)) {
                            case OAK:
                                plankOakCost++;
                                break;
                            case SPRUCE:
                                plankSpruceCost++;
                                break;
                            case BIRCH:
                                plankBirchCost++;
                                break;
                            case JUNGLE:
                                plankJungleCost++;
                                break;
                            case ACACIA:
                                plankAcaciaCost++;
                                break;
                            case DARK_OAK:
                                plankDarkCost++;
                                break;
                        }
                    } else if (block == MillBlocks.byzantineTile) {
                        byzBricksHalf += 2;
                    } else if (block == MillBlocks.byzantineTileSlab) {
                        byzBricksHalf++;
                    } else if (block == MillBlocks.byzantineStoneTile) {
                        byzBricksHalf++;
                        addToCost(new ItemStack(Blocks.stone), 1);
                    } else if (block == Blocks.glass_pane || block == Blocks.stained_glass_pane) {
                        glassPaneCost++;
                    } else if (block == Blocks.glass || block == Blocks.stained_glass) {
                        glassPaneCost += 3;
                    } else if (block == Blocks.crafting_table) {
                        plankCost += 4;
                    } else if (block == Blocks.chest) {
                        plankCost += 8;
                    } else if (block == Blocks.furnace) {
                        addToCost(new ItemStack(Blocks.cobblestone), 8);
                    } else if (block == Blocks.torch) {
                        plankCost++;
                    } else if (block == Blocks.oak_fence) {
                        plankOakCost++;
                    } else if (block == Blocks.spruce_fence) {
                        plankSpruceCost++;
                    } else if (block == Blocks.birch_fence) {
                        plankBirchCost++;
                    } else if (block == Blocks.jungle_fence) {
                        plankJungleCost++;
                    } else if (block == Blocks.acacia_fence) {
                        plankAcaciaCost++;
                    } else if (block == Blocks.dark_oak_fence) {
                        plankDarkCost++;
                    } else if (block == Blocks.oak_fence_gate) {
                        plankOakCost += 4;
                    } else if (block == Blocks.spruce_fence_gate) {
                        plankSpruceCost += 4;
                    } else if (block == Blocks.birch_fence_gate) {
                        plankBirchCost += 4;
                    } else if (block == Blocks.jungle_fence_gate) {
                        plankJungleCost += 4;
                    } else if (block == Blocks.acacia_fence_gate) {
                        plankAcaciaCost += 4;
                    } else if (block == Blocks.dark_oak_fence_gate) {
                        plankDarkCost += 4;
                    } else if (block == Blocks.wooden_pressure_plate) {
                        plankCost += 2;
                    } else if (block == Blocks.stone_pressure_plate) {
                        addToCost(new ItemStack(Blocks.stone), 2);
                    } else if (block == Blocks.stonebrick) {
                        addToCost(new ItemStack(Blocks.stone), 1);
                    } else if (block == Blocks.stone_slab) {
                        switch (state.getValue(BlockStoneSlab.VARIANT)) {
                            case STONE:
                                addToCost(new ItemStack(Blocks.stone), 1);
                                break;
                            case SAND:
                                addToCost(new ItemStack(Blocks.sandstone), 1);
                                break;
                            case WOOD:
                                plankCost++;
                                break;
                            case COBBLESTONE:
                                addToCost(new ItemStack(Blocks.cobblestone), 1);
                                break;
                            case BRICK:
                                addToCost(new ItemStack(Blocks.brick_block), 1);
                                break;
                            case SMOOTHBRICK:
                                addToCost(new ItemStack(Blocks.stone), 1);
                                break;
                        }
                    } else if (state.getBlock() == Blocks.wool) {
                        addToCost(new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 1);
                    } else if (state.getBlock() == Blocks.nether_wart) {
                        addToCost(new ItemStack(Items.nether_wart), 1);
                    } else if (state.getBlock() == Blocks.double_stone_slab && state.getValue(BlockStoneSlab.VARIANT) == BlockStoneSlab.EnumType.STONE) {
                        addToCost(new ItemStack(Blocks.stone), 1);
                    } else if (state.getBlock() == Blocks.iron_block) {
                        addToCost(new ItemStack(Items.iron_ingot), 9);
                    } else if (state.getBlock() == Blocks.anvil) {
                        addToCost(new ItemStack(Items.iron_ingot), 30);
                    } else if (state.getBlock() == Blocks.iron_bars) {
                        addToCost(new ItemStack(Items.iron_ingot), 1);
                    } else if (state.getBlock() == Blocks.gold_block) {
                        addToCost(new ItemStack(Items.gold_ingot), 9);
                    } else if (state.getBlock() == Blocks.cauldron) {
                        addToCost(new ItemStack(Items.iron_ingot), 7);
                    } else if (state.getBlock() == Blocks.cobblestone_wall) {
                        addToCost(new ItemStack(Blocks.cobblestone), 1);
                    } else if (state.getBlock() == MillBlocks.blockMillChest) {
                        plankCost += 8;
                    } else if (state.getBlock() == Blocks.oak_stairs) {
                        plankOakCost += 2;
                    } else if (state.getBlock() == Blocks.spruce_stairs) {
                        plankSpruceCost += 2;
                    } else if (state.getBlock() == Blocks.birch_stairs) {
                        plankBirchCost += 2;
                    } else if (state.getBlock() == Blocks.jungle_stairs) {
                        plankJungleCost += 2;
                    } else if (state.getBlock() == Blocks.acacia_stairs) {
                        plankAcaciaCost += 2;
                    } else if (state.getBlock() == Blocks.dark_oak_stairs) {
                        plankDarkCost += 2;
                    } else if (state.getBlock() == Blocks.stone_stairs) {
                        addToCost(new ItemStack(Blocks.cobblestone), 2);
                    } else if (state.getBlock() == Blocks.stone_brick_stairs) {
                        addToCost(new ItemStack(Blocks.stone), 2);
                    } else if (state.getBlock() == Blocks.sandstone_stairs) {
                        addToCost(new ItemStack(Blocks.sandstone), 2);
                    } else if (state.getBlock() == Blocks.brick_stairs) {
                        addToCost(new ItemStack(Blocks.brick_block), 2);
                    } else if (state.getBlock() == Blocks.ladder) {
                        plankCost += 2;
                    } else if (state.getBlock() == Blocks.standing_sign) {
                        plankCost += 7;
                    } else if (state.getBlock() == Blocks.wall_sign) {
                        plankCost += 7;
                    } else if (state.getBlock() == MillBlocks.blockMillSign) {
                        plankCost += 7;
                    } else if (state.getBlock() == Blocks.oak_door) {
                        plankOakCost += 2;
                    } else if (state.getBlock() == Blocks.spruce_door) {
                        plankSpruceCost += 2;
                    } else if (state.getBlock() == Blocks.birch_door) {
                        plankBirchCost += 2;
                    } else if (state.getBlock() == Blocks.jungle_door) {
                        plankJungleCost += 2;
                    } else if (state.getBlock() == Blocks.acacia_door) {
                        plankAcaciaCost += 2;
                    } else if (state.getBlock() == Blocks.dark_oak_door) {
                        plankDarkCost += 2;
                    } else if (state.getBlock() == Blocks.trapdoor) {
                        plankCost += 6;
                    } else if (state.getBlock() == Blocks.bed && state.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                        plankCost += 3;
                        addToCost(new ItemStack(Blocks.wool, 1, OreDictionary.WILDCARD_VALUE), 3);
                    } else if (state.getBlock() == MillBlocks.emptySericulture) {
                        plankCost += 4;
                    } else if (state.getBlock() != Blocks.air && !freeBuild(state)) {
                        addToCost(new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)), 1);
                    }
                }
            }
        }

        if (plankCost > 0) {
            addToCost("logWood", (int) Math.max(Math.ceil(plankCost * 1.0 / 4), 1));
        }

        if (plankOakCost > 0) {
            addToCost(new ItemStack(Blocks.log, 1, Blocks.log.getMetaFromState(Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK))), (int) Math.max(Math.ceil(plankOakCost * 1.0 / 4), 1));
        }

        if (plankSpruceCost > 0) {
            addToCost(new ItemStack(Blocks.log, 1, Blocks.log.getMetaFromState(Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE))), (int) Math.max(Math.ceil(plankSpruceCost * 1.0 / 4), 1));
        }

        if (plankBirchCost > 0) {
            addToCost(new ItemStack(Blocks.log, 1, Blocks.log.getMetaFromState(Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.BIRCH))), (int) Math.max(Math.ceil(plankBirchCost * 1.0 / 4), 1));
        }

        if (plankJungleCost > 0) {
            addToCost(new ItemStack(Blocks.log, 1, Blocks.log.getMetaFromState(Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE))), (int) Math.max(Math.ceil(plankJungleCost * 1.0 / 4), 1));
        }

        if (plankAcaciaCost > 0) {
            addToCost(new ItemStack(Blocks.log2, 1, Blocks.log2.getMetaFromState(Blocks.log2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA))), (int) Math.max(Math.ceil(plankJungleCost * 1.0 / 4), 1));
        }

        if (plankDarkCost > 0) {
            addToCost(new ItemStack(Blocks.log2, 1, Blocks.log2.getMetaFromState(Blocks.log2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.DARK_OAK))), (int) Math.max(Math.ceil(plankJungleCost * 1.0 / 4), 1));
        }

        if (glassPaneCost > 0) {
            addToCost(new ItemStack(Blocks.glass), (int) Math.max(Math.ceil(glassPaneCost * 6.0 / 16), 1));
        }

        if (byzBricksHalf > 0) {
            addToCost(new ItemStack(MillBlocks.byzantineTile), (int) Math.max(Math.ceil(byzBricksHalf / 2), 1));
        }
    }

    /**
     * Checks if we can build in the specified location.
     * @param geography The village geography, used to check terrain.
     * @param center The center of the village
     * @param x The x coord to check
     * @param z The z coord to check
     * @param facing Which way this building is facing.
     * @param pathing A {@link MillPathNavigate} instance, used to check that the given location is reachable.
     * @return A LocationReturn with a location if a valid placement if found, or one with an error code and the coordinates of the block that caused the error otherwise.
     */
    private LocationReturn testLocation(VillageGeography geography, BlockPos center, int x, int z, EnumFacing facing, MillPathNavigate pathing) {
        EnumFacing orientation;

        pathing.setSearchRange(128);

        final int relativeX = x + geography.mapStartX - center.getX();
        final int relativeZ = z + geography.mapStartZ - center.getZ();

        geography.buildTested[x][z] = true;

        if (facing == null || facing.getIndex() < 2) {
            if (relativeX * relativeX > relativeZ * relativeZ) {
                if (relativeX > 0) {
                    orientation = EnumFacing.NORTH;
                } else {
                    orientation = EnumFacing.SOUTH;
                }
            } else {
                if (relativeZ > 0) {
                    orientation = EnumFacing.EAST;
                } else {
                    orientation = EnumFacing.WEST;
                }
            }
        } else {
            orientation = facing;
        }

        orientation = EnumFacing.getFront((orientation.getHorizontalIndex() + buildingOrientation.getHorizontalIndex()) % 4);

        int xwidth;
        int zwidth;

        if (orientation == EnumFacing.NORTH || orientation == EnumFacing.SOUTH) {
            xwidth = length + areaToClear * 2 + 2;
            zwidth = width + areaToClear * 2 + 2;
        } else {
            xwidth = width + areaToClear * 2 + 2;
            zwidth = length + areaToClear * 2 + 2;
        }

        int altitudeTotal = 0;
        int nbPoints = 0;
        int nbError = 0;

        int allowedErrors = 10;
        boolean hugeBuilding = false;

        if (xwidth * zwidth > 6000) {
            allowedErrors = 1500;
            hugeBuilding = true;
        } else if (xwidth * zwidth > 200) {
            allowedErrors = xwidth * zwidth / 20;
        }

        boolean reachable = false;

        for (int i = 0; i <= xwidth; i++) {
            for (int j = 0; j <= zwidth; j++) {
                int ci, cj;
                ci = x + i;
                cj = z + j;


                if (ci < 0 || cj < 0 || ci >= geography.length || cj >= geography.width) {
                    BlockPos p = new BlockPos(ci + geography.mapStartX, 64, cj + geography.mapStartZ);

                    return new LocationReturn(LocationReturn.OUTSIDE_RADIUS, p);
                }

                if (geography.buildingLoc[ci][cj]) {
                    if (nbError > allowedErrors) {
                        final BlockPos p = new BlockPos(ci + geography.mapStartX, 64, cj + geography.mapStartZ);

                        return new LocationReturn(LocationReturn.LOCATION_CLASH, p);
                    } else {
                        nbError += 5;
                    }
                } else if (geography.buildingForbidden[ci][cj]) {
                    if (!hugeBuilding || nbError > allowedErrors) {
                        final BlockPos p = new BlockPos(ci + geography.mapStartX, 64, cj + geography.mapStartZ);

                        return new LocationReturn(LocationReturn.CONSTRUCTION_FORBIDDEN, p);
                    } else {
                        nbError++;
                    }
                } else if (geography.danger[ci][cj]) {
                    if (nbError > allowedErrors) {
                        final BlockPos p = new BlockPos(ci + geography.mapStartX, 64, cj + geography.mapStartZ);

                        return new LocationReturn(LocationReturn.DANGER, p);
                    } else {
                        nbError++;
                    }
                } else if (!geography.canBuild[ci][cj]) {
                    if (nbError > allowedErrors) {
                        final BlockPos p = new BlockPos(ci + geography.mapStartX, 64, cj + geography.mapStartZ);

                        return new LocationReturn(LocationReturn.WRONG_ALTITUDE, p);
                    } else {
                        nbError++;
                    }
                } else if (geography.water[ci][cj]) {
                    if (nbError > allowedErrors) {
                        final BlockPos p = new BlockPos(ci + geography.mapStartX, 64, cj + geography.mapStartZ);

                        return new LocationReturn(LocationReturn.NOT_REACHABLE, p);
                    } else {
                        nbError++;
                    }
                }

                reachable = !pathing.tryMoveToXYZ(ci, geography.world.getHeight(new BlockPos(ci, 10, cj)).getY(), cj, 0.5D);

                altitudeTotal += geography.topGround[ci][cj];
                nbPoints++;
            }
        }

        if (!reachable) {
            return new LocationReturn(LocationReturn.NOT_REACHABLE, center);
        }

        final int altitude = (int) (1 + altitudeTotal * 1.0f / nbPoints);

        //Adjust for trees + plants
        World world = geography.world;
        BlockPos highestY = new BlockPos(x + geography.mapStartX, altitude, z + geography.mapStartZ);

        Block b = world.getBlockState(highestY).getBlock();
        while (b == Blocks.leaves || b == Blocks.leaves2 || b == Blocks.log || b == Blocks.log2 || b == Blocks.vine
                || b == Blocks.brown_mushroom_block || b == Blocks.red_mushroom_block || b == Blocks.tallgrass
                || b == Blocks.double_plant) {
            highestY = highestY.subtract(new Vec3i(0, 1, 0));
            b = world.getBlockState(highestY).getBlock();
        }

        final BuildingLocation l = new BuildingLocation(this, highestY, orientation);

        return new LocationReturn(l);
    }

    /**
     * Returns an array of all special points and blocks in this building.
     * @param worldIn The world to be reading from
     * @param location The location of the building
     * @param villageGeneration True if we are in WorldGen, else false.
     * @return An array of {@link BuildingBlock BuildingBlocks}
     */
    public BuildingBlock[] getBuildingPoints(World worldIn, BuildingLocation location, boolean villageGeneration) {
        final int x = location.position.getX();
        final int y = location.position.getY();
        final int z = location.position.getZ();
        List<BuildingBlock> bblocks = new ArrayList<>();

        EnumFacing orientation = location.orientation;

        if (!isUpgrade && !isSubBuilding) {
            // filling above ground area with air

            for (int j = -areaToClear; j < length + areaToClear; j++) {
                for (int k = -areaToClear; k < width + areaToClear; k++) {
                    for (int i = height + 50; i > -1; i--) {
                        final int ak = j % 2 == 0 ? k : width - k - 1;

                        // how far from building we are in the margin (0=in
                        // building itself)
                        int offset = 0;

                        if (j < 0) {
                            offset = -j;
                        } else if (j >= length - 1) {
                            offset = j - length + 1;
                        }

                        if (ak < 0 && -ak > offset) {
                            offset = -ak;
                        } else if (ak >= width - 1 && ak - width + 1 > offset) {
                            offset = ak - width + 1;
                        }

                        offset--;

                        if (i >= offset - 1) {
                            // for each block away from building, one extra height allowed
                            final BlockPos p = CommonUtilities.adjustForOrientation(x, y + i, z, j - lengthOffset, ak - widthOffset, orientation);
                            bblocks.add(new BuildingBlock(null, p, BuildingBlock.CLEARGROUND));
                        } else {
                            final BlockPos p = CommonUtilities.adjustForOrientation(x, y + i, z, j - lengthOffset, k - widthOffset, orientation);
                            bblocks.add(new BuildingBlock(null, p, BuildingBlock.CLEARTREE));
                        }
                    }
                }
            }

            // filling Foundations with Soil
            for (int j = -areaToClear; j < length + areaToClear; j++) {
                for (int k = -areaToClear; k < width + areaToClear; k++) {
                    for (int i = -10 + depth; i < 0; i++) {
                        final int ak = j % 2 == 0 ? k : width - k - 1;

                        // how far from building we are in the margin (0=in building itself)
                        int offset = 0;

                        if (j < 0) {
                            offset = -j;
                        } else if (j >= length - 1) {
                            offset = j - length + 1;
                        }

                        if (ak < 0 && -ak > offset) {
                            offset = -ak;
                        } else if (ak >= width - 1 && ak - width + 1 > offset) {
                            offset = ak - width + 1;
                        }

                        offset--;

                        if (-i > offset) {
                            final BlockPos p = CommonUtilities.adjustForOrientation(x, y + i, z, j - lengthOffset, k - widthOffset, orientation);
                            bblocks.add(new BuildingBlock(null, p, BuildingBlock.PRESERVEGROUNDDEPTH));
                        } else if (-i == offset) {
                            final BlockPos p = CommonUtilities.adjustForOrientation(x, y + i, z, j - lengthOffset, k - widthOffset, orientation);
                            bblocks.add(new BuildingBlock(null, p, BuildingBlock.PRESERVEGROUNDSURFACE));
                        } else {
                            final BlockPos p = CommonUtilities.adjustForOrientation(x, y + i, z, j - lengthOffset, k - widthOffset, orientation);
                            bblocks.add(new BuildingBlock(null, p, BuildingBlock.CLEARTREE));
                        }
                    }
                }
            }
        }

        //Starting with Deletion
        for (int i = height - 1; i >= 0; i--) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < width; k++) {
                    int ak = j % 2 == 0 ? k : width - k - 1;

                    IBlockState state = blocksInBuilding[i][j][ak];

                    BlockPos p = CommonUtilities.adjustForOrientation(x, y + i + depth, z, j - lengthOffset, ak - widthOffset, orientation);

                    if (state.getBlock() == Blocks.air) {
                        bblocks.add(new BuildingBlock(state, p));
                    }
                }
            }
        }

        //Standard Blocks
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < width; k++) {
                    int ak = j % 2 == 0 ? k : width - k - 1;
                    int ai = i + depth < 0 ? -i - depth - 1 : i;

                    IBlockState state = blocksInBuilding[ai][j][ak];

                    BlockPos p = CommonUtilities.adjustForOrientation(x, y + ai + depth, z, j - lengthOffset, ak - widthOffset, orientation);
                    for (IProperty prop : state.getProperties().keySet()) {
                        if (prop.getName().equals("facing")) {
                            if (((EnumFacing) state.getValue(prop)).getAxis() != EnumFacing.Axis.Y) {
                                state = state.withProperty(prop, EnumFacing.getHorizontal(((EnumFacing) state.getValue(prop)).getHorizontalIndex() + orientation.getHorizontalIndex() % 4));
                            }
                        } else if (prop.getName().equals("axis")) {
                            EnumFacing.Axis orientedAxis;

                            if (orientation == EnumFacing.NORTH || orientation == EnumFacing.SOUTH) {
                                orientedAxis = EnumFacing.Axis.X;
                            } else if (orientation == EnumFacing.EAST || orientation == EnumFacing.WEST) {
                                orientedAxis = EnumFacing.Axis.Z;
                            } else {
                                orientedAxis = EnumFacing.Axis.Y;
                                System.err.println("How the f#%$ did orientation end up vertical?");
                            }
                            if (!orientedAxis.apply(buildingOrientation)) {
                                if (!(state.getBlock() instanceof BlockLog)) {
                                    if (state.getValue(prop) == EnumFacing.Axis.X) {
                                        state = state.withProperty(prop, EnumFacing.Axis.Z);
                                    } else if (state.getValue(prop) == EnumFacing.Axis.Z) {
                                        state = state.withProperty(prop, EnumFacing.Axis.X);
                                    }
                                } else {
                                    if (state.getValue(prop) == BlockLog.EnumAxis.X) {
                                        state = state.withProperty(prop, BlockLog.EnumAxis.Z);
                                    } else if (state.getValue(prop) == BlockLog.EnumAxis.Z) {
                                        state = state.withProperty(prop, BlockLog.EnumAxis.X);
                                    }
                                }
                            }
                        }
                    }

                    setReferencePositions(state, p, location);

                    if (state.getBlock() != null && state.getBlock() != Blocks.air && firstPass(state)) {
                        if (state.getBlock() == Blocks.farmland) {
                            state = Blocks.dirt.getDefaultState();
                        }

                        bblocks.add(new BuildingBlock(state, p));
                    }
                }
            }
        }

        //Decorative and Other Blocks
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < width; k++) {
                    int ak = j % 2 == 0 ? k : width - k - 1;
                    int ai = i + depth < 0 ? -i - depth - 1 : i;

                    IBlockState state = blocksInBuilding[ai][j][ak];

                    BlockPos p = CommonUtilities.adjustForOrientation(x, y + ai + depth, z, j - lengthOffset, ak - widthOffset, orientation);
                    for (IProperty prop : state.getProperties().keySet()) {
                        if (prop.getName().equals("facing")) {
                            if (((EnumFacing) state.getValue(prop)).getAxis() != EnumFacing.Axis.Y) {
                                state = state.withProperty(prop, EnumFacing.getHorizontal(((EnumFacing) state.getValue(prop)).getHorizontalIndex() + orientation.getHorizontalIndex() % 4));
                            }
                        } else if (prop.getName().equals("axis")) {
                            EnumFacing.Axis orientedAxis;

                            if (orientation == EnumFacing.NORTH || orientation == EnumFacing.SOUTH) {
                                orientedAxis = EnumFacing.Axis.X;
                            } else if (orientation == EnumFacing.EAST || orientation == EnumFacing.WEST) {
                                orientedAxis = EnumFacing.Axis.Z;
                            } else {
                                orientedAxis = EnumFacing.Axis.Y;
                                System.err.println("How the f#%$ did orientation end up vertical?");
                            }
                            if (!orientedAxis.apply(buildingOrientation)) {
                                if (!(state.getBlock() instanceof BlockLog)) {
                                    if (state.getValue(prop) == EnumFacing.Axis.X) {
                                        state = state.withProperty(prop, EnumFacing.Axis.Z);
                                    } else if (state.getValue(prop) == EnumFacing.Axis.Z) {
                                        state = state.withProperty(prop, EnumFacing.Axis.X);
                                    }
                                } else {
                                    if (state.getValue(prop) == BlockLog.EnumAxis.X) {
                                        state = state.withProperty(prop, BlockLog.EnumAxis.Z);
                                    } else if (state.getValue(prop) == BlockLog.EnumAxis.Z) {
                                        state = state.withProperty(prop, BlockLog.EnumAxis.X);
                                    }
                                }
                            }
                        }
                    }

                    if (state.getBlock() != null && !firstPass(state)) {
                        bblocks.add(new BuildingBlock(state, p));
                    }
                }
            }
        }

        final HashMap<BlockPos, BuildingBlock> bbmap = new HashMap<BlockPos, BuildingBlock>();

        final boolean[] toDelete = new boolean[bblocks.size()];

        for (int i = 0; i < bblocks.size(); i++) {
            final BuildingBlock bb = bblocks.get(i);
            Block block = bb.blockState != null ? bb.blockState.getBlock() : null;
            IBlockState state = bb.blockState;
            int special = bb.specialBlock;

            if (bbmap.containsKey(bb.position)) {
                block = bbmap.get(bb.position).blockState != null ? bbmap.get(bb.position).blockState.getBlock() : null;
                state = bbmap.get(bb.position).blockState;
                special = bbmap.get(bb.position).specialBlock;
            } else {
                block = worldIn.getBlockState(bb.position).getBlock();
                state = worldIn.getBlockState(bb.position);
                special = 0;
            }

            Block bDirt = bb.blockState != null ? bb.blockState.getBlock() : null;

            if ((state == bb.blockState && special == 0 || block == Blocks.grass && bDirt == Blocks.dirt) && bb.specialBlock == 0) {
                toDelete[i] = true;
            } else if (bb.specialBlock == BuildingBlock.CLEARTREE && block != Blocks.log && block != Blocks.leaves) {
                toDelete[i] = true;
            } else if (bb.specialBlock == BuildingBlock.CLEARGROUND && (block == null || block == Blocks.air)) {
                toDelete[i] = true;
            } else if (bb.specialBlock == BuildingBlock.PRESERVEGROUNDDEPTH && CommonUtilities.getValidGroundBlock(block, false) == block) {
                toDelete[i] = true;
            } else if (bb.specialBlock == BuildingBlock.PRESERVEGROUNDSURFACE && CommonUtilities.getValidGroundBlock(block, true) == block) {
                toDelete[i] = true;
            } else {
                bbmap.put(bb.position, bb);
                toDelete[i] = false;
            }
        }

        for (int i = toDelete.length - 1; i >= 0; i--) {
            if (toDelete[i]) {
                bblocks.remove(i);
            }
        }

        BuildingBlock[] abblocks = new BuildingBlock[bblocks.size()];

        for (int i = 0; i < bblocks.size(); i++) {
            abblocks[i] = bblocks.get(i);
        }

        return abblocks;
    }

    /**
     * Returns true if this is a valid block.
     * @param state The block to check
     * @return True if this is a solid cube (or one of the millenaire decorative blocks)
     */
    private boolean firstPass(IBlockState state) {
        //TODO: This cannot ever work on servers - creative tabs don't exist, so I've replaced with if it's a cube
        //TODO: But do fences fall into this category? Probably not. Need an alternative.
        return /*state.getBlock().getCreativeTabToDisplayOn() == CreativeTabs.tabBlock*/ state.getBlock().isNormalCube() || state.getBlock() instanceof BlockDecorativeEarth || state.getBlock() instanceof BlockDecorativeWood ||
                state.getBlock() instanceof BlockDecorativeStone || state.getBlock() == MillBlocks.byzantineStoneTile || state.getBlock() == MillBlocks.byzantineTile ||
                state.getBlock() == MillBlocks.byzantineTileSlab || state.getBlock() == MillBlocks.byzantineTileSlabDouble;
    }

    /**
     * Determines whether the given block is one of the special positions, and if so adds it to the provided BuildingLocation
     * @param state The block to check
     * @param pos The position of the block
     * @param location The BuildingLocation to add any special locations to.
     */
    private void setReferencePositions(IBlockState state, BlockPos pos, BuildingLocation location) {
        Block block = state.getBlock();

        if (block instanceof BlockMillChest) {
            location.chestPos.add(pos);
        } else if (block == MillBlocks.storedPosition) {
            if (state.getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.TRADEPOS) {
                location.tradePos = (pos);
            } else if (state.getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.SLEEPPOS) {
                location.sleepPos.add(pos);
            } else if (state.getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.SOURCEPOS) {
                location.sourcePos.add(pos);
            } else if (state.getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.HIDEPOS) {
                location.hidePos.add(pos);
            } else if (state.getValue(StoredPosition.VARIANT) == StoredPosition.EnumType.DEFENDPOS) {
                location.defendPos.add(pos);
            }
        } else if (state.getBlock() == Blocks.furnace) {
            location.craftPos.add(pos);
        } else if (state.getBlock() == Blocks.crafting_table) {
            location.craftPos.add(pos);
        } else if (state.getBlock() == Blocks.iron_block) {
            location.craftPos.add(pos);
        } else if (state.getBlock() == Blocks.anvil) {
            location.craftPos.add(pos);
        }
    }

    //////////////////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public static class LocationReturn {
        static final int OUTSIDE_RADIUS = 1;
        static final int LOCATION_CLASH = 2;
        static final int CONSTRUCTION_FORBIDDEN = 3;
        static final int WRONG_ALTITUDE = 4;
        static final int DANGER = 5;
        static final int NOT_REACHABLE = 4;

        BuildingLocation location;
        int errorCode;
        BlockPos errorPos;

        LocationReturn(final BuildingLocation l) {
            location = l;
            errorCode = 0;
            errorPos = null;
        }

        LocationReturn(final int error, final BlockPos pos) {
            location = null;
            errorCode = error;
            errorPos = pos;
        }

    }

    public static class ResourceCost {
        int amount;
        ItemStack stack;
        String odString;

        ResourceCost(ItemStack stackIn, int amountIn) {
            amount = amountIn;
            stack = stackIn;
            odString = null;
        }

        ResourceCost(String nameIn, int amountIn) {
            amount = amountIn;
            stack = null;
            odString = nameIn;
        }

        public ItemStack getStack() {
            return stack;
        }

        public String getOreDictionaryName() {
            return odString;
        }

        public void add(int amountIn) {
            amount += amountIn;
        }

        public int getCost(ItemStack stackIn) {
            if (stackIn.getIsItemStackEqual(stack)) {
                return amount;
            } else {
                List<ItemStack> odStack = OreDictionary.getOres(odString, true);

                if (odStack.isEmpty()) {
                    System.err.println("Error! - Resource computed with unidentifed OreID.");
                    return 0;
                }

                for (ItemStack anOdStack : odStack) {
                    if (stackIn.getIsItemStackEqual(anOdStack)) {
                        return amount;
                    }
                }
            }

            return 0;
        }
    }
}
