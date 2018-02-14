package org.millenaire.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.millenaire.Reference;
import org.millenaire.blocks.MillBlocks;
import org.millenaire.entities.TileEntityMillChest;
import org.millenaire.entities.TileEntityMillSign;
import org.millenaire.items.MillItems;

public class ClientProxy extends CommonProxy {

    @Override
    public void prerenderBlocks () {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeStone), 0, new ModelResourceLocation(Reference.MODID + ":goldOrnament", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeStone), 1, new ModelResourceLocation(Reference.MODID + ":cookedBrick", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeStone), 2, new ModelResourceLocation(Reference.MODID + ":galianiteBlock", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeWood), 0, new ModelResourceLocation(Reference.MODID + ":plainTimberFrame", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeWood), 1, new ModelResourceLocation(Reference.MODID + ":crossTimberFrame", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeWood), 2, new ModelResourceLocation(Reference.MODID + ":thatch", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeWood), 3, new ModelResourceLocation(Reference.MODID + ":sericulture", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeEarth), 0, new ModelResourceLocation(Reference.MODID + ":dirtWall", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockDecorativeEarth), 1, new ModelResourceLocation(Reference.MODID + ":driedBrick", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.thatchSlabDouble), 0, new ModelResourceLocation(Reference.MODID + ":thatch"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.byzantineTileSlabDouble), 0, new ModelResourceLocation(Reference.MODID + ":byzantineTile", "inventory"));

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockSodPlanks), 0, new ModelResourceLocation(Reference.MODID + ":sodOak", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockSodPlanks), 1, new ModelResourceLocation(Reference.MODID + ":sodPine", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockSodPlanks), 2, new ModelResourceLocation(Reference.MODID + ":sodBirch", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockSodPlanks), 3, new ModelResourceLocation(Reference.MODID + ":sodJungle", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockSodPlanks), 4, new ModelResourceLocation(Reference.MODID + ":sodJungle", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockSodPlanks), 5, new ModelResourceLocation(Reference.MODID + ":sodPine", "inventory"));

        //Paths
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPath), 0, new ModelResourceLocation(Reference.MODID + ":pathDirt", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPath), 1, new ModelResourceLocation(Reference.MODID + ":pathGravel", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPath), 2, new ModelResourceLocation(Reference.MODID + ":pathSlab", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPath), 3, new ModelResourceLocation(Reference.MODID + ":pathSandstoneSlab", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPath), 4, new ModelResourceLocation(Reference.MODID + ":pathOchreSlab", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPath), 5, new ModelResourceLocation(Reference.MODID + ":pathSlabAndGravel", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlab), 0, new ModelResourceLocation(Reference.MODID + ":pathDirtHalf", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlab), 1, new ModelResourceLocation(Reference.MODID + ":pathGravelHalf", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlab), 2, new ModelResourceLocation(Reference.MODID + ":pathSlabHalf", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlab), 3, new ModelResourceLocation(Reference.MODID + ":pathSandstoneSlabHalf", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlab), 4, new ModelResourceLocation(Reference.MODID + ":pathOchreSlabHalf", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlab), 5, new ModelResourceLocation(Reference.MODID + ":pathSlabAndGravelHalf", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlabDouble), 0, new ModelResourceLocation(Reference.MODID + ":pathDirt", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlabDouble), 1, new ModelResourceLocation(Reference.MODID + ":pathGravel", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlabDouble), 2, new ModelResourceLocation(Reference.MODID + ":pathSlab", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlabDouble), 3, new ModelResourceLocation(Reference.MODID + ":pathSandstoneSlab", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlabDouble), 4, new ModelResourceLocation(Reference.MODID + ":pathOchreSlab", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MillBlocks.blockMillPathSlabDouble), 5, new ModelResourceLocation(Reference.MODID + ":pathSlabAndGravel", "inventory"));
    }

    @Override
    public void renderBlocks () {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.emptySericulture), 0, new ModelResourceLocation(Reference.MODID + ":emptySericulture", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.mudBrick), 0, new ModelResourceLocation(Reference.MODID + ":mudBrick", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.thatchSlab), 0, new ModelResourceLocation(Reference.MODID + ":thatchSlab", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.thatchStairs), 0, new ModelResourceLocation(Reference.MODID + ":thatchStairs", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.byzantineTile), 0, new ModelResourceLocation(Reference.MODID + ":byzantineTile", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.byzantineStoneTile), 0, new ModelResourceLocation(Reference.MODID + ":byzantineStoneTile", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.byzantineTileSlab), 0, new ModelResourceLocation(Reference.MODID + ":byzantineTileSlab", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.paperWall), 0, new ModelResourceLocation(Reference.MODID + ":paperWall", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.byzantineTileStairs), 0, new ModelResourceLocation(Reference.MODID + ":byzantineTileStairs", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.blockCarving), 0, new ModelResourceLocation(Reference.MODID + ":inuitCarving", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.blockMillChest), 0, new ModelResourceLocation(Reference.MODID + ":blockMillChest", "inventory"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMillChest.class, new TileEntityChestRenderer());
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.blockMillSign), 0, new ModelResourceLocation(Reference.MODID + ":blockMillSign", "inventory"));
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMillSign.class, new TileEntitySignRenderer());
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.blockAlchemists), 0, new ModelResourceLocation(Reference.MODID + ":blockAlchemists", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.galianiteOre), 0, new ModelResourceLocation(Reference.MODID + ":galianiteOre", "inventory"));
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.villageStone), 0, new ModelResourceLocation(Reference.MODID + ":villageStone", "inventory"));
        renderItem.getItemModelMesher().getModelManager().getBlockModelShapes().registerBuiltInBlocks(MillBlocks.storedPosition);
        renderItem.getItemModelMesher().register(Item.getItemFromBlock(MillBlocks.storedPosition), 0, new ModelResourceLocation(Reference.MODID + ":storedPosition", "inventory"));
    }

    @Override
    public void prerenderItems () {
        //Tools
        //ModelBakery.addVariantName(japaneseBow_pulling_1, Reference.MODID + ":japaneseBow", Reference.MODID + ":japaneseBow_pulling_1", Reference.MODID + ":japaneseBow_pulling_2", Reference.MODID + ":japaneseBow_pulling_3");
        ModelBakery.registerItemVariants(MillItems.japaneseBow, new ModelResourceLocation(Reference.MODID + ":japaneseBow", "inventory"), new ModelResourceLocation(Reference.MODID + ":japaneseBow_pulling_1", "inventory"),
                                         new ModelResourceLocation(Reference.MODID + ":japaneseBow_pulling_2", "inventory"), new ModelResourceLocation(Reference.MODID + ":japaneseBow_pulling_3", "inventory"));
        ModelLoader.setCustomModelResourceLocation(MillItems.japaneseBow, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBow", "inventory"));
		/*ModelLoader.setCustomModelResourceLocation(japaneseBow, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBow_pulling_1", "inventory"));
    	ModelLoader.setCustomModelResourceLocation(japaneseBow, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBow_pulling_2", "inventory"));
    	ModelLoader.setCustomModelResourceLocation(japaneseBow, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBow_pulling_3", "inventory"));*/

        //Amulets
        ModelLoader.setCustomModelResourceLocation(MillItems.amuletSkollHati, 0, new ModelResourceLocation(Reference.MODID + ":amuletSkollHati"));
        ModelLoader.setCustomModelResourceLocation(MillItems.amuletAlchemist, 0, new ModelResourceLocation(Reference.MODID + ":amuletAlchemist"));
        ModelLoader.setCustomModelResourceLocation(MillItems.amuletVishnu, 0, new ModelResourceLocation(Reference.MODID + ":amuletVishnu"));
        ModelLoader.setCustomModelResourceLocation(MillItems.amuletYggdrasil, 0, new ModelResourceLocation(Reference.MODID + ":amuletYggdrasil"));

        //Parchments
        ModelLoader.setCustomModelResourceLocation(MillItems.normanVillagerParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentVillager"));
        ModelLoader.setCustomModelResourceLocation(MillItems.normanBuildingParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentBuilding"));
        ModelLoader.setCustomModelResourceLocation(MillItems.normanItemParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentItem"));
        ModelLoader.setCustomModelResourceLocation(MillItems.normanAllParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentAll"));

        ModelLoader.setCustomModelResourceLocation(MillItems.byzantineVillagerParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentVillager"));
        ModelLoader.setCustomModelResourceLocation(MillItems.byzantineBuildingParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentBuilding"));
        ModelLoader.setCustomModelResourceLocation(MillItems.byzantineItemParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentItem"));
        ModelLoader.setCustomModelResourceLocation(MillItems.byzantineAllParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentAll"));

        ModelLoader.setCustomModelResourceLocation(MillItems.hindiVillagerParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentVillager"));
        ModelLoader.setCustomModelResourceLocation(MillItems.hindiBuildingParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentBuilding"));
        ModelLoader.setCustomModelResourceLocation(MillItems.hindiItemParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentItem"));
        ModelLoader.setCustomModelResourceLocation(MillItems.hindiAllParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentAll"));

        ModelLoader.setCustomModelResourceLocation(MillItems.mayanVillagerParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentVillager"));
        ModelLoader.setCustomModelResourceLocation(MillItems.mayanBuildingParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentBuilding"));
        ModelLoader.setCustomModelResourceLocation(MillItems.mayanItemParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentItem"));
        ModelLoader.setCustomModelResourceLocation(MillItems.mayanAllParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentAll"));

        ModelLoader.setCustomModelResourceLocation(MillItems.japaneseVillagerParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentVillager"));
        ModelLoader.setCustomModelResourceLocation(MillItems.japaneseBuildingParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentBuilding"));
        ModelLoader.setCustomModelResourceLocation(MillItems.japaneseItemParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentItem"));
        ModelLoader.setCustomModelResourceLocation(MillItems.japaneseAllParchment, 0, new ModelResourceLocation(Reference.MODID + ":parchmentAll"));
    }

    @Override
    public void renderItems () {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        renderItem.getItemModelMesher().register(MillItems.denier, 0, new ModelResourceLocation(Reference.MODID + ":denier", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.denierOr, 0, new ModelResourceLocation(Reference.MODID + ":denierOr", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.denierArgent, 0, new ModelResourceLocation(Reference.MODID + ":denierArgent", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.silk, 0, new ModelResourceLocation(Reference.MODID + ":silk", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.obsidianFlake, 0, new ModelResourceLocation(Reference.MODID + ":obsidianFlake", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.unknownPowder, 0, new ModelResourceLocation(Reference.MODID + ":unknownPowder", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.woolClothes, 0, new ModelResourceLocation(Reference.MODID + ":woolClothes", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.silkClothes, 0, new ModelResourceLocation(Reference.MODID + ":silkClothes", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.galianiteDust, 0, new ModelResourceLocation(Reference.MODID + ":galianiteDust", "inventory"));

        //Crops
        renderItem.getItemModelMesher().register(MillItems.turmeric, 0, new ModelResourceLocation(Reference.MODID + ":turmeric", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.rice, 0, new ModelResourceLocation(Reference.MODID + ":rice", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.maize, 0, new ModelResourceLocation(Reference.MODID + ":maize", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.grapes, 0, new ModelResourceLocation(Reference.MODID + ":grapes", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.ciderApple, 0, new ModelResourceLocation(Reference.MODID + ":ciderApple", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.cider, 0, new ModelResourceLocation(Reference.MODID + ":cider", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.calva, 0, new ModelResourceLocation(Reference.MODID + ":calva", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.tripes, 0, new ModelResourceLocation(Reference.MODID + ":tripes", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.boudinNoir, 0, new ModelResourceLocation(Reference.MODID + ":boudinNoir", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.vegCurry, 0, new ModelResourceLocation(Reference.MODID + ":vegCurry", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.murghCurry, 0, new ModelResourceLocation(Reference.MODID + ":murghCurry", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.rasgulla, 0, new ModelResourceLocation(Reference.MODID + ":rasgulla", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.cacauhaa, 0, new ModelResourceLocation(Reference.MODID + ":cacauhaa", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.masa, 0, new ModelResourceLocation(Reference.MODID + ":masa", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.wah, 0, new ModelResourceLocation(Reference.MODID + ":wah", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.wine, 0, new ModelResourceLocation(Reference.MODID + ":wine", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.malvasiaWine, 0, new ModelResourceLocation(Reference.MODID + ":malvasiaWine", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.feta, 0, new ModelResourceLocation(Reference.MODID + ":feta", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.souvlaki, 0, new ModelResourceLocation(Reference.MODID + ":souvlaki", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.sake, 0, new ModelResourceLocation(Reference.MODID + ":sake", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.udon, 0, new ModelResourceLocation(Reference.MODID + ":udon", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.ikayaki, 0, new ModelResourceLocation(Reference.MODID + ":ikayaki", "inventory"));

        //Armour
        renderItem.getItemModelMesher().register(MillItems.normanHelmet, 0, new ModelResourceLocation(Reference.MODID + ":normanHelmet", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.normanChestplate, 0, new ModelResourceLocation(Reference.MODID + ":normanChestplate", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.normanLeggings, 0, new ModelResourceLocation(Reference.MODID + ":normanLeggings", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.normanBoots, 0, new ModelResourceLocation(Reference.MODID + ":normanBoots", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.byzantineHelmet, 0, new ModelResourceLocation(Reference.MODID + ":byzantineHelmet", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.byzantineChestplate, 0, new ModelResourceLocation(Reference.MODID + ":byzantineChestplate", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.byzantineLeggings, 0, new ModelResourceLocation(Reference.MODID + ":byzantineLeggings", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.byzantineBoots, 0, new ModelResourceLocation(Reference.MODID + ":byzantineBoots", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.japaneseGuardHelmet, 0, new ModelResourceLocation(Reference.MODID + ":japaneseGuardHelmet", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseGuardChestplate, 0, new ModelResourceLocation(Reference.MODID + ":japaneseGuardChestplate", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseGuardLeggings, 0, new ModelResourceLocation(Reference.MODID + ":japaneseGuardLeggings", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseGuardBoots, 0, new ModelResourceLocation(Reference.MODID + ":japaneseGuardBoots", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.japaneseBlueHelmet, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBlueHelmet", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseBlueChestplate, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBlueChestplate", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseBlueLeggings, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBlueLeggings", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseBlueBoots, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBlueBoots", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.japaneseRedHelmet, 0, new ModelResourceLocation(Reference.MODID + ":japaneseRedHelmet", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseRedChestplate, 0, new ModelResourceLocation(Reference.MODID + ":japaneseRedChestplate", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseRedLeggings, 0, new ModelResourceLocation(Reference.MODID + ":japaneseRedLeggings", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.japaneseRedBoots, 0, new ModelResourceLocation(Reference.MODID + ":japaneseRedBoots", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.mayanQuestCrown, 0, new ModelResourceLocation(Reference.MODID + ":mayanQuestCrown", "inventory"));

        //Wands
        renderItem.getItemModelMesher().register(MillItems.wandSummoning, 0, new ModelResourceLocation(Reference.MODID + ":wandSummoning", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.wandNegation, 0, new ModelResourceLocation(Reference.MODID + ":wandNegation", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.wandCreative, 0, new ModelResourceLocation(Reference.MODID + ":wandCreative", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.tuningFork, 0, new ModelResourceLocation(Reference.MODID + ":tuningFork", "inventory"));

        //Tools
        renderItem.getItemModelMesher().register(MillItems.normanAxe, 0, new ModelResourceLocation(Reference.MODID + ":normanAxe", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.normanShovel, 0, new ModelResourceLocation(Reference.MODID + ":normanShovel", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.normanPickaxe, 0, new ModelResourceLocation(Reference.MODID + ":normanPickaxe", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.normanHoe, 0, new ModelResourceLocation(Reference.MODID + ":normanHoe", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.normanSword, 0, new ModelResourceLocation(Reference.MODID + ":normanSword", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.mayanAxe, 0, new ModelResourceLocation(Reference.MODID + ":mayanAxe", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.mayanShovel, 0, new ModelResourceLocation(Reference.MODID + ":mayanShovel", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.mayanPickaxe, 0, new ModelResourceLocation(Reference.MODID + ":mayanPickaxe", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.mayanHoe, 0, new ModelResourceLocation(Reference.MODID + ":mayanHoe", "inventory"));
        renderItem.getItemModelMesher().register(MillItems.mayanMace, 0, new ModelResourceLocation(Reference.MODID + ":mayanMace", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.byzantineMace, 0, new ModelResourceLocation(Reference.MODID + ":byzantineMace", "inventory"));

        renderItem.getItemModelMesher().register(MillItems.japaneseSword, 0, new ModelResourceLocation(Reference.MODID + ":japaneseSword", "inventory"));
        //renderItem.getItemModelMesher().register(japaneseBow_pulling_1, 0, new ModelResourceLocation(Reference.MODID + ":japaneseBow_pulling_1", "inventory"));

        //Wallet
        renderItem.getItemModelMesher().register(MillItems.itemMillPurse, 0, new ModelResourceLocation(Reference.MODID + ":itemMillPurse", "inventory"));

        //Sign
        renderItem.getItemModelMesher().register(MillItems.itemMillSign, 0, new ModelResourceLocation(Reference.MODID + ":blockMillSign", "inventory"));
    }
}
