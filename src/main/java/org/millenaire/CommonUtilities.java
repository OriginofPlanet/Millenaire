package org.millenaire;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.millenaire.gui.MillAchievement;
import org.millenaire.items.MillItems;

import java.util.Random;

public class CommonUtilities {
    public static Random random = new Random();

    /**
     * pretty much orgainizes the player's money
     *
     * @param playerIn The player to orgainize
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
     * yep
     *
     * @return A random non-zero integer
     */
    public static float getRandomNonzero() {
        return random.nextFloat() + 0.1f;
    }

    /**
     * gets a random Millager Gender
     *
     * @return
     */
    public static int randomizeGender() {
        return random.nextInt(3) - 2;
    }

    /**
     * yep
     *
     * @param b       the block to check
     * @param surface if the ground is on the top of the ground (true) or underground (false)
     * @return
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
}