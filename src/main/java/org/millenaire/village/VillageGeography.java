package org.millenaire.village;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.millenaire.Millenaire;
import org.millenaire.blocks.MillBlocks;
import org.millenaire.building.Building;
import org.millenaire.building.BuildingLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VillageGeography {
    /**
     * The distance from the edge of the village area we take into account.
     */
    private static final int MAP_MARGIN = 10;
    /**
     * The distance from each building taken into account when calculating the changes around a building
     */
    private static final int BUILDING_MARGIN = 5;
    /**
     * The max difference in Y from the center for an area to be buildable.
     */
    private static final int VALID_HEIGHT_DIFF = 10;

    /**
     * The length of this village
     */
    public int length = 0;
    /**
     * The width of this village.
     */
    public int width = 0;
    /**
     * The coordinates where this area starts in terms of the actual world.
     */
    public int mapStartX = 0, mapStartZ = 0;
    /**
     * The position of the top block at the given RELATIVE x and z coords
     */
    public short[][] topGround;
    /**
     * Any coordinates where there is lava
     */
    public boolean[][] danger;
    /**
     * Anywhere that contains forbidden blocks is set to true
     */
    public boolean[][] buildingForbidden;
    /**
     * Anywhere we can build is set to true
     */
    public boolean[][] canBuild;
    /**
     * Anywhere where an existing building is placed is set to true
     */
    public boolean[][] buildingLoc;
    /**
     * Anywhere a building has tried to be placed is set to true.
     */
    public boolean[][] buildTested = null;
    /**
     * Anywhere a path is placed is set to true
     */
    public boolean[][] path;
    /**
     * The location of any placed buildings and what they are
     */
    public LinkedHashMap<BuildingLocation, Building> buildingLocations = new LinkedHashMap<>();
    /**
     * The world this village is in
     */
    public World world;
    /**
     * The base Y-Coordinate of this village.
     */
    private int yBaseline = 0;
    /**
     * The amount of space in a given spot
     */
    public short[][] spaceAbove;
    /**
     * Whether there is water in the given spot
     */
    public boolean[][] water;
    /**
     * Whether there is a tree in the given spot
     */
    private boolean[][] tree;
    /**
     * Anywhere we have attempted to bridge a gap is set to true
     */
    private boolean[][] topAdjusted;
    /**
     * How often we should update chunks
     */
    private int frequency = 10;
    /**
     * The coordinates of the last updated chunk
     */
    private int lastUpdatedX, lastUpdatedZ;

    /**
     * How many update attempts there has been since the last actual update, used in conjunction with {@link VillageGeography#frequency}
     */
    private int updateCounter;

    public VillageGeography() {

    }

    /**
     * Returns true if we shouldn't build here because of this block.
     *
     * @param block The block to check.
     * @return True if this entire column should be forbidden, else false.
     */
    private static boolean isForbiddenBlockForConstruction(final Block block) {
        return block == Blocks.water || block == Blocks.flowing_water || block == Blocks.ice || block == Blocks.flowing_lava || block == Blocks.lava || block == Blocks.planks || block == Blocks.cobblestone || block == Blocks.brick_block || block == Blocks.chest || block == Blocks.glass || block == Blocks.stonebrick || block == Blocks.prismarine
                || block instanceof BlockWall || block instanceof BlockFence || block == MillBlocks.blockDecorativeEarth || block == MillBlocks.blockDecorativeStone || block == MillBlocks.blockDecorativeWood || block == MillBlocks.byzantineTile || block == MillBlocks.byzantineTileSlab || block == MillBlocks.byzantineStoneTile || block == MillBlocks.paperWall || block == MillBlocks.emptySericulture;
    }

    /**
     * Whether this block is considered ground.
     *
     * @param b The block to check.
     * @return True if this is a ground block - i.e. something we should build on.
     */
    public static boolean isBlockIdGround(final Block b) {
        return b == Blocks.bedrock || b == Blocks.clay || b == Blocks.dirt || b == Blocks.stone || b == Blocks.snow ||
                b == Blocks.packed_ice || b == Blocks.grass || b == Blocks.gravel || b == Blocks.obsidian ||
                b == Blocks.sand || b == Blocks.farmland || b == Blocks.mycelium || b == Blocks.sandstone;
    }

    /**
     * Whether this block interferes with paths.
     *
     * @param block The block to check.
     * @return True if an entity cannot move through this block.
     */
    private static boolean isBlockSolid(Block block) {
        return block.getMaterial().blocksMovement();
    }

    /**
     * Constructs a map of this area and populates all the two-dimensional arrays such as {@link VillageGeography#canBuild}
     *
     * @param pstartX The X coord of the start of the area
     * @param pstartZ The Z coord of the start of the area
     * @param endX    The X coord of the end of the area
     * @param endZ    The Z coord of the end of the area
     */
    private void createWorldInfo(final int pstartX, final int pstartZ, final int endX, final int endZ) {
        int chunkStartX = pstartX >> 4;
        int chunkStartZ = pstartZ >> 4;
        mapStartX = chunkStartX << 4;
        mapStartZ = chunkStartZ << 4;

        length = ((endX >> 4) + 1 << 4) - mapStartX;
        width = ((endZ >> 4) + 1 << 4) - mapStartZ;

        frequency = (int) Math.max(1000 * 1.0 / (length * width / 256), 10);

        if (frequency == 0) {
            System.err.println("Null frequency in createWorldInfo.");
        }

        topGround = new short[length][width];
        spaceAbove = new short[length][width];
        danger = new boolean[length][width];
        buildingLoc = new boolean[length][width];
        buildingForbidden = new boolean[length][width];
        canBuild = new boolean[length][width];
        buildTested = new boolean[length][width];
        water = new boolean[length][width];
        tree = new boolean[length][width];
        path = new boolean[length][width];
        topAdjusted = new boolean[length][width];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                buildingLoc[i][j] = false;
                canBuild[i][j] = false;
            }
        }

        for (int i = 0; i < length; i += 16) {
            for (int j = 0; j < width; j += 16) {
                updateChunk(i, j);
            }
        }
        lastUpdatedX = 0;
        lastUpdatedZ = 0;
    }

    /**
     * Registers this building in both {@link VillageGeography#buildingLoc} and {@link VillageGeography#buildingLocations},
     * preventing buildings from being built on top of this one.
     *
     * @param p  Which building is being built here
     * @param bl The location of the building.
     */
    public void registerBuilding(Building p, final BuildingLocation bl) {
        buildingLocations.put(bl, p);

        final int lowerX = Math.max(bl.minXMargin - mapStartX, 0);
        final int lowerZ = Math.max(bl.minZMargin - mapStartZ, 0);
        final int upperX = Math.max(bl.maxXMargin - mapStartX, 0);
        final int upperZ = Math.max(bl.maxZMargin - mapStartZ, 0);

        for (int x = lowerX; x < upperX; x++) {
            for (int z = lowerZ; z < upperZ; z++) {
                buildingLoc[x][z] = true;
            }
        }
    }

    /**
     * Queues an update of any chunks in the given area, either by the radius - i.e. distance from the center, or by
     * ensuring that all the area including and surrounding building locations provided are updated - whichever is larger.
     *
     * @param world     The world to update from
     * @param locations A list of any locations you want to update. Can be null.
     * @param blIP      An individual building location you want to update - just convenience for the above with one location. Can be null.
     * @param center    The center to update from.
     * @param radius    The amount of blocks from the center to update, regardless of buildings.
     * @return True if a new world info was created, or false if the existing one was updated.
     */
    public boolean update(final World world, final List<BuildingLocation> locations, final BuildingLocation blIP, final BlockPos center, final int radius) {
        try {
            this.world = world;
            this.yBaseline = world.getHeight(center).getY();

            int startX = center.getX(), startZ = center.getZ(), endX = center.getX(), endZ = center.getZ();

            if (locations != null) {
                for (final BuildingLocation location : locations) {
                    if (location != null) {
                        if (location.minXMargin < startX) {
                            startX = location.minXMargin;
                        }
                        if (location.maxXMargin > endX) {
                            endX = location.maxXMargin;
                        }
                        if (location.minZMargin < startZ) {
                            startZ = location.minZMargin;
                        }
                        if (location.maxZMargin > endZ) {
                            endZ = location.maxZMargin;
                        }
                    }
                }
            }

            if (blIP != null) {
                if (blIP.minXMargin < startX) {
                    startX = blIP.minXMargin;
                }
                if (blIP.maxXMargin > endX) {
                    endX = blIP.maxXMargin;
                }
                if (blIP.minZMargin < startZ) {
                    startZ = blIP.minZMargin;
                }
                if (blIP.maxZMargin > endZ) {
                    endZ = blIP.maxZMargin;
                }
            }

            startX = Math.min(startX - BUILDING_MARGIN, center.getX() - radius - MAP_MARGIN);
            startZ = Math.min(startZ - BUILDING_MARGIN, center.getZ() - radius - MAP_MARGIN);
            endX = Math.max(endX + BUILDING_MARGIN, center.getX() + radius + MAP_MARGIN);
            endZ = Math.max(endZ + BUILDING_MARGIN, center.getZ() + radius + MAP_MARGIN);

            final int chunkStartXTemp = startX >> 4;
            final int chunkStartZTemp = startZ >> 4;
            final int mapStartXTemp = chunkStartXTemp << 4;
            final int mapStartZTemp = chunkStartZTemp << 4;
            final int lengthTemp = ((endX >> 4) + 1 << 4) - mapStartXTemp;
            final int widthTemp = ((endZ >> 4) + 1 << 4) - mapStartZTemp;

            if (lengthTemp != length || widthTemp != width) {
                createWorldInfo(startX, startZ, endX, endZ);
                return true;
            } else {
                updateNextChunk();
                return false;
            }
        } catch (Exception e) {
            System.out.println("Exception updating geography -> " + e.toString());
            System.out.println(e.getStackTrace()[0]);
            return false;
        }
    }

    /**
     * Updates the info for the chunk at the given BLOCK (NOT CHUNK) coordinates.
     * Updates only the chunk that contains the given block.
     *
     * @param relativeStartX The X position of the BLOCK, NOT OF THE CHUNK, to start updating at.
     * @param relativeStartZ The Z position of the BLOCK, NOT OF THE CHUNK, to start updating at.
     */
    private void updateChunk(final int relativeStartX, final int relativeStartZ) {
        // We have to test not just for this chunk but the surrounding ones also
        // as we need to do some operations that involve
        // neighbouring blocks

        //System.out.println("Updating chunk starting at relative position " + relativeStartX + ", " + relativeStartZ);

        for (int chunkX = -1; chunkX < 2; chunkX++) {
            for (int chunkZ = -1; chunkZ < 2; chunkZ++) {
                if (!world.getChunkProvider().chunkExists((relativeStartX + mapStartX >> 4) + chunkX, (relativeStartZ + mapStartZ >> 4) + chunkZ)) {
                    world.getChunkProvider().provideChunk((relativeStartX + mapStartX >> 4) + chunkX, (relativeStartZ + mapStartZ >> 4) + chunkZ);
                }
            }
        }

        final Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(relativeStartX + mapStartX, yBaseline, relativeStartZ + mapStartZ));

        for (int xInChunk = 0; xInChunk < 16; xInChunk++) {
            for (int zInChunk = 0; zInChunk < 16; zInChunk++) {
                final short maxPermittedYCoord = (short) Math.min(yBaseline + 25, 255);

                final int currentRelativeX = xInChunk + relativeStartX;
                final int currentRelativeZ = zInChunk + relativeStartZ;

                canBuild[currentRelativeX][currentRelativeZ] = false;
                buildingForbidden[currentRelativeX][currentRelativeZ] = false;
                water[currentRelativeX][currentRelativeZ] = false;
                topAdjusted[currentRelativeX][currentRelativeZ] = false;

                Block blockAboveFloor;

                short currentY = (short) (chunk.getHeight(new BlockPos(xInChunk, yBaseline, zInChunk)));

                short topBlockYPos = currentY;

                if (!chunk.canSeeSky(new BlockPos(xInChunk, currentY, zInChunk))) {
                    System.out.println("Block is Blocked");
                }

                Block b = chunk.getBlockState(new BlockPos(xInChunk, topBlockYPos, zInChunk)).getBlock();
                while (b == Blocks.water || b == Blocks.flowing_water || b == Blocks.leaves || b == Blocks.leaves2
                        || b == Blocks.log || b == Blocks.log2 || b == Blocks.air || b == Blocks.vine || b == Blocks.cocoa
                        || b == Blocks.melon_block || b == Blocks.pumpkin || b == Blocks.snow_layer || b == Blocks.tallgrass
                        || b == Blocks.acacia_fence || b == Blocks.birch_fence || b == Blocks.dark_oak_fence
                        || b == Blocks.jungle_fence || b == Blocks.oak_fence || b == Blocks.spruce_fence
                        || b == Blocks.iron_bars || b == Blocks.nether_brick_fence) {
                    topBlockYPos--;
                    b = chunk.getBlockState(new BlockPos(xInChunk, topBlockYPos, zInChunk)).getBlock();
                }

                if (currentY <= maxPermittedYCoord && currentY > 1) {
                    blockAboveFloor = chunk.getBlock(xInChunk, topBlockYPos, zInChunk);
                } else {
                    blockAboveFloor = null;
                }

                boolean onGround = true;

                short lastLiquid = -1;

                //Keep going up from our highest block until it's not solid
                while (blockAboveFloor != null && (isBlockSolid(blockAboveFloor) || blockAboveFloor instanceof BlockLiquid || !onGround)) {
                    if (isForbiddenBlockForConstruction(blockAboveFloor)) {
                        buildingForbidden[currentRelativeX][currentRelativeZ] = true;
                    }

                    if (blockAboveFloor instanceof BlockLiquid) {
                        onGround = false;
                        lastLiquid = currentY;
                    } else if (isBlockSolid(blockAboveFloor)) {
                        onGround = true;
                    }

                    currentY++;

                    if (currentY <= maxPermittedYCoord && currentY > 1) {
                        blockAboveFloor = chunk.getBlock(xInChunk, currentY, zInChunk);
                    } else {
                        blockAboveFloor = null;
                    }
                }

                if (!onGround) {
                    currentY = lastLiquid;
                }

                while (currentY <= maxPermittedYCoord && currentY > 1 && !(!isBlockSolid(chunk.getBlock(xInChunk, currentY, zInChunk)) && !isBlockSolid(chunk.getBlock(xInChunk, currentY + 1, zInChunk)))) {
                    currentY++;
                }

                currentY = (byte) Math.max(1, currentY);

                topGround[currentRelativeX][currentRelativeZ] = topBlockYPos;
                spaceAbove[currentRelativeX][currentRelativeZ] = 0;


                final Block floorBlock = chunk.getBlock(xInChunk, topBlockYPos, zInChunk);
                blockAboveFloor = chunk.getBlock(xInChunk, topBlockYPos + 1, zInChunk);

                water[currentRelativeX][currentRelativeZ] = (blockAboveFloor == Blocks.flowing_water || blockAboveFloor == Blocks.water
                        || floorBlock == Blocks.flowing_water || floorBlock == Blocks.water);

                tree[currentRelativeX][currentRelativeZ] = (blockAboveFloor == Blocks.log);

                path[currentRelativeX][currentRelativeZ] = (floorBlock == MillBlocks.blockMillPath || floorBlock == MillBlocks.blockMillPathSlab || floorBlock == MillBlocks.blockMillPathSlabDouble);

                boolean blocked = false;

                //If we're not on top of a fence or wall (which are just over a block tall)
                //And we're not a full cube, glass, iron bars or similar, and not a liquid, then we're empty space
                if (!(floorBlock instanceof BlockFence) && !(floorBlock instanceof BlockWall) && !isBlockSolid(blockAboveFloor) && blockAboveFloor != Blocks.flowing_water && floorBlock != Blocks.water) {
                    spaceAbove[currentRelativeX][currentRelativeZ] = 1;
                } else {
                    //Otherwise we're blocked - i.e. not walkable
                    blocked = true;
                }

                //If this is lava it's dangerous. Simple enough
                if (blockAboveFloor == Blocks.flowing_lava || blockAboveFloor == Blocks.lava || floorBlock == Blocks.flowing_lava || floorBlock == Blocks.lava) {
                    danger[currentRelativeX][currentRelativeZ] = true;
                } else {
                    danger[currentRelativeX][currentRelativeZ] = false;
                    //If this is a forbidden block, AND the one below it is the SAME block, this is dangerous
                    for (final Block forbiddenBlock : Millenaire.instance.forbiddenBlocks) {
                        danger[currentRelativeX][currentRelativeZ] = (forbiddenBlock == blockAboveFloor);
                        danger[currentRelativeX][currentRelativeZ] = (floorBlock == blockAboveFloor);
                    }
                }

                //If we're not dangerous
                if (!danger[currentRelativeX][currentRelativeZ] && !buildingLoc[currentRelativeX][currentRelativeZ]) {
                    if (topBlockYPos > yBaseline - VALID_HEIGHT_DIFF && topBlockYPos < yBaseline + VALID_HEIGHT_DIFF) {
                        //If the block below the top block is in the height range, we can build here
                        canBuild[currentRelativeX][currentRelativeZ] = true;
                    }
                }

                buildingForbidden[currentRelativeX][currentRelativeZ] = isForbiddenBlockForConstruction(blockAboveFloor);

                //Get the block above the block above the surface.
                currentY++;

                //Keep going up until we're at the max Y
                while (currentY < maxPermittedYCoord && currentY > 0) {
                    blockAboveFloor = chunk.getBlock(xInChunk, currentY, zInChunk);

                    //If we're not blocked, and the current block isn't solid (i.e. it's walkable) then there's another
                    //block's worth of space here.
                    if (!blocked && spaceAbove[currentRelativeX][currentRelativeZ] < 3 && !isBlockSolid(blockAboveFloor)) {
                        //System.out.println("Not solid - we have space");
                        spaceAbove[currentRelativeX][currentRelativeZ]++;
                    } else {
                        blocked = true;
                    }

                    //If any blocks ABOVE the top one are forbidden then this area is forbidden.
                    buildingForbidden[currentRelativeX][currentRelativeZ] = (isForbiddenBlockForConstruction(blockAboveFloor));

                    currentY++;
                }

                //If building isn't forbidden, we can build. Duh.
                canBuild[currentRelativeX][currentRelativeZ] = !(buildingForbidden[currentRelativeX][currentRelativeZ]);
            }
        }

        /*
         * New method: attempt to "bridge" gaps in topground (especially
         * doorways)
         *
         * First, gaps one block large, possibly with difference in level up to
         * 2
         */

        boolean gapFilled = true;

        while (gapFilled) {
            gapFilled = false;
            for (int i = -5; i < 21; i++) {
                for (int j = -5; j < 21; j++) {
                    final int mx = i + relativeStartX;
                    final int mz = j + relativeStartZ;

                    if (mz >= 0 && mz < width) {
                        if (mx > 1 && mx < length - 1) {
                            if (Math.abs(topGround[mx - 1][mz] - topGround[mx + 1][mz]) < 2 && (topGround[mx - 1][mz] + 2 < topGround[mx][mz] || topGround[mx + 1][mz] + 2 < topGround[mx][mz])) {
                                final short ntg = topGround[mx - 1][mz];
                                final boolean samesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean belowsolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg - 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean below2solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg - 2, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean abovesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean above2solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 2, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean above3solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 3, relativeStartZ + mapStartZ + j)).getBlock());

                                // check if same level works
                                if (Math.abs(topGround[mx - 1][mz] - topGround[mx + 1][mz]) < 2 && belowsolid && !samesolid && !abovesolid) {
                                    topGround[mx][mz] = ntg;
                                    if (!above2solid) {
                                        spaceAbove[mx][mz] = 3;
                                    } else {
                                        spaceAbove[mx][mz] = 2;
                                    }
                                    gapFilled = true;
                                    topAdjusted[mx][mz] = true;
                                } else if (topGround[mx + 1][mz] <= topGround[mx - 1][mz] && below2solid && !belowsolid && !samesolid && !abovesolid) {
                                    topGround[mx][mz] = (short) (ntg - 1);
                                    if (!abovesolid) {
                                        spaceAbove[mx][mz] = 3;
                                    } else {
                                        spaceAbove[mx][mz] = 2;
                                    }
                                    gapFilled = true;
                                    topAdjusted[mx][mz] = true;
                                } else if (topGround[mx + 1][mz] >= topGround[mx - 1][mz] && samesolid && !abovesolid && !above2solid) {
                                    topGround[mx][mz] = (short) (ntg + 1);
                                    if (!above3solid) {
                                        spaceAbove[mx][mz] = 3;
                                    } else {
                                        spaceAbove[mx][mz] = 2;
                                    }
                                    gapFilled = true;
                                    topAdjusted[mx][mz] = true;
                                }
                            }
                        }
                    }
                    if (mx >= 0 && mx < length) {
                        if (mz > 1 && mz < width - 1) {
                            if (Math.abs(topGround[mx][mz - 1] - topGround[mx][mz + 1]) < 3 && (topGround[mx][mz - 1] + 2 < topGround[mx][mz] || topGround[mx][mz + 1] + 2 < topGround[mx][mz])) {
                                final short ntg = topGround[mx][mz - 1];
                                final boolean samesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean belowsolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg - 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean below2solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg - 2, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean abovesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean above2solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 2, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean above3solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 3, relativeStartZ + mapStartZ + j)).getBlock());

                                // check if same level works
                                if (Math.abs(topGround[mx][mz - 1] - topGround[mx][mz + 1]) < 2 && belowsolid && !samesolid && !abovesolid) {
                                    topGround[mx][mz] = ntg;
                                    if (!above2solid) {
                                        spaceAbove[mx][mz] = 3;
                                    } else {
                                        spaceAbove[mx][mz] = 2;
                                    }
                                    gapFilled = true;
                                    topAdjusted[mx][mz] = true;
                                } else if (topGround[mx][mz + 1] <= topGround[mx][mz - 1] && below2solid && !belowsolid && !samesolid && !abovesolid) {
                                    topGround[mx][mz] = (short) (ntg - 1);
                                    if (!abovesolid) {
                                        spaceAbove[mx][mz] = 3;
                                    } else {
                                        spaceAbove[mx][mz] = 2;
                                    }
                                    gapFilled = true;
                                    topAdjusted[mx][mz] = true;
                                } else if (topGround[mx][mz + 1] >= topGround[mx][mz - 1] && samesolid && !abovesolid && !above2solid) {
                                    topGround[mx][mz] = (short) (ntg + 1);
                                    if (!above3solid) {
                                        spaceAbove[mx][mz] = 3;
                                    } else {
                                        spaceAbove[mx][mz] = 2;
                                    }
                                    gapFilled = true;
                                    topAdjusted[mx][mz] = true;
                                }
                            }
                        }
                    }
                }
            }

            /*
             * Then, gaps two blocks large, on the same level (for instance,
             * passage between a double-size wall)
             */
            for (int i = -5; i < 21; i++) {
                for (int j = -5; j < 21; j++) {
                    final int mx = i + relativeStartX;
                    final int mz = j + relativeStartZ;

                    if (mz >= 0 && mz < width) {
                        if (mx > 1 && mx < length - 2) {
                            if (topGround[mx - 1][mz] == topGround[mx + 2][mz] && topGround[mx - 1][mz] < topGround[mx][mz] && topGround[mx - 1][mz] < topGround[mx + 1][mz]) {
                                final short ntg = topGround[mx - 1][mz];
                                final boolean samesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean belowsolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg - 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean abovesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean above2solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 2, relativeStartZ + mapStartZ + j)).getBlock());

                                // using the world obj because we might be
                                // beyond the chunk
                                final boolean nextsamesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i + 1, ntg, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean nextbelowsolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i + 1, ntg - 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean nextabovesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i + 1, ntg + 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean nextabove2solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i + 1, ntg + 2, relativeStartZ + mapStartZ + j)).getBlock());

                                // check if same level works
                                if (belowsolid && nextbelowsolid && !samesolid && !nextsamesolid && !abovesolid && !nextabovesolid) {
                                    topGround[mx][mz] = ntg;
                                    topGround[mx + 1][mz] = ntg;
                                    if (!above2solid) {
                                        spaceAbove[mx][mz] = 3;
                                    } else {
                                        spaceAbove[mx][mz] = 2;
                                    }

                                    if (!nextabove2solid) {
                                        spaceAbove[mx + 1][mz] = 3;
                                    } else {
                                        spaceAbove[mx + 1][mz] = 2;
                                    }
                                    gapFilled = true;
                                    topAdjusted[mx][mz] = true;
                                }
                            }
                        }
                    }
                    if (mx >= 0 && mx < length) {
                        if (mz > 1 && mz < width - 2) {
                            if (topGround[mx][mz - 1] == topGround[mx][mz + 2] && topGround[mx][mz - 1] < topGround[mx][mz] && topGround[mx][mz - 1] < topGround[mx][mz + 1]) {
                                final short ntg = topGround[mx][mz - 1];
                                final boolean samesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean belowsolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg - 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean abovesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 1, relativeStartZ + mapStartZ + j)).getBlock());
                                final boolean above2solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 2, relativeStartZ + mapStartZ + j)).getBlock());

                                // using the world obj because we might be
                                // beyond the chunk
                                final boolean nextsamesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg, relativeStartZ + mapStartZ + j + 1)).getBlock());
                                final boolean nextbelowsolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg - 1, relativeStartZ + mapStartZ + j + 1)).getBlock());
                                final boolean nextabovesolid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 1, relativeStartZ + mapStartZ + j + 1)).getBlock());
                                final boolean nextabove2solid = isBlockSolid(world.getBlockState(new BlockPos(relativeStartX + mapStartX + i, ntg + 2, relativeStartZ + mapStartZ + j + 1)).getBlock());

                                // check if same level works
                                if (belowsolid && nextbelowsolid && !samesolid && !nextsamesolid && !abovesolid && !nextabovesolid) {
                                    // MLN.temp(this,
                                    // i+"/"+j+" Hor 2 space: "+topGround[mx][mz]+" to "+ntg);
                                    topGround[mx][mz] = ntg;
                                    topGround[mx][mz + 1] = ntg;
                                    if (!above2solid) {
                                        spaceAbove[mx][mz] = 3;
                                    } else {
                                        spaceAbove[mx][mz] = 2;
                                    }

                                    if (!nextabove2solid) {
                                        spaceAbove[mx][mz + 1] = 3;
                                    } else {
                                        spaceAbove[mx][mz + 1] = 2;
                                    }
                                    gapFilled = true;
                                    topAdjusted[mx][mz] = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int xInChunk = 0; xInChunk < 16; xInChunk++) {
            for (int zInChunk = 0; zInChunk < 16; zInChunk++) {

                final int relativeX = xInChunk + relativeStartX;
                final int relativeZ = zInChunk + relativeStartZ;

                if (danger[relativeX][relativeZ]) {
                    for (int k = -2; k < 3; k++) {
                        for (int l = -2; l < 3; l++) {
                            if (k >= 0 && l >= 0 && k < length && l < width) {
                                spaceAbove[relativeX][relativeZ] = 0;
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Calls updateChunk if the frequency has been met - i.e. only executes some of the time.
     */
    private void updateNextChunk() {
        /*updateCounter = (updateCounter + 1) % frequency;

        if (updateCounter != 0) {
            return;
        }

        lastUpdatedX++;
        if (lastUpdatedX * 16 >= length) {
            lastUpdatedX = 0;
            lastUpdatedZ++;
        }

        if (lastUpdatedZ * 16 >= width) {
            lastUpdatedZ = 0;
        }*/

        final UpdateThread thread = new UpdateThread(Thread.currentThread());
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.x = lastUpdatedX << 4;
        thread.z = lastUpdatedZ << 4;

        thread.run();
    }

    /**
     * Deep clones a two-dimensional short array.
     *
     * @param source The array to clone
     * @return A deep clone of the array.
     */
    public static short[][] shortArrayDeepClone(final short[][] source) {

        final short[][] target = new short[source.length][];

        for (int i = 0; i < source.length; i++) {
            target[i] = source[i].clone();
        }

        return target;
    }

    public void validateAllBuildings() {
        for(Map.Entry<BuildingLocation, Building> entry : buildingLocations.entrySet()) {
            BuildingLocation loc = entry.getKey();
            Building building = entry.getValue();

            boolean error = false;

            IBlockState[][][] blocks = building.blocksInBuilding;
            for (int x = loc.minX; x < loc.maxX; x++) {
                for (int y = loc.minY; y < loc.maxY; y++) {
                    for (int z = loc.minZ; z < loc.maxZ; z++) {
                        int relativeX = x - loc.minX;
                        int relativeZ = z - loc.minZ;
                        int relativeY = y - loc.minY;

                        IBlockState desired = blocks[relativeY][relativeZ][relativeX];
                        IBlockState actual = world.getBlockState(new BlockPos(x, y, z));

                        if(actual.getBlock() == MillBlocks.blockMillPath || actual.getBlock() == MillBlocks.blockMillPathSlab
                                || actual.getBlock() == MillBlocks.blockMillPathSlabDouble) continue; //Paths don't matter

                        if(desired.getBlock() != actual.getBlock() || desired.getBlock().getMetaFromState(desired) != actual.getBlock().getMetaFromState(actual)) {
                            System.out.println("Block mismatch at (" + x + ", " + y + ", " + z + ") - should be "
                            + desired.getBlock() + ":" + desired.getBlock().getMetaFromState(desired) + " but is "
                                    + actual.getBlock() + ":" + actual.getBlock().getMetaFromState(actual));
                            error = true;
                            //TODO: queue a rebuild/repair goal?
                        }
                    }
                }
            }

            if(!error)
                System.out.println("Building \"" + building.nativeName + "\" verified with no errors");
            else
                System.out.println("Building \"" + building.nativeName + "\" contains errors.");
        }
    }

    //////////////////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Background thread that runs the chunk updates.
     */
    public class UpdateThread extends Thread {
        int x;
        int z;
        Thread t;

        public UpdateThread(Thread thread) {
            t = thread;
        }

        @Override
        public void run() {
            for (int i = 0; i < length; i += 16) {
                for (int j = 0; j < width; j += 16) {
                    updateChunk(i, j);
                }
            }
            //updateChunk(x, z);
            //t.notify();
        }
    }
}
