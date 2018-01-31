package org.millenaire.building;

import java.util.ArrayList;
import java.util.List;

import org.millenaire.MillConfig;
import org.millenaire.entities.EntityMillVillager;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class BuildingLocation 
{
	public int minx, maxx, minz, maxz, miny, maxy;
	public int minxMargin, maxxMargin, minyMargin, maxyMargin, minzMargin, maxzMargin;
	public int length, height, width;
	
	public EnumFacing orientation;
	public BlockPos position;
	
	public List<BlockPos>chestPos = new ArrayList<BlockPos>();
	public BlockPos tradePos;
	public List<BlockPos>sourcePos = new ArrayList<BlockPos>();
	public List<BlockPos>craftPos = new ArrayList<BlockPos>();
	public List<BlockPos>sleepPos = new ArrayList<BlockPos>();
	public List<BlockPos>hidePos = new ArrayList<BlockPos>();
	public List<BlockPos>defendPos = new ArrayList<BlockPos>();
	
	List<EntityMillVillager>residents = new ArrayList<EntityMillVillager>();
	public List<String> subBuildings = new ArrayList<String>();
	
	public BuildingLocation(BuildingPlan plan, BlockPos pos, EnumFacing orientIn)
	{
		orientation = orientIn;
		position = pos;
		length = plan.length;
		height = plan.height;
		width = plan.width;
		this.computeMargins();
	}
	
	public BuildingLocation(int l, int h, int w, BlockPos pos, EnumFacing orientIn)
	{
		orientation = orientIn;
		position = pos;
		length = l;
		height = h;
		width = w;
		this.computeMargins();
	}
	
	BuildingLocation(ResourceLocation rl, BlockPos pos, EnumFacing orientIn) {
		this(BuildingTypes.getTypeByID(rl).loadBuilding(), pos, orientIn);
	}
	
	public void computeMargins() 
	{
		minx = position.getX();
		miny = position.getY();
		minz = position.getZ();
		
		maxx = position.getX() + width;
		maxy = position.getY() + height;
		maxz = position.getZ() + length;
		
		minxMargin = minx - MillConfig.minBuildingDistance + 1;
		minzMargin = minz - MillConfig.minBuildingDistance + 1;
		minyMargin = miny - 3;
		maxyMargin = maxy + 1;
		maxxMargin = maxx + MillConfig.minBuildingDistance + 1;
		maxzMargin = maxz + MillConfig.minBuildingDistance + 1;
	}
	/*
	public static BuildingLocation fromNBT(NBTTagCompound nbt) {
		ResourceLocation rl = ResourceLocationUtil.getRL(nbt.getString("planID"));
		BlockPos pos = BlockPos.fromLong(nbt.getLong("pos"));
		EnumFacing fac = EnumFacing.getHorizontal(nbt.getInteger("facing"));
		return new BuildingLocation(rl, pos, fac);
	}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setString("planID", ResourceLocationUtil.getString(planid));
		nbt.setInteger("facing", orientation.getHorizontalIndex());
		
		
		return nbt;
	}
	*/
}
