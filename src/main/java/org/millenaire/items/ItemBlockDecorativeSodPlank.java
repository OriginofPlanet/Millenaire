package org.millenaire.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import org.millenaire.blocks.BlockDecorativeSodPlank;

public class ItemBlockDecorativeSodPlank extends ItemBlock {

    public ItemBlockDecorativeSodPlank(Block block) {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    public String getUnlocalizedName(ItemStack stack) {
        return ((BlockDecorativeSodPlank) this.block).getUnlocalizedName(stack.getMetadata());
    }

    public int getMetadata(int damage) {
        return damage;
    }
}
