package org.millenaire;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import org.millenaire.village.Village;

import java.util.*;
import java.util.Map.Entry;

public class VillageTracker extends WorldSavedData {
    private final static String IDENTITY = "Millenaire.VillageInfo";

    private Map<UUID, Village> villages = new HashMap<>();
    private List<BlockPos> VPs = new ArrayList<>();

    public VillageTracker() {
        super(IDENTITY);
    }

    //THIS MUST BE PUBLIC FOR THE DATA TO BE LOADED
    //SERIOUSLY, DO NOT MAKE THIS PRIVATE, PACKAGE-PRIVATE, OR ANYTHING BUT PUBLIC. BAD THINGS ENSUE
    @SuppressWarnings("WeakerAccess")
    public VillageTracker(String id) {
        super(id);
    }

    public static VillageTracker get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        VillageTracker data = (VillageTracker) storage.loadData(VillageTracker.class, IDENTITY);
        if (data == null) {
            data = new VillageTracker(IDENTITY);
            storage.setData(IDENTITY, data);
        }

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        System.out.println("Village Tracker reading from NBT");
        for (String s : nbt.getKeySet()) {
            if (nbt.getTag(s) instanceof NBTTagCompound) {
                UUID u = UUID.fromString(s);
                villages.put(u, readVillageFromCompound(nbt.getCompoundTag(s)));
                villages.get(u).setUUID(u);
            }
        }
    }

    private Village readVillageFromCompound(NBTTagCompound nbt) {
        Village vil = new Village();
        vil.setPos(BlockPos.fromLong(nbt.getLong("pos")));
        vil.setType(MillCulture.getTypeFromName(nbt.getString("Type")));
        return vil;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        System.out.println("Village Tracker Writing to NBT");
        for (Entry<UUID, Village> e : villages.entrySet()) {
            NBTTagCompound villageTag = new NBTTagCompound();

            villageTag.setLong("Pos", e.getValue().getPos().toLong());
            villageTag.setString("Type", e.getValue().getType().id);

            nbt.setTag(e.getKey().toString(), villageTag);
        }
    }

    /**
     * Checks registered Village POSITIONS (not registered villages in case village hasn't been created yet)
     * to see if it is too close to another village.
     *
     * @param pos     Position to check
     * @param minDist maximum distance to check
     * @return
     */
    public boolean isCloseToOtherVillage(BlockPos pos, int minDist) {
        boolean tooClose = false;

        for (BlockPos bp : VPs) {
            if (bp.distanceSq(pos) <= minDist * minDist) {
                tooClose = true;
                break;
            }
        }

        return tooClose;
    }

    public void registerVillage(UUID id, Village vil) {
        markDirty();
        villages.put(id, vil);
    }

    public void registerVillagePos(BlockPos pos) {
        VPs.add(pos);
    }

    public void unregisterVillage(UUID id) {
        markDirty();
        villages.remove(id);
    }

    public void unregisterVillagePos(BlockPos pos) {
        VPs.remove(pos);
    }
}
