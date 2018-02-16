package org.millenaire.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSlab;
import org.millenaire.blocks.BlockMillPathSlabDouble;
import org.millenaire.blocks.BlockMillPathSlabHalf;

public class ItemMillPathSlab extends ItemSlab {

    public ItemMillPathSlab(Block block, BlockMillPathSlabHalf singleSlab, BlockMillPathSlabDouble doubleSlab) {
        super(block, singleSlab, doubleSlab);
    }
}
