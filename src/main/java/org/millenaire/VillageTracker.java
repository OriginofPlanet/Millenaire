package org.millenaire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.millenaire.village.Village;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class VillageTracker extends WorldSavedData
{
	private final static String IDENTITY = "Millenaire.VillageInfo";

	private Map<UUID, Village> villages = new HashMap<>();
	private List<BlockPos> VPs = new ArrayList<>();
	
	public VillageTracker() { super(IDENTITY); }
	
	private VillageTracker(String id) { super(id); }

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		System.out.println("Village Tracker reading from NBT");
		for(String s : nbt.getKeySet()) {
			if(nbt.getTag(s) instanceof NBTTagCompound) {
				villages.put(UUID.fromString(s), readVillageFromCompound(nbt.getCompoundTag(s)));
			}
		}
	}

	private Village readVillageFromCompound(NBTTagCompound nbt) {
		Village vil = new Village();
		vil.setPos(BlockPos.fromLong(nbt.getLong("pos")));
		return vil;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) 
	{
		System.out.println("Village Tracker Writing to NBT");
		for(Entry<UUID, Village> e : villages.entrySet()) {
			NBTTagCompound villageTag = new NBTTagCompound();
			
			villageTag.setLong("Pos", e.getValue().getPos().toLong());
			
			nbt.setTag(e.getKey().toString(), villageTag);
		}
	}

	/**
	 * Checks registered Village POSITIONS (not registered villages in case village hasn't been created yet)
	 * to see if it is too close to another village.
	 * 
	 * @param pos Position to check
	 * @param minDist maximum distance to check
	 * @return
	 */
	public boolean isCloseToOtherVillage(BlockPos pos, int minDist) {
		boolean tooClose = false;
		
		for(BlockPos bp : VPs) {
			if(bp.distanceSq(pos) <= minDist*minDist) {
				tooClose = true;
				break;
			}
		}
		
		return tooClose;
	}
	
	public void registerVillage(UUID id, Village vil) { villages.put(id, vil); }
	public void registerVillagePos(BlockPos pos) { VPs.add(pos); }
	
	public void unregisterVillage(UUID id) { villages.remove(id); }
	
	public static VillageTracker get(World world)
	{
		MapStorage storage = world.getPerWorldStorage();
		VillageTracker data = (VillageTracker)storage.loadData(VillageTracker.class, IDENTITY);
		if(data == null)
		{
			data = new VillageTracker(IDENTITY);
			storage.setData(IDENTITY, data);
		}
		
		return data;
	}
}
