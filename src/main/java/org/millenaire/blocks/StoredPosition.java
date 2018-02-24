package org.millenaire.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class StoredPosition extends Block {
    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class);
    private final int sourceColor = 44820;
    private final int tradeColor = 9983;
    private final int pathColor = 16766976;
    private final int sleepColor = 57538;
    private final int defendColor = 16711680;
    private final int hideColor = 8323127;
    private boolean showParticles = false;

    StoredPosition() {
        super(Material.vine); //For passthrough
        this.disableStats();
        this.translucent = true;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue() {
        return 1.0F;
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (showParticles) {
            int color = 16777215;

            if (state.getValue(VARIANT) == EnumType.TRADEPOS)
                color = tradeColor;
            else if (state.getValue(VARIANT) == EnumType.SOURCEPOS)
                color = sourceColor;
            else if (state.getValue(VARIANT) == EnumType.PATHPOS)
                color = pathColor;
            else if (state.getValue(VARIANT) == EnumType.SLEEPPOS)
                color = sleepColor;
            else if (state.getValue(VARIANT) == EnumType.DEFENDPOS)
                color = defendColor;
            else if (state.getValue(VARIANT) == EnumType.HIDEPOS)
                color = hideColor;

            double d0 = (double) (color >> 16 & 255) / 255.0D;
            double d1 = (double) (color >> 8 & 255) / 255.0D;
            double d2 = (double) (color & 255) / 255.0D;
            worldIn.spawnParticle(EnumParticleTypes.SPELL_MOB, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, d0, d1, d2);
        }
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
        return showParticles;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        return null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        if (showParticles)
            return new AxisAlignedBB((double) pos.getX() + this.minX, (double) pos.getY() + this.minY, (double) pos.getZ() + this.minZ, (double) pos.getX() + this.maxX, (double) pos.getY() + this.maxY, (double) pos.getZ() + this.maxZ);
        else
            return null;
    }

    public boolean getShowParticles() {
        return showParticles;
    }

    public void setShowParticles(boolean bool) {
        showParticles = bool;
    }

    public IProperty getVariantProperty() {
        return VARIANT;
    }
	
	/*@Override
	@SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
    {
        if (Block.getBlockFromItem(itemIn) == this)
        {
        	StoredPosition.EnumType[] aenumtype = StoredPosition.EnumType.values();
            int i = aenumtype.length;

            for (int j = 0; j < i; ++j)
            {
            	StoredPosition.EnumType enumtype = aenumtype[j];
                list.add(new ItemStack(itemIn, 1, enumtype.getMetadata()));
            }
        }
    }*/

    public String getUnlocalizedName(int meta) {
        return super.getUnlocalizedName() + "." + EnumType.byMetadata(meta).getUnlocalizedName();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, VARIANT);
    }

    //////////////////////////////////////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public enum EnumType implements IStringSerializable {
        TRADEPOS(0, "tradePos"),
        SOURCEPOS(1, "sourcePos"),
        PATHPOS(2, "pathPos"),
        HIDEPOS(3, "hidePos"),
        DEFENDPOS(4, "defendPos"),
        SLEEPPOS(5, "sleepPos");

        private static final EnumType[] META_LOOKUP = new EnumType[values().length];

        static {
            EnumType[] var0 = values();

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

        public static EnumType byMetadata(int meta) {
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