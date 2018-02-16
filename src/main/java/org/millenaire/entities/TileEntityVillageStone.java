package org.millenaire.entities;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.millenaire.CommonUtilities;
import org.millenaire.MillCulture;
import org.millenaire.MillCulture.VillageType;
import org.millenaire.VillagerType;
import org.millenaire.blocks.BlockVillageStone;
import org.millenaire.village.Village;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileEntityVillageStone extends TileEntity {
    //Control Value.  Changed when using wandSummon, if left as 'biome' when onLoad called, culture decided by biome.
    public String culture = "biome";
    public boolean randomVillage = true;
    public Village village;
    public VillageType villageType;
    public String villageName;
    public boolean shouldGenVillage = true;
    public boolean willExplode = false;
    public int testVar = 0;
    private List<EntityMillVillager> currentVillagers = new ArrayList<>();
    private UUID villageID;

    @Override
    public void onLoad() {
        if(worldObj.isRemote) return;

        System.out.println("TEVS created");

        if(!shouldGenVillage) return;

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
                        villageType = MillCulture.getCulture(culture).getVillageType(villageName);

                    villageName = villageType.getVillageName();

                    village = Village.createVillage(this.getPos(), world, villageType, MillCulture.getCulture(culture), villageName);
                    village.setupVillage();

                    markDirty();
                } catch (Exception ex) {
                    System.err.println("Something went catastrophically wrong creating this village");
                    ex.printStackTrace();
                }
            } else {
                System.err.println("VillageStone TileEntity loaded wrong");
            }
        }

    }

    //@SideOnly(Side.SERVER)
    public EntityMillVillager createVillager(World worldIn, MillCulture cultureIn, int villagerID) {
        VillagerType currentVillagerType;
        int currentGender;

        if (villagerID == 0) {
            int balance = 0;
            villagerID = (int) CommonUtilities.getRandomNonzero();
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
    public void readFromNBT(NBTTagCompound compound) {
        System.out.println("TEVS Reading state");
        String uuidRaw;
        if((uuidRaw = compound.getString("VillageUUID")) != null) {
            villageID = UUID.fromString(uuidRaw);
            shouldGenVillage = false;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        if(village != null)
            compound.setString("VillageUUID", village.getUUID().toString());
    }
}
