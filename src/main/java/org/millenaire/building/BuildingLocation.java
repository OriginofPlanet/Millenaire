package org.millenaire.building;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.millenaire.CommonUtilities;
import org.millenaire.MillConfig;
import org.millenaire.entities.EntityMillVillager;

import java.util.ArrayList;
import java.util.List;

/**
 * The location of a Building, including its starting and ending X, Y, and Z coords, the same coords including the margin between buildings,
 * the length, height, and width of the building, the chosen orientation and starting position, as well as any special positions, sub-buildings
 * and villagers contained within.
 */
public class BuildingLocation {
    /**
     * Contains the specified block coordinates
     */
    public int minx, maxx, minz, maxz, miny, maxy;
    /**
     * Contains the coordinates including the required bulding-to-building space.
     */
    public int minxMargin, maxxMargin, minyMargin, maxyMargin, minzMargin, maxzMargin;
    /**
     * How far above the ground this building is (can be negative if in the ground)
     */
    private int depth;
    /**
     * The dimensions of the building.
     */
    public int length, height, width;

    /**
     * Which way the building is facing.
     */
    public EnumFacing orientation;
    /**
     * The position of one corner of this building
     */
    public BlockPos position;

    /**
     * The position of all chests
     */
    public List<BlockPos> chestPos = new ArrayList<>();
    /**
     * The position of the trade spot, if there is one.
     */
    public BlockPos tradePos;
    /**
     * The position of resources that can be infinitely mined, such as in quarries.
     */
    public List<BlockPos> sourcePos = new ArrayList<>();
    /**
     * The positions of any crafting stations
     */
    public List<BlockPos> craftPos = new ArrayList<>();
    /**
     * The positions where the villagers will sleep.
     */
    public List<BlockPos> sleepPos = new ArrayList<>();
    /**
     * The positions where villagers will hide
     */
    public List<BlockPos> hidePos = new ArrayList<>();
    /**
     * The position where villagers will defend from
     */
    public List<BlockPos> defendPos = new ArrayList<>();
    /**
     * The names of any sub buildings.
     */
    public List<String> subBuildings = new ArrayList<>();
    /**
     * All the villagers in this building.
     */
    List<EntityMillVillager> residents = new ArrayList<>();

    /**
     * Creates a location, using the values from the specified building for length, width, height, and depth, and the given
     * start position and orientation.
     *
     * @param building The building this location is for.
     * @param pos The starting position to build from
     * @param orientIn The direction to build in.
     */
    public BuildingLocation(Building building, BlockPos pos, EnumFacing orientIn) {
        orientation = orientIn;
        position = pos;
        length = building.length;
        height = building.height;
        width = building.width;
        depth = building.depth;
        this.computeMargins();
    }

    /**
     * Creates a location manually from the given length, width, height, start position and orientation. Depth defaults
     * to 0.
     *
     * @param l The length of the building.
     * @param h The height of the building
     * @param w The width of the building
     * @param pos The position to start from
     * @param orientIn The direction to build in.
     */
    public BuildingLocation(int l, int h, int w, BlockPos pos, EnumFacing orientIn) {
        orientation = orientIn;
        position = pos;
        length = l;
        height = h;
        width = w;
        depth = 0;
        this.computeMargins();
    }

    /**
     * Loads the building specified by the given resource location, and uses its dimensions and the given start pos
     * and orientation.
     *
     * @param rl The location of a Building Plan file.
     * @param pos The position to build from
     * @param orientIn The direction to build in.
     */
    BuildingLocation(ResourceLocation rl, BlockPos pos, EnumFacing orientIn) {
        this(BuildingTypes.getTypeByID(rl).loadBuilding(), pos, orientIn);
    }

    /**
     * Generates the x, y, and z coordinates for this location based on the dimensions, start pos, depth, and orientation.
     */
    private void computeMargins() {
        minx = position.getX();
        miny = position.getY() + depth;
        minz = position.getZ();

        BlockPos p = CommonUtilities.adjustForOrientation(position, width, length, orientation);

        maxx = p.getX();
        maxy = miny + height;
        maxz = p.getZ();

        minxMargin = minx - MillConfig.minBuildingDistance;
        minzMargin = minz - MillConfig.minBuildingDistance;
        minyMargin = miny - 3;
        maxyMargin = maxy + 1;
        maxxMargin = maxx + MillConfig.minBuildingDistance;
        maxzMargin = maxz + MillConfig.minBuildingDistance;
    }


	/*
	public static BuildingLocation fromNBT(NBTTagCompound nbt) {
		ResourceLocation rl = ResourceLocationUtil.getRL(nbt.getOreDictionaryName("planID"));
		BlockPos pos = BlockPos.fromLong(nbt.getLong("pos"));
		EnumFacing fac = EnumFacing.getHorizontal(nbt.getInteger("facing"));
		return new BuildingLocation(rl, pos, fac);
	}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setString("planID", ResourceLocationUtil.getOreDictionaryName(planid));
		nbt.setInteger("facing", orientation.getHorizontalIndex());
		
		
		return nbt;
	}
	*/
}
