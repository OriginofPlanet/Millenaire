package org.millenaire.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.millenaire.blocks.MillBlocks;
import org.millenaire.blocks.StoredPosition;

public class PacketShowBuildPoints implements IMessage {

    boolean show;

    public PacketShowBuildPoints() {

    }

    public PacketShowBuildPoints(boolean show) {
        this.show = show;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        show = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(show);
    }

    public static class Handler implements IMessageHandler<PacketShowBuildPoints, IMessage> {

        @Override
        public IMessage onMessage(PacketShowBuildPoints message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketShowBuildPoints message, MessageContext ctx) {
            ((StoredPosition) MillBlocks.storedPosition).setShowParticles(message.show);
        }
    }
}
