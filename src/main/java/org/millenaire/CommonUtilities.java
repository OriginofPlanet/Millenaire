package org.millenaire;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.millenaire.gui.MillAchievement;
import org.millenaire.items.MillItems;

import java.util.Random;

public class CommonUtilities {
    public static Random random = new Random();

    /**
     * Sorts out a player's money, converting any stacks of smaller coins into one of the next denomination.
     *
     * @param playerIn The player to organize money for.
     */
    public static void changeMoney(EntityPlayer playerIn) {
        ItemStack denier = new ItemStack(MillItems.denier, 0, 0);
        ItemStack argent = new ItemStack(MillItems.denierArgent, 0, 0);
        ItemStack or = new ItemStack(MillItems.denierOr, 0, 0);

        for (int i = 0; i < playerIn.inventory.getSizeInventory(); i++) {
            ItemStack stack = playerIn.inventory.getStackInSlot(i);
            if (stack != null) {
                if (stack.getItem() == MillItems.denier) {
                    denier.stackSize = denier.stackSize + stack.stackSize;
                    playerIn.inventory.removeStackFromSlot(i);
                }
                if (stack.getItem() == MillItems.denierArgent) {
                    argent.stackSize = argent.stackSize + stack.stackSize;
                    playerIn.inventory.removeStackFromSlot(i);
                }
                if (stack.getItem() == MillItems.denierOr) {
                    or.stackSize = or.stackSize + stack.stackSize;
                    playerIn.inventory.removeStackFromSlot(i);
                }
            }
        }

        argent.stackSize = argent.stackSize + (denier.stackSize / 64);
        denier.stackSize = denier.stackSize % 64;

        or.stackSize = or.stackSize + (argent.stackSize / 64);
        if (or.stackSize >= 1) {
            playerIn.addStat(MillAchievement.cresus, 1);
        }

        argent.stackSize = argent.stackSize % 64;

        playerIn.inventory.addItemStackToInventory(denier);
        playerIn.inventory.addItemStackToInventory(argent);

        while (or.stackSize > 64) {
            playerIn.inventory.addItemStackToInventory(new ItemStack(MillItems.denierOr, 64, 0));
            or.stackSize = or.stackSize - 64;
        }

        playerIn.inventory.addItemStackToInventory(or);
    }

    /**
     * Gets a random number.
     *
     * @return A random decimal between 0.1 and 1.1
     */
    public static float getRandomNonzero() {
        return random.nextFloat() + 0.1f;
    }

    /**
     * Gets a random Villager Gender
     *
     * @return A random number from -2 to 0
     */
    public static int randomizeGender() {
        return random.nextInt(3) - 2;
    }

    /**
     * Gets the appropriate block to make surfaces out of
     *
     * @param b       The block to base the check on
     * @param surface if the ground is on the top of the ground (true) or underground (false)
     * @return The surface block
     */
    public static Block getValidGroundBlock(final Block b, final boolean surface) {
        if (b == Blocks.bedrock || b == Blocks.dirt ||
                b == Blocks.grass) {
            return Blocks.dirt;
        } else if (b == Blocks.stone) {
            if (surface) {
                return Blocks.dirt;
            } else {
                return Blocks.grass;
            }
        } else if (b == Blocks.gravel) {
            return Blocks.gravel;
        } else if (b == Blocks.sand) {
            return Blocks.sand;
        } else if (b == Blocks.sandstone) {
            if (surface) {
                return Blocks.sand;
            } else {
                return Blocks.sandstone;
            }
        }

        return null;
    }

    /**
     * Convenience method, same as {@link CommonUtilities#adjustForOrientation(int, int, int, int, int, EnumFacing)} but
     * with a BlockPos instead of individual coordinates.
     *
     * @param pos         The base position to add to.
     * @param xoffset     The amount to increase the x coordinate by (may be negative)
     * @param zoffset     The amount to increase the z coordinate by (may be negative)
     * @param orientation The direction this object is facing.
     * @return A BlockPos containing the adjusted coordinates.
     */
    public static BlockPos adjustForOrientation(final BlockPos pos, final int xoffset, final int zoffset, final EnumFacing orientation) {
        return adjustForOrientation(pos.getX(), pos.getY(), pos.getZ(), xoffset, zoffset, orientation);
    }

    /**
     * Adds the specified x and z offsets to the given x and z coordinates, based on which way the object is facing.
     *
     * @param x           The X Pos
     * @param y           The Y Pos
     * @param z           The Z Pos
     * @param xoffset     The amount to increase the x coordinate by (may be negative)
     * @param zoffset     The amount to increase the z coordinate by (may be negative)
     * @param orientation The direction this object is facing.
     * @return A BlockPos containing the adjusted coordinates.
     */
    public static BlockPos adjustForOrientation(final int x, final int y, final int z, final int xoffset, final int zoffset, final EnumFacing orientation) {

        //West is negative x
        //North is negative z
        //East is positive x
        //South is positive z

        //We add the coordinates so that they extend in the direction of `orientation` and the direction 90 degrees clockwise (i.e. to the right)

        BlockPos pos = new BlockPos(x, y, z);
        if (orientation == EnumFacing.SOUTH) {
            pos = new BlockPos(x - xoffset, y, z + zoffset);
        } else if (orientation == EnumFacing.WEST) {
            pos = new BlockPos(x - xoffset, y, z - zoffset);
        } else if (orientation == EnumFacing.NORTH) {
            pos = new BlockPos(x + xoffset, y, z - zoffset);
        } else if (orientation == EnumFacing.EAST) {
            pos = new BlockPos(x + xoffset, y, z + zoffset);
        }

        return pos;
    }
}