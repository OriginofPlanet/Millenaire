package org.millenaire.blocks;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMillPath extends Block {
    public static final PropertyEnum VARIANT = PropertyEnum.create("variant", EnumType.class);

    BlockMillPath () {
        super(Material.ground);

        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.9375F, 1.0F);
    }

    @Override
    public boolean isFullCube () { return false; }

    @Override
    public boolean isOpaqueCube () { return false; }

    @Override
    public int damageDropped (IBlockState state) {
        return ((EnumType) state.getValue(VARIANT)).getMetadata();
    }

    public IProperty getVariantProperty () { return VARIANT; }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks (Item itemIn, CreativeTabs tab, List list) {
        if (Block.getBlockFromItem(itemIn) == this) {
            EnumType[] aenumtype = EnumType.values();

            for (EnumType enumtype : aenumtype) {
                list.add(new ItemStack(itemIn, 1, enumtype.getMetadata()));
            }
        }
    }

    public String getUnlocalizedName (int meta) {
        return super.getUnlocalizedName() + "." + EnumType.byMetadata(meta).getUnlocalizedName();
    }

    @Override
    public IBlockState getStateFromMeta (int meta) {
        return this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
    }

    @Override
    public int getMetaFromState (IBlockState state) {
        return ((EnumType) state.getValue(VARIANT)).getMetadata();
    }

    @Override
    protected BlockState createBlockState () { return new BlockState(this, VARIANT); }

    //////////////////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public static enum EnumType implements IStringSerializable {
        DIRT(0, "dirt"),
        GRAVEL(1, "gravel"),
        SLAB(2, "slab"),
        SANDSTONESLAB(3, "sandstoneSlab"),
        OCHRESLAB(4, "ochreSlab"),
        SLABANDGRAVEL(5, "slabAndGravel");

        private static final EnumType[] META_LOOKUP = new EnumType[values().length];
        private final int meta;
        private final String name;

        EnumType (int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata () { return this.meta; }

        public String toString () { return this.name; }

        public static EnumType byMetadata (int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName () { return this.name; }

        public String getUnlocalizedName () {
            return this.name;
        }

        static {
            EnumType[] var0 = values();

            for (EnumType var3 : var0) {
                META_LOOKUP[var3.getMetadata()] = var3;
            }
        }
    }
}
