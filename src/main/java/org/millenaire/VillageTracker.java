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

    /**
     * Creates or gets the village tracker for a world.
     * @param world The world to get the tracker for
     * @return An instance of the VillageTracker class.
     */
    public static VillageTracker get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        VillageTracker data = (VillageTracker) storage.loadData(VillageTracker.class, IDENTITY);
        if (data == null) {
            data = new VillageTracker(IDENTITY);
            storage.setData(IDENTITY, data);
        }

        return data;
    }

    /**
     * Called by MC on world load - reads in the saved village data.
     * @param nbt The data saved in the world
     */
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

    /**
     * Convenience method that reads a village from NBT and instantiates it.
     * @param nbt The NBT to read the village from.
     * @return The instantiated village object.
     */
    private Village readVillageFromCompound(NBTTagCompound nbt) {
        Village vil = new Village();
        vil.setPos(BlockPos.fromLong(nbt.getLong("pos")));
        vil.setType(MillCulture.findVillageTypeByID(nbt.getString("Type")));
        return vil;
    }

    /**
     * Called by MC to get us to save the village data
     * @param nbt The NBT we must write to.
     */
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
    public boolean isTooCloseToOtherVillage(BlockPos pos, int minDist) {
        boolean tooClose = false;

        for (BlockPos bp : VPs) {
            if (bp.distanceSq(pos) <= minDist * minDist) {
                tooClose = true;
                break;
            }
        }

        return tooClose;
    }

    /**
     * Registers a village so that it will be saved with the world.
     * @param id The {@link UUID Universally Unique Identifier (UUID)} associated with this village.
     * @param vil The {@link Village} itself.
     */
    public void registerVillage(UUID id, Village vil) {
        villages.put(id, vil);
        markDirty();
    }

    /**
     * Registers a village position so that no other villages will be generated too close.
     * @param pos The {@link BlockPos} of the village
     */
    public void registerVillagePos(BlockPos pos) {
        VPs.add(pos);
    }

    /**
     * Removes a village from the village list so that it is no longer saved.
     * @param id The {@link UUID Universally Unique Identifier (UUID)} of the village to remove
     */
    public void unregisterVillage(UUID id) {
        villages.remove(id);
        markDirty();
    }

    /**
     * Removes a {@link VillageTracker#registerVillagePos(BlockPos) village position} from the list so that other villages may once again generate near it
     * @param pos The position to remove
     */
    public void unregisterVillagePos(BlockPos pos) {
        VPs.remove(pos);
    }
}
