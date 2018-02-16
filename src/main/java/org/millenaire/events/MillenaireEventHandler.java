package org.millenaire.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.millenaire.PlayerTracker;

public class MillenaireEventHandler {

    @SubscribeEvent
    public void onEntityConstructing(EntityConstructing event) {
        if (event.entity.worldObj.isRemote) return;

        if (event.entity instanceof EntityPlayer && PlayerTracker.get((EntityPlayer) event.entity) == null) {
            PlayerTracker.register((EntityPlayer) event.entity);
        }
    }
}