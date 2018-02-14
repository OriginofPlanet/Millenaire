package org.millenaire.generation;

import java.util.HashSet;
import java.util.Random;

import org.millenaire.MillConfig;
import org.millenaire.VillageTracker;
import org.millenaire.blocks.MillBlocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;


public class VillageGenerator implements IWorldGenerator {
    // MoonCutter added stuff
    private static final boolean VG_TRACE = true;
    private static final boolean VG_TRACE2 = false;

    // MoonCutter added stuff - probably not the best way to do this
    static public HashSet<Integer> coordsTried = new HashSet();
    static public HashSet<BlockPos> placedVillages = new HashSet<BlockPos>();


    @Override
    public void generate (Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimensionId() == 0) {
            BlockPos pos1 = new BlockPos(chunkX * 16, 0, chunkZ * 16);
            //System.out.println("testing1");
            try {
                generateVillageAt(random, world.getHeight(pos1), world);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Attempt to generate the village
     */
    private boolean generateVillageAt (Random rand, BlockPos pos, World world) {

        BlockPos villageCenter = null;

        if (!(MillConfig.generateVillages || MillConfig.generateLoneBuildings)) {
            return false;
        }

        if (world.isRemote) {
            return false;
        }

        // moved this check here and corrected a bug - Too close to spawn point.
        if (world.getSpawnPoint().distanceSq(pos) < MillConfig.spawnDistance * MillConfig.spawnDistance) {
            return false;
        }

        // Avoid checking same chunk again
        if (coordsTried.contains(pos.getX() + (pos.getZ() << 16))) {
            return false;
        }

        // check that all 11 x 11 chunks are loaded (they seldom are).
        if (areChunksLoaded(world, pos)) {
            villageCenter = pos;
        } else {
            // check surrounding chunks in a 13 x 13 area - this could be optimized I think
            villageCenter = anyAreaCloseByLoaded(world, pos);
            if (villageCenter == null) {
                return false;
            }
        }


        //TODO: Use configured radius?
        int villageRadius = 60;

        BlockPos corner1 = villageCenter.add(villageRadius, 0, 0).subtract(new Vec3i(0, 0, villageRadius));
        BlockPos corner2 = villageCenter.add(villageRadius, 0, villageRadius);
        BlockPos corner3 = villageCenter.subtract(new Vec3i(villageRadius, 0, villageRadius));
        BlockPos corner4 = villageCenter.subtract(new Vec3i(villageRadius, 0, 0)).add(0, 0, villageRadius);

        if (world.getBiomeGenForCoords(corner1).biomeID == BiomeGenBase.ocean.biomeID
                || world.getBiomeGenForCoords(corner2).biomeID == BiomeGenBase.ocean.biomeID
                || world.getBiomeGenForCoords(corner3).biomeID == BiomeGenBase.ocean.biomeID
                || world.getBiomeGenForCoords(corner4).biomeID == BiomeGenBase.ocean.biomeID
                || world.getBiomeGenForCoords(villageCenter).biomeID == BiomeGenBase.ocean.biomeID
                || world.getBiomeGenForCoords(villageCenter).biomeID == BiomeGenBase.river.biomeID) {
            return false; //Water is a big no-no
        }
        int biomeMismatches = 0;

        if (world.getBiomeGenForCoords(villageCenter).biomeID != world.getBiomeGenForCoords(corner1).biomeID) {
            biomeMismatches++;
        }
        if (world.getBiomeGenForCoords(villageCenter).biomeID != world.getBiomeGenForCoords(corner2).biomeID) {
            biomeMismatches++;
        }
        if (world.getBiomeGenForCoords(villageCenter).biomeID != world.getBiomeGenForCoords(corner3).biomeID) {
            biomeMismatches++;
        }
        if (world.getBiomeGenForCoords(villageCenter).biomeID != world.getBiomeGenForCoords(corner4).biomeID) {
            biomeMismatches++;
        }
        if (biomeMismatches > 2) {
            return false; //Biome mismatch
        }

        // this line is for player controlled villages I think
        EntityPlayer generatingPlayer = world.getClosestPlayer(villageCenter.getX(), villageCenter.getY(), villageCenter.getZ(), -1);

        // remember that this chunk has been checked for later
        coordsTried.add(villageCenter.getX() + (villageCenter.getZ() << 16));

        // check if other villages are too close
        // TODO - debug and use this instead of my own
        //if(!VillageTracker.get(world).getNearVillages(villageCenter, MillConfig.minVillageDistance).isEmpty()) {

        if (VillageTracker.get(world).isCloseToOtherVillage(villageCenter, MillConfig.minVillageDistance)) {
            return false;
        } else {
            BlockPos nPos = world.getHeight(villageCenter);
            VillageTracker.get(world).registerVillagePos(nPos);
            world.setBlockState(nPos, MillBlocks.villageStone.getDefaultState());
            return false;
        }
    }

    // Check that all chunks around the tested chunk are loaded
    // used both for the generated chunk and when checking close by
    private boolean areChunksLoaded (World world, BlockPos pos) {
        BlockPos bPos1 = new BlockPos(pos.getX() - 5 * 16, pos.getY(), pos.getZ() - 5 * 16);
        BlockPos bPos2 = new BlockPos(pos.getX() + 5 * 16, pos.getY(), pos.getZ() + 5 * 16);

        return world.isAreaLoaded(bPos1, bPos2);
    }

    // check other areas close by the generated chunk
    // should get a hit closer to the player.
    // TODO check if this can be optimized by only checking some of the chunks.
    private BlockPos anyAreaCloseByLoaded (World world, BlockPos pos) {
        int ix = 0;
        for (int i = -6; i < 7; i++) {
            for (int j = -6; j < 7; j++) {
                final int tx = pos.getX() + i * 16;
                final int tz = pos.getZ() + j * 16;
                if (!coordsTried.contains(tx + (tz << 16))) {
                    BlockPos newPos = new BlockPos(tx, pos.getY(), tz);
                    ix++;
                    if (areChunksLoaded(world, newPos)) {
                        if (VG_TRACE2) System.out.println("Tested areas = " + ix);
                        return newPos;
                    }
                }
            }
        }
        return null;
    }

   /* // TODO this method should be replaced with a call to VillageTracker when it works
    private boolean canAttemptVillageAt(World world, EntityPlayer generatingPlayer, int minDistance, BlockPos villagePos) {

        // should use VillageTracker here I think, not sure how though

        final int minDistanceVillages = Math.min(minDistance, MillConfig.minVillageDistance);
//        final int minDistanceLoneBuildings = Math.min(minDistance, MillConfig.minVillageLoneDistance);

        final int minDistanceVillagesSq = minDistanceVillages * minDistanceVillages;
//        final int minDistanceLoneBuildingsSq = minDistanceLoneBuildings * minDistanceLoneBuildings;

        for (BlockPos vp : ) {
            if (VG_TRACE2) System.out.println("There is another village at " + vp.getX() + ", " + vp.getZ());
            double d = villagePos.distanceSq(vp.getX(), vp.getY(), vp.getZ());
            if (VG_TRACE2) System.out.println("Distance is " + d);
            if (d < minDistanceVillagesSq) {
                return false;
            }
        }
        return true;

    }*/
}