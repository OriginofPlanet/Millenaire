package org.millenaire.proxy;

import net.minecraft.server.MinecraftServer;

public class CommonProxy {
    public void prerenderBlocks() {

    }

    public void renderBlocks() {

    }

    public void renderItems() {

    }

    public void prerenderItems() {

    }

    public void scheduleTask(Runnable task) {
        MinecraftServer.getServer().addScheduledTask(task);
    }
}
