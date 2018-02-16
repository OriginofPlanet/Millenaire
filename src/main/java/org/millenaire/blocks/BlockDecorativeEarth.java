package org.millenaire.blocks;

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

import java.util.List;

public class BlockDecorativeEarth extends Block {
    public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockDecorativeEarth.EnumType.class);

    BlockDecorativeEarth() {
        super(Material.ground);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return ((BlockDecorativeEarth.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    public IProperty getVariantProperty() {
        return VARIANT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        if (Block.getBlockFromItem(itemIn) == this) {
            BlockDecorativeEarth.EnumType[] aenumtype = BlockDecorativeEarth.EnumType.values();

            for (EnumType enumtype : aenumtype) {
                list.add(new ItemStack(itemIn, 1, enumtype.getMetadata()));
            }
        }
    }

    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName() + "." + BlockDecorativeEarth.EnumType.byMetadata(meta).getUnlocalizedName();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, BlockDecorativeEarth.EnumType.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return ((BlockDecorativeEarth.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, VARIANT);
    }

    //////////////////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public static enum EnumType implements IStringSerializable {
        DIRTWALL(0, "dirtWall"),
        DRIEDBRICK(1, "driedBrick");

        private static final BlockDecorativeEarth.EnumType[] META_LOOKUP = new BlockDecorativeEarth.EnumType[values().length];

        static {
            BlockDecorativeEarth.EnumType[] var0 = values();

            for (EnumType var3 : var0) {
                META_LOOKUP[var3.getMetadata()] = var3;
            }
        }

        private final int meta;
        private final String name;

        EnumType(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public static BlockDecorativeEarth.EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public int getMetadata() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public String getName() {
            return this.name;
        }

        public String getUnlocalizedName() {
            return this.name;
        }
    }
}
