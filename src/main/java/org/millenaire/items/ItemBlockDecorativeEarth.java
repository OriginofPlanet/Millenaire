package org.millenaire.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.millenaire.blocks.BlockDecorativeEarth;

public class ItemBlockDecorativeEarth extends ItemBlock {

    public ItemBlockDecorativeEarth(Block block) {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public String getUnlocalizedName(ItemStack stack) {
        return ((BlockDecorativeEarth) this.block).getUnlocalizedName(stack.getMetadata());
    }

    public int getMetadata(int damage) {
        return damage;
    }
}
