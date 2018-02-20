package org.millenaire.building;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import org.millenaire.CommonUtilities;
import org.millenaire.items.MillItems;

/**
 * Class that stores either a block, or an action to be performed at this location, used (presumably) when villagers are
 * building
 */
public class BuildingBlock {
    public static final byte OAKSPAWN = 1;
    public static final byte SPRUCESPAWN = 2;
    public static final byte BIRCHSPAWN = 3;
    public static final byte JUNGLESPAWN = 4;
    public static final byte ACACIASPAWN = 5;
    public static final byte PRESERVEGROUNDDEPTH = 6;
    public static final byte PRESERVEGROUNDSURFACE = 7;
    public static final byte CLEARTREE = 8;
    public static final byte CLEARGROUND = 9;
    public static final byte SPAWNERSKELETON = 10;
    public static final byte SPAWNERZOMBIE = 11;
    public static final byte SPAWNERSPIDER = 12;
    public static final byte SPAWNERCAVESPIDER = 13;
    public static final byte SPAWNERCREEPER = 14;
    public static final byte SPAWNERBLAZE = 15;
    public static final byte DISPENDERUNKNOWNPOWDER = 16;
    public static final byte TAPESTRY = 17;
    public static final byte BYZANTINEICONSMALL = 18;
    public static final byte BYZANTINEICONMEDIUM = 19;
    public static final byte BYZANTINEICONLARGE = 20;
    public static final byte INDIANSTATUE = 21;
    public static final byte MAYANSTATUE = 22;

    public IBlockState blockState;
    public BlockPos position;
    public byte specialBlock;

    BuildingBlock(IBlockState state, BlockPos pos, byte special) {
        blockState = state;
        position = pos;
        specialBlock = special;
    }

    BuildingBlock(IBlockState state, BlockPos pos) {
        blockState = state;
        position = pos;
        specialBlock = 0;
    }

    /**
     * Performs this action in the given world.
     * @param worldIn The world to perform in
     * @param onGeneration Whether we are in world generation.
     */
    public void build(World worldIn, boolean onGeneration) {
        if (specialBlock != PRESERVEGROUNDDEPTH && specialBlock != PRESERVEGROUNDSURFACE && specialBlock != CLEARTREE) {
            if (blockState != null) {
                worldIn.setBlockState(position, blockState);
                String soundName = blockState.getBlock().stepSound.getPlaceSound();
                worldIn.playSoundEffect(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, soundName, 0.3F, 0.6F);
            }
        }

        if (specialBlock == PRESERVEGROUNDDEPTH || specialBlock == PRESERVEGROUNDSURFACE) {
            Block block = worldIn.getBlockState(position).getBlock();

            final boolean surface = specialBlock == PRESERVEGROUNDSURFACE;

            final Block validGroundBlock = CommonUtilities.getValidGroundBlock(block, surface);

            if (validGroundBlock == null) {
                BlockPos below = position.down();
                Block targetblock = null;
                while (targetblock == null && below.getY() > 0) {
                    block = worldIn.getBlockState(below).getBlock();
                    if (CommonUtilities.getValidGroundBlock(block, surface) != null)
                        targetblock = CommonUtilities.getValidGroundBlock(block, surface);
                    below = below.down();
                }

                if (targetblock == Blocks.dirt && onGeneration) {
                    targetblock = Blocks.grass;
                } else if (targetblock == Blocks.grass && !onGeneration) {
                    targetblock = Blocks.dirt;
                }

                if (targetblock == Blocks.air) {
                    if (onGeneration)
                        targetblock = Blocks.grass;
                    else
                        targetblock = Blocks.dirt;
                }

                assert targetblock != null;
                worldIn.setBlockState(position, targetblock.getDefaultState());
                String soundName = targetblock.stepSound.getPlaceSound();
                worldIn.playSoundEffect(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, soundName, 0.3F, 0.6F);
            } else if (onGeneration && validGroundBlock == Blocks.dirt && worldIn.getBlockState(position.up()) == null) {
                worldIn.setBlockState(position, Blocks.grass.getDefaultState());
                String soundName = Blocks.grass.stepSound.getPlaceSound();
                worldIn.playSoundEffect(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, soundName, 0.3F, 0.6F);
            } else if (validGroundBlock != block && !(validGroundBlock == Blocks.dirt && block == Blocks.grass)) {
                worldIn.setBlockState(position, validGroundBlock.getDefaultState());
                String soundName = validGroundBlock.stepSound.getPlaceSound();
                worldIn.playSoundEffect(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, soundName, 0.3F, 0.6F);
            }
        } else if (specialBlock == CLEARTREE) {
            Block block = worldIn.getBlockState(position).getBlock();

            if (block == Blocks.log || block == Blocks.leaves) {
                worldIn.setBlockToAir(position);
                String soundName = block.stepSound.getBreakSound();
                worldIn.playSoundEffect(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, soundName, 0.3F, 0.6F);

                final Block blockBelow = worldIn.getBlockState(position.down()).getBlock();

                final Block targetBlock = CommonUtilities.getValidGroundBlock(blockBelow, true);

                if (onGeneration && targetBlock == Blocks.dirt) {
                    worldIn.setBlockState(position.down(), Blocks.grass.getDefaultState());
                } else if (targetBlock != null) {
                    worldIn.setBlockState(position.down(), targetBlock.getDefaultState());
                }
            }

        } else if (specialBlock == CLEARGROUND) {
            Block block = worldIn.getBlockState(position).getBlock();

            worldIn.setBlockToAir(position);
            String soundName = block.stepSound.getBreakSound();
            worldIn.playSoundEffect(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D, soundName, 0.3F, 0.6F);

            final Block blockBelow = worldIn.getBlockState(position.down()).getBlock();

            final Block targetBlock = CommonUtilities.getValidGroundBlock(blockBelow, true);

            if (onGeneration && targetBlock == Blocks.dirt) {
                worldIn.setBlockState(position.down(), Blocks.grass.getDefaultState());
            } else if (targetBlock != null) {
                worldIn.setBlockState(position.down(), targetBlock.getDefaultState());
            }
        } else if (specialBlock == OAKSPAWN) {
            if (onGeneration) {
                WorldGenerator wg = new WorldGenTrees(false);
                wg.generate(worldIn, CommonUtilities.random, position);
            } else {
                WorldGenerator wg = new WorldGenTrees(true, 4 + CommonUtilities.random.nextInt(7), Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK), Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK), false);
                wg.generate(worldIn, CommonUtilities.random, position);
            }
        } else if (specialBlock == SPRUCESPAWN) {
            if (onGeneration) {
                WorldGenerator wg = new WorldGenTaiga2(false);
                wg.generate(worldIn, CommonUtilities.random, position);
            } else {
                WorldGenerator wg = new WorldGenTaiga2(true);
                wg.generate(worldIn, CommonUtilities.random, position);
            }
        } else if (specialBlock == BIRCHSPAWN) {
            if (onGeneration) {
                WorldGenerator wg = new WorldGenForest(true, false);
                wg.generate(worldIn, CommonUtilities.random, position);
            } else {
                WorldGenerator wg = new WorldGenForest(false, true);
                wg.generate(worldIn, CommonUtilities.random, position);
            }
        } else if (specialBlock == JUNGLESPAWN) {
            if (onGeneration) {
                WorldGenerator wg = new WorldGenTrees(false, 4 + CommonUtilities.random.nextInt(7), Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE), Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE), true);
                wg.generate(worldIn, CommonUtilities.random, position);
            } else {
                WorldGenerator wg = new WorldGenTrees(true, 4 + CommonUtilities.random.nextInt(7), Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE), Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE), true);
                wg.generate(worldIn, CommonUtilities.random, position);
            }
        } else if (specialBlock == ACACIASPAWN) {
            if (onGeneration) {
                WorldGenerator wg = new WorldGenSavannaTree(false);
                wg.generate(worldIn, CommonUtilities.random, position);
            } else {
                WorldGenerator wg = new WorldGenSavannaTree(true);
                wg.generate(worldIn, CommonUtilities.random, position);
            }
        } else if (specialBlock == SPAWNERSKELETON) {
            worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState());
            final TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) worldIn.getTileEntity(position);
            tileentitymobspawner.getSpawnerBaseLogic().setEntityName("Skeleton");
        } else if (specialBlock == SPAWNERZOMBIE) {
            worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState());
            final TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) worldIn.getTileEntity(position);
            tileentitymobspawner.getSpawnerBaseLogic().setEntityName("Zombie");
        } else if (specialBlock == SPAWNERSPIDER) {
            worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState());
            final TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) worldIn.getTileEntity(position);
            tileentitymobspawner.getSpawnerBaseLogic().setEntityName("Spider");
        } else if (specialBlock == SPAWNERCAVESPIDER) {
            worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState());
            final TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) worldIn.getTileEntity(position);
            tileentitymobspawner.getSpawnerBaseLogic().setEntityName("CaveSpider");
        } else if (specialBlock == SPAWNERCREEPER) {
            worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState());
            final TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) worldIn.getTileEntity(position);
            tileentitymobspawner.getSpawnerBaseLogic().setEntityName("Creeper");
        } else if (specialBlock == SPAWNERBLAZE) {
            worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState());
            final TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) worldIn.getTileEntity(position);
            tileentitymobspawner.getSpawnerBaseLogic().setEntityName("Blaze");
        } else if (specialBlock == DISPENDERUNKNOWNPOWDER) {
            worldIn.setBlockState(position, Blocks.dispenser.getDefaultState());
            final TileEntityDispenser dispenser = (TileEntityDispenser) worldIn.getTileEntity(position);
            dispenser.addItemStack(new ItemStack(MillItems.unknownPowder, 2));
        }
    }
}
