package org.millenaire.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;
import org.millenaire.blocks.BlockDecorativeOrientedSlabDouble;
import org.millenaire.blocks.BlockDecorativeOrientedSlabHalf;

public class ItemOrientedSlab extends ItemSlab {

    public ItemOrientedSlab(Block block, BlockDecorativeOrientedSlabHalf singleSlab, BlockDecorativeOrientedSlabDouble doubleSlab) {
        super(block, singleSlab, doubleSlab);

        this.setHasSubtypes(false);
    }

    @Override
    public int getMetadata(int damage) {
        return 0;
    }
}
