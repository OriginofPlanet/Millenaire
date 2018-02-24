package org.millenaire.entities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.millenaire.CommonUtilities;
import org.millenaire.MillCulture;
import org.millenaire.MillCulture.VillageType;
import org.millenaire.VillagerType;
import org.millenaire.blocks.BlockVillageStone;
import org.millenaire.building.Building;
import org.millenaire.building.BuildingLocation;
import org.millenaire.village.Village;
import org.millenaire.village.VillageTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stores data for a village
 */
public class TileEntityVillageStone extends TileEntity {
    //Control Value.  Changed when using wandSummon, if left as 'biome' when onLoad called, culture decided by biome.
    public String culture = "biome";
    /**
     * True if this village should be of a random type, else false.
     */
    public boolean randomVillage = true;
    /**
     * The village this stone is linked to.
     */
    public Village village;
    /**
     * What type of village this stone is linked to
     */
    public VillageType villageType;
    /**
     * The name of the village this stone is linked to, as shown to the player.
     */
    public String villageName;
    /**
     * Whether a village should be generated (true) or already has been (false)
     */
    public boolean villageNotGenerated = true;
    /**
     * Whether this should explode next tick.
     */
    public boolean willExplode = false;
    public int testVar = 0;
    /**
     * All the villagers in the village
     */
    private List<EntityMillVillager> currentVillagers = new ArrayList<>();
    /**
     * The UUID of the village.
     */
    private UUID villageID;
    /**
     * True if we need to read from the village tracker once we get a world. Because minecraft loads us from NBT
     * BEFORE we actually get put into the world.
     */
    private boolean needsReadOnGetWorld = false;
    /**
     * Contains all the buildings loaded from NBT and populated into the village when {@link Village#readDataFromTE()} is called
     */
    public NBTTagList buildings;

    /**
     * Called by MC when this TE is created (i.e. generated) for the first time.
     */
    @Override
    public void onLoad() {
        if (worldObj.isRemote) return;

        //System.out.println("TEVS at " + this.getPos() + " created.");

        if (!villageNotGenerated) return;

        World world = this.getWorld();
        BlockPos pos = this.getPos();
        if (!world.isRemote) { //server only
            if (world.getBlockState(pos).getBlock() instanceof BlockVillageStone) {

                pos = world.getTopSolidOrLiquidBlock(pos); //Get the top block

                if (culture.equalsIgnoreCase("biome")) {
                    if (world.getBiomeGenForCoords(pos) != null) {
                        //Do awesome stuff and set culture.  Below is simply for testing.
                        //System.out.println("Village Culture being set by biome");
                        culture = "norman";
                    }
                }

                try {
                    if (randomVillage)
                        villageType = MillCulture.getCulture(culture).getRandomVillageType();
                    else
                        villageType = MillCulture.getCulture(culture).getVillageTypeByID(villageName);

                    villageName = villageType.getVillageName();

                    //System.out.println("TEVS at " + getPos() + " creating village...");

                    village = Village.createVillage(getPos(), world, villageType, MillCulture.getCulture(culture), villageName);
                    village.setupVillage();

                    markDirty();
                    villageNotGenerated = false;
                } catch (Exception ex) {
                    System.err.println("Something went catastrophically wrong creating this village");
                    ex.printStackTrace();
                }
            } else {
                System.err.println("VillageStone TileEntity loaded wrong");
            }
        }

    }

    /**
     * Creates a villager BUT DOES NOT SPAWN IT IN THE WORLD.
     *
     * @param worldIn    The world to spawn the villager in
     * @param cultureIn  The culture of the villager.
     * @param villagerID The villager's ID. If 0, a random one is created.
     * @return The created villager.
     */
    public EntityMillVillager createVillager(World worldIn, MillCulture cultureIn, int villagerID) {
        VillagerType currentVillagerType;
        int currentGender;

        if (villagerID == 0) {
            int balance = 0;
            villagerID = (int) CommonUtilities.getRandomNonzero(); //TODO: This is not a random integer but a decimal. Fix?
            boolean checkAgain = false;

            for (EntityMillVillager currentVillager : currentVillagers) {
                if (currentVillager.getGender() == 0) {
                    balance++;
                } else {
                    balance--;
                }

                if (villagerID == currentVillager.villagerID) {
                    villagerID = (int) CommonUtilities.getRandomNonzero();
                    checkAgain = true;
                }
            }
            while (checkAgain) {
                checkAgain = false;
                for (EntityMillVillager currentVillager : currentVillagers) {
                    if (villagerID == currentVillager.villagerID) {
                        villagerID = (int) CommonUtilities.getRandomNonzero();
                        checkAgain = true;
                    }
                }
            }

            balance += CommonUtilities.randomizeGender();

            if (balance < 0) {
                currentGender = 0;
                currentVillagerType = cultureIn.getChildType(0);
            } else {
                currentGender = 1;
                currentVillagerType = cultureIn.getChildType(1);
            }

            EntityMillVillager newVillager = new EntityMillVillager(worldIn, villagerID, cultureIn, null);
            newVillager.setTypeAndGender(currentVillagerType, currentGender);

            return newVillager;
        } else {
            for (EntityMillVillager currentVillager : currentVillagers) {
                if (villagerID == currentVillager.villagerID) {
                    return currentVillager;
                }
            }

            System.err.println("Attempted to create nonspecific Villager.");
        }

        return null;
    }

    @Override
    public void setWorldObj(World worldIn) {
        super.setWorldObj(worldIn);
        if (needsReadOnGetWorld) {
            village = VillageTracker.get(worldObj).getVillageByUUID(villageID);
            needsReadOnGetWorld = false;
        }
    }


    /**
     * Called by MC to allow us to read any saved state. Used here to prevent villages being generated where they already
     * existed.
     *
     * @param compound The data saved via {@link TileEntityVillageStone#writeToNBT(NBTTagCompound)}.
     */
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        //System.out.println("TEVS Reading state");
        String uuidRaw;
        if ((uuidRaw = compound.getString("VillageUUID")) != null) {
            villageID = UUID.fromString(uuidRaw);
            villageType = MillCulture.findVillageTypeByID(compound.getString("Type"));
            culture = compound.getString("Culture");
            villageName = compound.getString("Name");
            villageNotGenerated = false;
            needsReadOnGetWorld = true;

            buildings = compound.getTagList("Buildings", Constants.NBT.TAG_COMPOUND);
        }
    }

    /**
     * Called by MC to allow us to save any data. Used to prevent villages spawning again.
     *
     * @param compound The data tag we must write to.
     */
    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        //System.out.println("TEVS Writing State");
        if (village != null) {
            compound.setString("VillageUUID", village.getUUID().toString());
            compound.setString("Type", villageType.id);
            compound.setString("Culture", culture);
            compound.setString("Name", villageName);

            NBTTagList buildings = new NBTTagList();

            for (Map.Entry<BuildingLocation, Building> entry : village.geography.buildingLocations.entrySet()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("ID", entry.getValue().ID);
                tag.setInteger("Level", entry.getValue().level);
                tag.setLong("StartingPos", entry.getKey().position.toLong());
                tag.setString("Orientation", entry.getKey().orientation.getName());
                buildings.appendTag(tag);
            }

            compound.setTag("Buildings", buildings);
        }
    }
}
