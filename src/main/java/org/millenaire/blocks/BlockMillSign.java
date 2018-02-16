package org.millenaire.blocks;

import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.millenaire.entities.TileEntityMillSign;

import java.util.Random;

public class BlockMillSign extends BlockWallSign {
    BlockMillSign() {
        super();

        this.setBlockUnbreakable();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMillSign();
    }
}
