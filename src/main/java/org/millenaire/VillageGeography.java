package org.millenaire;

import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.millenaire.blocks.MillBlocks;
import org.millenaire.building.BuildingLocation;
import org.millenaire.building.BuildingPlan;

import java.util.LinkedHashMap;
import java.util.List;

public class VillageGeography {
    private static final int MAP_MARGIN = 10;
    private static final int BUILDING_MARGIN = 5;
    private static final int VALIDHEIGHTDIFF = 10;

    public int length = 0;
    public int width = 0;
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
    public LinkedHashMap<BuildingLocation, BuildingPlan> buildingLocations = new LinkedHashMap<>();
    public World world;
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
    private int frequency = 10;
    private int lastUpdatedX, lastUpdatedZ;

    private int updateCounter;

    public VillageGeography() {

    }

    private static boolean isForbiddenBlockForConstruction(final Block block) {
        return block == Blocks.water || block == Blocks.flowing_water || block == Blocks.ice || block == Blocks.flowing_lava || block == Blocks.lava || block == Blocks.planks || block == Blocks.cobblestone || block == Blocks.brick_block || block == Blocks.chest || block == Blocks.glass || block == Blocks.stonebrick || block == Blocks.prismarine
                || block instanceof BlockWall || block instanceof BlockFence || block == MillBlocks.blockDecorativeEarth || block == MillBlocks.blockDecorativeStone || block == MillBlocks.blockDecorativeWood || block == MillBlocks.byzantineTile || block == MillBlocks.byzantineTileSlab || block == MillBlocks.byzantineStoneTile || block == MillBlocks.paperWall || block == MillBlocks.emptySericulture;
    }

    private static boolean isBlockIdGround(final Block b) {
        return b == Blocks.bedrock || b == Blocks.clay || b == Blocks.dirt || b == Blocks.stone || b == Blocks.snow ||
                b == Blocks.packed_ice || b == Blocks.grass || b == Blocks.gravel || b == Blocks.obsidian ||
                b == Blocks.sand || b == Blocks.farmland || b == Blocks.mycelium;
    }

    private static boolean isBlockSolid(Block block) {
        return block.getMaterial().blocksMovement() || block == Blocks.glass || block == Blocks.glass_pane || block instanceof BlockSlab || block instanceof BlockStairs || block instanceof BlockFence || block instanceof BlockWall || block == MillBlocks.paperWall;
    }

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

    public void registerBuilding(BuildingPlan p, final BuildingLocation bl) {
        buildingLocations.put(bl, p);

        final int lowerX = Math.max(bl.minxMargin - mapStartX, 0);
        final int lowerZ = Math.max(bl.minzMargin - mapStartZ, 0);
        final int upperX = Math.min(bl.maxxMargin - mapStartX, length + 1);
        final int upperZ = Math.min(bl.maxzMargin - mapStartZ, width + 1);

        for (int x = lowerX; x < upperX; x++) {
            for (int z = lowerZ; z < upperZ; z++) {
                buildingLoc[x][z] = true;
            }
        }
    }

    public boolean update(final World world, final List<BuildingLocation> locations, final BuildingLocation blIP, final BlockPos center, final int radius) {
        this.world = world;
        this.yBaseline = center.getY();

        int startX = center.getX(), startZ = center.getZ(), endX = center.getX(), endZ = center.getZ();

        for (final BuildingLocation location : locations) {
            if (location != null) {
                if (location.position.getX() - location.length / 2 < startX) {
                    startX = location.position.getX() - location.length / 2;
                }
                if (location.position.getX() + location.length / 2 > endX) {
                    endX = location.position.getX() + location.length / 2;
                }
                if (location.position.getZ() - location.width / 2 < startZ) {
                    startZ = location.position.getZ() - location.width / 2;
                }
                if (location.position.getZ() + location.width / 2 > endZ) {
                    endZ = location.position.getZ() + location.width / 2;
                }
            }
        }

        if (blIP != null) {
            if (blIP.position.getX() - blIP.length / 2 < startX) {
                startX = blIP.position.getX() - blIP.length / 2;
            }
            if (blIP.position.getX() + blIP.length / 2 > endX) {
                endX = blIP.position.getX() + blIP.length / 2;
            }
            if (blIP.position.getZ() - blIP.width / 2 < startZ) {
                startZ = blIP.position.getZ() - blIP.width / 2;
            }
            if (blIP.position.getZ() + blIP.width / 2 > endZ) {
                endZ = blIP.position.getZ() + blIP.width / 2;
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
    }

    private void updateChunk(final int startX, final int startZ) {
        // We have to test not just for this chunk but the surrounding ones also
        // as we need to do some operations that involve
        // neighbouring blocks
        for (int chunkX = -1; chunkX < 2; chunkX++) {
            for (int chunkZ = -1; chunkZ < 2; chunkZ++) {
                if (!world.getChunkProvider().chunkExists((startX + mapStartX >> 4) + chunkX, (startZ + mapStartZ >> 4) + chunkZ)) {
                    world.getChunkProvider().provideChunk((startX + mapStartX >> 4) + chunkX, (startZ + mapStartZ >> 4) + chunkZ);
                }
            }
        }

        final Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(startX + mapStartX, yBaseline, startZ + mapStartZ));

        for (int xInChunk = 0; xInChunk < 16; xInChunk++) {
            for (int zInChunk = 0; zInChunk < 16; zInChunk++) {
                final short minPermittedYCoord = (short) Math.max(yBaseline - 25, 1);
                final short maxPermittedYCoord = (short) Math.min(yBaseline + 25, 255);

                final int currentRelativeX = xInChunk + startX;
                final int currentRelativeZ = zInChunk + startZ;

                canBuild[currentRelativeX][currentRelativeZ] = false;
                buildingForbidden[currentRelativeX][currentRelativeZ] = false;
                water[currentRelativeX][currentRelativeZ] = false;
                topAdjusted[currentRelativeX][currentRelativeZ] = false;

                Block block;

                short currentY = (short) (chunk.getHeight(new BlockPos(xInChunk, yBaseline, zInChunk)));

                short topBlockYPos = currentY;

                if (!chunk.canSeeSky(new BlockPos(xInChunk, currentY, zInChunk))) {
                    System.out.println("Block is Blocked");
                }

                while(!isBlockIdGround(world.getBlockState(new BlockPos(xInChunk, topBlockYPos, zInChunk)).getBlock())) {
                    topBlockYPos--;
                }

                if (currentY <= maxPermittedYCoord && currentY > 1) {
                    block = chunk.getBlock(xInChunk, topBlockYPos, zInChunk);
                } else {
                    block = null;
                }

                boolean onGround = true;

                short lastLiquid = -1;

                //Keep going up from our highest block until it's not solid
                while (block != null && (isBlockSolid(block) || block instanceof BlockLiquid || !onGround)) {
                    if (isForbiddenBlockForConstruction(block)) {
                        buildingForbidden[currentRelativeX][currentRelativeZ] = true;
                    }

                    if (block instanceof BlockLiquid) {
                        onGround = false;
                        lastLiquid = currentY;
                    } else if (isBlockSolid(block)) {
                        onGround = true;
                    }

                    currentY++;

                    if (currentY <= maxPermittedYCoord && currentY > 1) {
                        block = chunk.getBlock(xInChunk, currentY, zInChunk);
                    } else {
                        block = null;
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

                final Block blockBelowTop = chunk.getBlock(xInChunk, topBlockYPos - 1, zInChunk);
                block = chunk.getBlock(xInChunk, topBlockYPos, zInChunk);

                water[currentRelativeX][currentRelativeZ] = (block == Blocks.flowing_water || block == Blocks.water);

                tree[currentRelativeX][currentRelativeZ] = (blockBelowTop == Blocks.log);

                path[currentRelativeX][currentRelativeZ] = (blockBelowTop == MillBlocks.blockMillPath || blockBelowTop == MillBlocks.blockMillPathSlab || blockBelowTop == MillBlocks.blockMillPathSlabDouble);

                boolean blocked = false;

                //If we're not on top of a fence or wall (which are just over a block tall)
                //And we're not a full cube, glass, iron bars or similar, and not a liquid, then we're empty space
                if (!(blockBelowTop instanceof BlockFence) && !(blockBelowTop instanceof BlockWall) && !isBlockSolid(block) && block != Blocks.flowing_water && blockBelowTop != Blocks.water) {
                    spaceAbove[currentRelativeX][currentRelativeZ] = 1;
                } else {
                    //Otherwise we're blocked - i.e. not walkable
                    blocked = true;
                }

                //If this is lava it's dangerous. Simple enough
                if (block == Blocks.flowing_lava || block == Blocks.lava) {
                    danger[currentRelativeX][currentRelativeZ] = true;
                } else {
                    danger[currentRelativeX][currentRelativeZ] = false;
                    //If this is a forbidden block, AND the one below it is the SAME forbidden block, this is dangerous
                    for (final Block forbiddenBlock : Millenaire.instance.forbiddenBlocks) {
                        danger[currentRelativeX][currentRelativeZ] = (forbiddenBlock == block);
                        danger[currentRelativeX][currentRelativeZ] = (blockBelowTop == block);
                    }
                }

                //If we're not dangerous
                if (!danger[currentRelativeX][currentRelativeZ] && !buildingLoc[currentRelativeX][currentRelativeZ]) {
                    if (topBlockYPos - 1 > yBaseline - VALIDHEIGHTDIFF && topBlockYPos - 1 < yBaseline + VALIDHEIGHTDIFF) {
                        //If the block below the top block is in the height range, we can build here
                        canBuild[currentRelativeX][currentRelativeZ] = true;
                    }
                }

                buildingForbidden[currentRelativeX][currentRelativeZ] = isForbiddenBlockForConstruction(block);

                //Get the block above the top block, which should in theory be air, no?
                currentY++;

                //Keep going up until we're at the max Y
                while (currentY < maxPermittedYCoord && currentY > 0) {
                    block = chunk.getBlock(xInChunk, currentY, zInChunk);

                    //If we're not blocked, and the current block isn't solid (i.e. it's walkable) then there's another
                    //block's worth of space here.
                    if (!blocked && spaceAbove[currentRelativeX][currentRelativeZ] < 3 && !isBlockSolid(block)) {
                        //System.out.println("Not solid - we have space");
                        spaceAbove[currentRelativeX][currentRelativeZ]++;
                    } else {
                        blocked = true;
                    }

                    //If any blocks ABOVE the top one are forbidden then this area is forbidden.
                    buildingForbidden[currentRelativeX][currentRelativeZ] = (isForbiddenBlockForConstruction(block));

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
                    final int mx = i + startX;
                    final int mz = j + startZ;

                    if (mz >= 0 && mz < width) {
                        if (mx > 1 && mx < length - 1) {
                            if (Math.abs(topGround[mx - 1][mz] - topGround[mx + 1][mz]) < 2 && (topGround[mx - 1][mz] + 2 < topGround[mx][mz] || topGround[mx + 1][mz] + 2 < topGround[mx][mz])) {
                                final short ntg = topGround[mx - 1][mz];
                                final boolean samesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg, startZ + mapStartZ + j)).getBlock());
                                final boolean belowsolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg - 1, startZ + mapStartZ + j)).getBlock());
                                final boolean below2solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg - 2, startZ + mapStartZ + j)).getBlock());
                                final boolean abovesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 1, startZ + mapStartZ + j)).getBlock());
                                final boolean above2solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 2, startZ + mapStartZ + j)).getBlock());
                                final boolean above3solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 3, startZ + mapStartZ + j)).getBlock());

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
                                final boolean samesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg, startZ + mapStartZ + j)).getBlock());
                                final boolean belowsolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg - 1, startZ + mapStartZ + j)).getBlock());
                                final boolean below2solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg - 2, startZ + mapStartZ + j)).getBlock());
                                final boolean abovesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 1, startZ + mapStartZ + j)).getBlock());
                                final boolean above2solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 2, startZ + mapStartZ + j)).getBlock());
                                final boolean above3solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 3, startZ + mapStartZ + j)).getBlock());

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
                    final int mx = i + startX;
                    final int mz = j + startZ;

                    if (mz >= 0 && mz < width) {
                        if (mx > 1 && mx < length - 2) {
                            if (topGround[mx - 1][mz] == topGround[mx + 2][mz] && topGround[mx - 1][mz] < topGround[mx][mz] && topGround[mx - 1][mz] < topGround[mx + 1][mz]) {
                                final short ntg = topGround[mx - 1][mz];
                                final boolean samesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg, startZ + mapStartZ + j)).getBlock());
                                final boolean belowsolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg - 1, startZ + mapStartZ + j)).getBlock());
                                final boolean abovesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 1, startZ + mapStartZ + j)).getBlock());
                                final boolean above2solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 2, startZ + mapStartZ + j)).getBlock());

                                // using the world obj because we might be
                                // beyond the chunk
                                final boolean nextsamesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i + 1, ntg, startZ + mapStartZ + j)).getBlock());
                                final boolean nextbelowsolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i + 1, ntg - 1, startZ + mapStartZ + j)).getBlock());
                                final boolean nextabovesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i + 1, ntg + 1, startZ + mapStartZ + j)).getBlock());
                                final boolean nextabove2solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i + 1, ntg + 2, startZ + mapStartZ + j)).getBlock());

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
                                final boolean samesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg, startZ + mapStartZ + j)).getBlock());
                                final boolean belowsolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg - 1, startZ + mapStartZ + j)).getBlock());
                                final boolean abovesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 1, startZ + mapStartZ + j)).getBlock());
                                final boolean above2solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 2, startZ + mapStartZ + j)).getBlock());

                                // using the world obj because we might be
                                // beyond the chunk
                                final boolean nextsamesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg, startZ + mapStartZ + j + 1)).getBlock());
                                final boolean nextbelowsolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg - 1, startZ + mapStartZ + j + 1)).getBlock());
                                final boolean nextabovesolid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 1, startZ + mapStartZ + j + 1)).getBlock());
                                final boolean nextabove2solid = isBlockSolid(world.getBlockState(new BlockPos(startX + mapStartX + i, ntg + 2, startZ + mapStartZ + j + 1)).getBlock());

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

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {

                final int mx = i + startX;
                final int mz = j + startZ;

                if (danger[mx][mz]) {
                    for (int k = -2; k < 3; k++) {
                        for (int l = -2; l < 3; l++) {
                            if (k >= 0 && l >= 0 && k < length && l < width) {
                                spaceAbove[mx][mz] = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateNextChunk() {
        updateCounter = (updateCounter + 1) % frequency;

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
        }

        final UpdateThread thread = new UpdateThread(Thread.currentThread());
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.x = lastUpdatedX << 4;
        thread.z = lastUpdatedZ << 4;

        thread.start();
        try {
            thread.wait(); //Wait for our chunk updater to finish.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static short[][] shortArrayDeepClone(final short[][] source) {

        final short[][] target = new short[source.length][];

        for (int i = 0; i < source.length; i++) {
            target[i] = source[i].clone();
        }

        return target;
    }

    //////////////////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public class UpdateThread extends Thread {
        int x;
        int z;
        Thread t;

        public UpdateThread(Thread thread) {
            t = thread;
        }

        @Override
        public void run() {
            updateChunk(x, z);
            t.notify();
        }
    }
}
