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
import org.millenaire.MillConfig;
import org.millenaire.MillCulture;
import org.millenaire.village.VillageGeography;
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
     * The ID for the project of this building
     */
    public String ID;
    /**
     * What level building this is
     */
    public int level;
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
    public IBlockState[][][] blocksInBuilding;

    public Building(MillCulture cultureIn, int level, String ID) {
        this.ID = ID;
        this.level = level;
        computeCost();
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
        ID = parent.ID;
        this.level = level;

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
     * @param maxRadius        When multiplied by {@link Building#maxDistance}, gives the max radius from the center pos.
     * @param random           A random
     * @param orientation      Which way this building is or should be facing.
     * @return A {@link BuildingLocation} if a valid placement was found, or null if one wasn't.
     */
    public BuildingLocation findBuildingLocation(VillageGeography geo, MillPathNavigate pathing, BlockPos center, int maxRadius, Random random, EnumFacing orientation) {
        final int relativeCenterX = center.getX() - geo.mapStartX;
        final int relativeCenterZ = center.getZ() - geo.mapStartZ;

        int radius = (int) (maxRadius * minDistance);
        maxRadius = (int) (maxRadius * maxDistance);

        for (int x = 0; x < geo.length; x++) {
            for (int z = 0; z < geo.width; z++) {
                geo.buildTested[x][z] = false;
            }
        }

        while (radius < maxRadius) {
            final int minxX = Math.max(0, relativeCenterX - radius);
            final int maxX = Math.min(geo.length - 1, relativeCenterX + radius);
            final int minZ = Math.max(0, relativeCenterZ - radius);
            final int maxZ = Math.min(geo.width - 1, relativeCenterZ + radius);

            //noinspection Duplicates
            for (int x = minxX; x < maxX; x++) {
                if (relativeCenterZ - radius == minZ) {
                    final LocationReturn lr = testLocation(geo, center, x, minZ, orientation, pathing);

                    if (lr.location != null)
                        return lr.location;
                }
                if (relativeCenterZ + radius == maxZ) {
                    final LocationReturn lr = testLocation(geo, center, x, minZ, orientation, pathing);

                    if (lr.location != null) {
                        return lr.location;
                    }
                }
            }

            //noinspection Duplicates
            for (int j = minZ; j < maxZ; j++) {
                if (relativeCenterX - radius == minxX) {
                    final LocationReturn lr = testLocation(geo, center, j, minZ, orientation, pathing);

                    if (lr.location != null) {
                        return lr.location;
                    }
                }
                if (relativeCenterX + radius == maxX) {
                    final LocationReturn lr = testLocation(geo, center, j, minZ, orientation, pathing);

                    if (lr.location != null) {
                        return lr.location;
                    }
                }
            }

            radius++;
        }

        //System.out.println("building search unsuccessful");
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
     *
     * @param geography The village geography, used to check terrain.
     * @param center    The center of the village
     * @param cornerX   The X coord of one corner to check
     * @param cornerZ   The Z coord of one corner to check
     * @param facing    Which way this building is facing.
     * @param pathing   A {@link MillPathNavigate} instance, used to check that the given location is reachable.
     * @return A LocationReturn with a location if a valid placement if found, or one with an error code and the coordinates of the block that caused the error otherwise.
     */
    private LocationReturn testLocation(VillageGeography geography, BlockPos center, int cornerX, int cornerZ, EnumFacing facing, MillPathNavigate pathing) {
        EnumFacing orientation;

        pathing.setSearchRange(128);

        final int cornerRelativeX = cornerX + geography.mapStartX - center.getX();
        final int cornerRelativeZ = cornerZ + geography.mapStartZ - center.getZ();

        geography.buildTested[cornerX][cornerZ] = true;

        if (facing == null || facing.getIndex() < 2) {
            if (cornerRelativeX * cornerRelativeX > cornerRelativeZ * cornerRelativeZ) {
                if (cornerRelativeX > 0) {
                    orientation = EnumFacing.NORTH;
                } else {
                    orientation = EnumFacing.SOUTH;
                }
            } else {
                if (cornerRelativeZ > 0) {
                    orientation = EnumFacing.EAST;
                } else {
                    orientation = EnumFacing.WEST;
                }
            }
        } else {
            orientation = facing;
        }

        orientation = EnumFacing.getFront((orientation.getHorizontalIndex() + buildingOrientation.getHorizontalIndex()) % 4);

        BlockPos oppositeCorner = CommonUtilities.adjustForOrientation(cornerX, 0, cornerZ, width, length, orientation);

        int minX = cornerX;
        int minZ = cornerZ;
        int maxX = oppositeCorner.getX();
        int maxZ = oppositeCorner.getZ();

        if(minX > maxX) {
            int temp = maxX;
            maxX = minX;
            minX = temp;
        }

        if(minZ > maxZ) {
            int temp = maxZ;
            maxZ = minZ;
            minZ = temp;
        }

        minX -= MillConfig.minBuildingDistance;
        minZ -= MillConfig.minBuildingDistance;
        maxX += MillConfig.minBuildingDistance;
        maxZ += MillConfig.minBuildingDistance;

        boolean reachable = false;

        for (int xPos = minX; xPos <= maxX; xPos++) {
            for (int zPos = minZ; zPos <= maxZ; zPos++) {

                if (xPos < 0 || zPos < 0 || xPos >= geography.length || zPos >= geography.width) {
                    BlockPos p = new BlockPos(xPos + geography.mapStartX, 64, zPos + geography.mapStartZ);

                    return new LocationReturn(LocationReturn.OUTSIDE_RADIUS, p);
                }

                if (geography.buildingLoc[xPos][zPos]) {
                    final BlockPos p = new BlockPos(xPos + geography.mapStartX, 64, zPos + geography.mapStartZ);

                    return new LocationReturn(LocationReturn.LOCATION_CLASH, p);
                } else if (geography.buildingForbidden[xPos][zPos]) {
                    final BlockPos p = new BlockPos(xPos + geography.mapStartX, 64, zPos + geography.mapStartZ);

                    return new LocationReturn(LocationReturn.CONSTRUCTION_FORBIDDEN, p);
                } else if (geography.danger[xPos][zPos]) {
                    final BlockPos p = new BlockPos(xPos + geography.mapStartX, 64, zPos + geography.mapStartZ);

                    return new LocationReturn(LocationReturn.DANGER, p);
                } else if (!geography.canBuild[xPos][zPos]) {
                    final BlockPos p = new BlockPos(xPos + geography.mapStartX, 64, zPos + geography.mapStartZ);

                    return new LocationReturn(LocationReturn.WRONG_ALTITUDE, p);
                } else if (geography.water[xPos][zPos]) {
                    final BlockPos p = new BlockPos(xPos + geography.mapStartX, 64, zPos + geography.mapStartZ);

                    return new LocationReturn(LocationReturn.WATER, p);
                }

                reachable = !pathing.tryMoveToXYZ(xPos, geography.world.getHeight(new BlockPos(xPos, 10, zPos)).getY(), zPos, 0.5D);
            }
        }

        if (!reachable) {
            return new LocationReturn(LocationReturn.NOT_REACHABLE, center);
        }


        //Adjust for trees + plants
        World world = geography.world;

        final int altitude = world.getHeight(new BlockPos(cornerX + geography.mapStartX, 10, cornerZ + geography.mapStartZ)).getY();

        BlockPos highestY = new BlockPos(cornerX + geography.mapStartX, altitude, cornerZ + geography.mapStartZ);

        //If the highest block is a tree or plant we want to be below it.
        Block b = world.getBlockState(highestY).getBlock();
        while (b == Blocks.leaves || b == Blocks.leaves2 || b == Blocks.log || b == Blocks.log2 || b == Blocks.vine
                || b == Blocks.brown_mushroom_block || b == Blocks.red_mushroom_block || b == Blocks.tallgrass
                || b == Blocks.double_plant || b == Blocks.red_flower || b == Blocks.red_flower || b == Blocks.reeds
                || !b.isNormalCube()) {
            highestY = highestY.subtract(new Vec3i(0, 1, 0));
            b = world.getBlockState(highestY).getBlock();

            //Used for e.g. the gap under leaves when not at the trunk
            while (b == Blocks.air) {
                highestY = highestY.subtract(new Vec3i(0, 1, 0));
                b = world.getBlockState(highestY).getBlock();
            }
        }

        //If the highest block (or the one above it, i.e. we are at the bottom of an ocean) is a fluid we want to be above it.
        Block b2 = world.getBlockState(highestY.up()).getBlock();
        if (b2 == Blocks.water || b == Blocks.flowing_water || b == Blocks.lava || b == Blocks.flowing_lava) {
            b = b2;
            while (b == Blocks.water || b == Blocks.flowing_water || b == Blocks.lava || b == Blocks.flowing_lava) {
                highestY = highestY.up();
                b = world.getBlockState(highestY).getBlock();
            }
        }

        final BuildingLocation l = new BuildingLocation(this, highestY, orientation);

        return new LocationReturn(l);
    }

    /**
     * Returns an array of all special points and blocks in this building.
     *
     * @param worldIn  The world to be reading from
     * @param location The location of the building
     * @return An array of {@link BuildingBlock BuildingBlocks}
     */
    public BuildingBlock[] getBuildingPoints(World worldIn, BuildingLocation location) {
        final int x = location.position.getX();
        final int y = location.position.getY();
        final int z = location.position.getZ();
        List<BuildingBlock> buildingBlocks = new ArrayList<>();

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
                            buildingBlocks.add(new BuildingBlock(null, p, BuildingBlock.CLEARGROUND));
                        } else {
                            final BlockPos p = CommonUtilities.adjustForOrientation(x, y + i, z, j - lengthOffset, k - widthOffset, orientation);
                            buildingBlocks.add(new BuildingBlock(null, p, BuildingBlock.CLEARTREE));
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
                            buildingBlocks.add(new BuildingBlock(null, p, BuildingBlock.PRESERVEGROUNDDEPTH));
                        } else if (-i == offset) {
                            final BlockPos p = CommonUtilities.adjustForOrientation(x, y + i, z, j - lengthOffset, k - widthOffset, orientation);
                            buildingBlocks.add(new BuildingBlock(null, p, BuildingBlock.PRESERVEGROUNDSURFACE));
                        } else {
                            final BlockPos p = CommonUtilities.adjustForOrientation(x, y + i, z, j - lengthOffset, k - widthOffset, orientation);
                            buildingBlocks.add(new BuildingBlock(null, p, BuildingBlock.CLEARTREE));
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
                        buildingBlocks.add(new BuildingBlock(state, p));
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

                        buildingBlocks.add(new BuildingBlock(state, p));
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
                        buildingBlocks.add(new BuildingBlock(state, p));
                    }
                }
            }
        }

        final HashMap<BlockPos, BuildingBlock> buildingBlockMap = new HashMap<>();

        final boolean[] toDelete = new boolean[buildingBlocks.size()];

        for (int i = 0; i < buildingBlocks.size(); i++) {
            final BuildingBlock bb = buildingBlocks.get(i);
            Block block;
            IBlockState state;
            int special;

            if (buildingBlockMap.containsKey(bb.position)) {
                block = buildingBlockMap.get(bb.position).blockState != null ? buildingBlockMap.get(bb.position).blockState.getBlock() : null;
                state = buildingBlockMap.get(bb.position).blockState;
                special = buildingBlockMap.get(bb.position).specialBlock;
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
                buildingBlockMap.put(bb.position, bb);
                toDelete[i] = false;
            }
        }

        for (int i = toDelete.length - 1; i >= 0; i--) {
            if (toDelete[i]) {
                buildingBlocks.remove(i);
            }
        }

        BuildingBlock[] abblocks = new BuildingBlock[buildingBlocks.size()];

        for (int i = 0; i < buildingBlocks.size(); i++) {
            abblocks[i] = buildingBlocks.get(i);
        }

        return abblocks;
    }

    /**
     * Returns true if this is a valid block.
     *
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
     *
     * @param state    The block to check
     * @param pos      The position of the block
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
        static final int NOT_REACHABLE = 6;
        static final int WATER = 7;

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
