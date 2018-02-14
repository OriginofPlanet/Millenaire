package org.millenaire;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.SidedProxy;
import org.millenaire.blocks.MillBlocks;
import org.millenaire.entities.EntityMillVillager;
import org.millenaire.generation.VillageGenerator;
import org.millenaire.gui.MillAchievement;
import org.millenaire.gui.MillGuiHandler;
import org.millenaire.items.MillItems;
import org.millenaire.networking.MillPacket;
import org.millenaire.networking.PacketExportBuilding;
import org.millenaire.networking.PacketImportBuilding;
import org.millenaire.networking.PacketSayTranslatedMessage;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.millenaire.proxy.CommonProxy;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION, guiFactory = Reference.GUIFACTORY)
public class Millenaire {
    public static boolean isServer = true;

    public List<Block> forbiddenBlocks;

    @Instance
    public static Millenaire instance = new Millenaire();
    public static SimpleNetworkWrapper simpleNetworkWrapper;

    @SidedProxy(modId = Reference.MODID, clientSide = "org.millenaire.proxy.ClientProxy", serverSide = "org.millenaire.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs tabMillenaire = new CreativeTabs("MillTab") {
        public Item getTabIconItem () { return MillItems.denierOr; }
    };

    @EventHandler
    public void preinit (FMLPreInitializationEvent event) {
        MillConfig.preinitialize();
        MinecraftForge.EVENT_BUS.register(new RaidEvent.RaidEventHandler());

        setForbiddenBlocks();

        MillBlocks.preinitialize();
        MillBlocks.recipes();

        MillItems.preinitialize();
        MillItems.recipies();
        EntityMillVillager.preinitialize();

        MillCulture.preinitialize();

        MillAchievement.preinitialize();

        if (event.getSide() == Side.CLIENT) {
            proxy.prerenderBlocks();
            proxy.prerenderItems();

            EntityMillVillager.prerender();

            MillConfig.eventRegister();

            isServer = false;
        }

        simpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("MillChannel");
        simpleNetworkWrapper.registerMessage(MillPacket.PacketHandlerOnServer.class, MillPacket.class, 0, Side.SERVER);
        simpleNetworkWrapper.registerMessage(PacketImportBuilding.Handler.class, PacketImportBuilding.class, 1, Side.SERVER);
        simpleNetworkWrapper.registerMessage(PacketSayTranslatedMessage.Handler.class, PacketSayTranslatedMessage.class, 2, Side.CLIENT);
        simpleNetworkWrapper.registerMessage(PacketExportBuilding.Handler.class, PacketExportBuilding.class, 3, Side.SERVER);
    }

    @EventHandler
    public void init (FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new MillGuiHandler());
        GameRegistry.registerWorldGenerator(new VillageGenerator(), 1000);

        if (event.getSide() == Side.CLIENT) {
            proxy.renderBlocks();
            proxy.renderItems();
        }
    }

    @EventHandler
    public void postinit (FMLPostInitializationEvent event) {

    }

    private void setForbiddenBlocks () {
        String parsing = MillConfig.forbiddenBlocks.substring(11);
        forbiddenBlocks = new ArrayList<>();

        for (final String name : parsing.split(", |,")) {
            if (Block.blockRegistry.containsKey(new ResourceLocation(name))) {
                forbiddenBlocks.add(Block.blockRegistry.getObject(new ResourceLocation(name)));
            }
        }
    }

    @EventHandler
    public void serverLoad (FMLServerStartingEvent event) { event.registerServerCommand(new MillCommand()); }
}
