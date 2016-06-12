package org.millenaire;

import org.millenaire.gui.MillAchievement;
import org.millenaire.items.MillItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class CommonUtilities 
{
	public static void changeMoney(EntityPlayer playerIn)
	{
		ItemStack denier = new ItemStack(MillItems.denier, 0, 0);
		ItemStack argent = new ItemStack(MillItems.denierArgent, 0, 0);
		ItemStack or = new ItemStack(MillItems.denierOr, 0, 0);
		
		for(int i = 0; i < playerIn.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = playerIn.inventory.getStackInSlot(i);
			if(stack != null)
			{
				if(stack.getItem() == MillItems.denier)
				{
					denier.stackSize = denier.stackSize + stack.stackSize;
					playerIn.inventory.removeStackFromSlot(i);
				}
				if(stack.getItem() == MillItems.denierArgent)
				{
					argent.stackSize = argent.stackSize + stack.stackSize;
					playerIn.inventory.removeStackFromSlot(i);
				}
				if(stack.getItem() == MillItems.denierOr)
				{
					or.stackSize = or.stackSize + stack.stackSize;
					playerIn.inventory.removeStackFromSlot(i);
				}
			}
		}
		
		argent.stackSize = argent.stackSize + (denier.stackSize / 64);
		denier.stackSize = denier.stackSize % 64;
		
		or.stackSize = or.stackSize + (argent.stackSize / 64);
		if(or.stackSize >= 1)
			playerIn.addStat(MillAchievement.cresus, 1);
		argent.stackSize = argent.stackSize % 64;
		
		playerIn.inventory.addItemStackToInventory(denier);
		playerIn.inventory.addItemStackToInventory(argent);
		
		while(or.stackSize > 64)
		{
			playerIn.inventory.addItemStackToInventory(new ItemStack(MillItems.denierOr, 64, 0));
			or.stackSize = or.stackSize - 64;
		}		
		playerIn.inventory.addItemStackToInventory(or);
	}
}