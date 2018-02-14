package org.millenaire.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.millenaire.Millenaire;
import org.millenaire.entities.TileEntityMillChest;
import org.millenaire.entities.TileEntityMillSign;
import org.millenaire.entities.TileEntityVillageStone;
import org.millenaire.items.*;

public class MillBlocks {
    public static Block blockDecorativeStone;
    public static Block blockDecorativeWood;
    public static Block blockDecorativeEarth;

    public static Block emptySericulture;
    public static Block mudBrick;

    public static Block thatchSlab;
    public static Block thatchSlabDouble;
    public static Block thatchStairs;

    public static Block byzantineTile;
    public static Block byzantineStoneTile;
    public static Block byzantineTileSlab;
    public static Block byzantineTileSlabDouble;
    public static Block byzantineTileStairs;

    public static Block paperWall;

    public static Block blockSodPlanks;
    public static Block blockCarving;

    public static Block cropTurmeric;
    public static Block cropRice;
    public static Block cropMaize;
    public static Block cropGrapeVine;

    public static Block blockMillChest;

    public static Block blockMillSign;

    public static Block blockAlchemists;

    public static Block blockMillPath;
    public static Block blockMillPathSlab;
    public static Block blockMillPathSlabDouble;

    public static Block galianiteOre;

    public static Block villageStone;

    public static Block storedPosition;

    public static void preinitialize () {

        //Decorative
        blockDecorativeStone = new BlockDecorativeStone().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("blockDecorativeStone");
        GameRegistry.registerBlock(blockDecorativeStone, ItemBlockDecorativeStone.class, "blockDecorativeStone");

        blockDecorativeWood = new BlockDecorativeWood().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("blockDecorativeWood");
        GameRegistry.registerBlock(blockDecorativeWood, ItemBlockDecorativeWood.class, "blockDecorativeWood");

        blockDecorativeEarth = new BlockDecorativeEarth().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("blockDecorativeEarth");
        GameRegistry.registerBlock(blockDecorativeEarth, ItemBlockDecorativeEarth.class, "blockDecorativeEarth");

        blockSodPlanks = new BlockDecorativeSodPlank().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("blockSodPlank");
        GameRegistry.registerBlock(blockSodPlanks, ItemBlockDecorativeSodPlank.class, "blockSodPlank");

        emptySericulture = new BlockDecorativeUpdate(Material.wood, blockDecorativeWood.getDefaultState().withProperty(BlockDecorativeWood.VARIANT, BlockDecorativeWood.EnumType.SERICULTURE)).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("emptySericulture");
        GameRegistry.registerBlock(emptySericulture, "emptySericulture");

        mudBrick = new BlockDecorativeUpdate(Material.ground, blockDecorativeEarth.getDefaultState().withProperty(BlockDecorativeEarth.VARIANT, BlockDecorativeEarth.EnumType.DRIEDBRICK)).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("mudBrick");
        GameRegistry.registerBlock(mudBrick, "mudBrick");

        thatchSlab = new BlockDecorativeOrientedSlabHalf(Material.wood).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("thatchSlab");
        thatchSlabDouble = new BlockDecorativeOrientedSlabDouble(Material.wood, thatchSlab).setUnlocalizedName("thatchSlabDouble");
        GameRegistry.registerBlock(thatchSlab, ItemOrientedSlab.class, "thatchSlab", thatchSlab, thatchSlabDouble);
        GameRegistry.registerBlock(thatchSlabDouble, ItemOrientedSlab.class, "thatchSlabDouble", thatchSlab, thatchSlabDouble);

        thatchStairs = new BlockDecorativeOrientedStairs(blockDecorativeWood.getDefaultState().withProperty(BlockDecorativeWood.VARIANT, BlockDecorativeWood.EnumType.THATCH)).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("thatchStairs");
        GameRegistry.registerBlock(thatchStairs, "thatchStairs");

        byzantineTile = new BlockDecorativeOriented(Material.rock).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("byzantineTile");
        GameRegistry.registerBlock(byzantineTile, "byzantineTile");

        byzantineStoneTile = new BlockDecorativeOriented(Material.rock).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("byzantineStoneTile");
        GameRegistry.registerBlock(byzantineStoneTile, "byzantineStoneTile");

        byzantineTileSlab = new BlockDecorativeOrientedSlabHalf(Material.rock).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("byzantineTileSlab");
        byzantineTileSlabDouble = new BlockDecorativeOrientedSlabDouble(Material.rock, byzantineTileSlab).setUnlocalizedName("byzantineTileSlabDouble");
        GameRegistry.registerBlock(byzantineTileSlab, ItemOrientedSlab.class, "byzantineTileSlab", byzantineTileSlab, byzantineTileSlabDouble);
        GameRegistry.registerBlock(byzantineTileSlabDouble, ItemOrientedSlab.class, "byzantineTileSlabDouble", byzantineTileSlab, byzantineTileSlabDouble);

        byzantineTileStairs = new BlockDecorativeOrientedStairs(byzantineStoneTile.getDefaultState()).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("byzantineTileStairs");
        GameRegistry.registerBlock(byzantineTileStairs, "byzantineTileStairs");

        paperWall = new BlockDecorativePane(Material.cloth).setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("paperWall");
        GameRegistry.registerBlock(paperWall, "paperWall");

        blockCarving = new BlockDecorativeCarving(Material.rock).setUnlocalizedName("inuitCarving");
        GameRegistry.registerBlock(blockCarving, "inuitCarving");

        //Crops
        cropTurmeric = new BlockMillCrops(false, false).setCreativeTab(null).setUnlocalizedName("cropTurmeric");
        GameRegistry.registerBlock(cropTurmeric, "cropTurmeric");

        cropRice = new BlockMillCrops(true, false).setCreativeTab(null).setUnlocalizedName("cropRice");
        GameRegistry.registerBlock(cropRice, "cropRice");

        cropMaize = new BlockMillCrops(false, true).setCreativeTab(null).setUnlocalizedName("cropMaize");
        GameRegistry.registerBlock(cropMaize, "cropMaize");

        cropGrapeVine = new BlockMillCrops(false, false).setCreativeTab(null).setUnlocalizedName("cropGrapeVine");
        GameRegistry.registerBlock(cropGrapeVine, "cropGrapeVine");

        //Chests
        blockMillChest = new BlockMillChest().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("blockMillChest");
        GameRegistry.registerBlock(blockMillChest, "blockMillChest");
        GameRegistry.registerTileEntity(TileEntityMillChest.class, "tileEntityMillChest");

        //Sign
        blockMillSign = new BlockMillSign().setUnlocalizedName("blockMillSign");
        GameRegistry.registerBlock(blockMillSign, "blockMillSign");
        GameRegistry.registerTileEntity(TileEntityMillSign.class, "tileEntityMillSign");

        //Alchemists
        blockAlchemists = new BlockAlchemists().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("blockAlchemists");
        GameRegistry.registerBlock(blockAlchemists, "blockAlchemists");

        //Paths
        blockMillPath = new BlockMillPath().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("blockMillPath");
        GameRegistry.registerBlock(blockMillPath, ItemMillPath.class, "blockMillPath");

        blockMillPathSlab = new BlockMillPathSlabHalf().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("blockMillPathSlab");
        blockMillPathSlabDouble = new BlockMillPathSlabDouble().setUnlocalizedName("blockMillPathSlabDouble");
        GameRegistry.registerBlock(blockMillPathSlab, ItemMillPathSlab.class, "blockMillPathSlab", blockMillPathSlab, blockMillPathSlabDouble);
        GameRegistry.registerBlock(blockMillPathSlabDouble, ItemMillPathSlab.class, "blockMillPathSlabDouble", blockMillPathSlab, blockMillPathSlabDouble);

        //Ores
        galianiteOre = new BlockMillOre(BlockMillOre.EnumMillOre.GALIANITE).setUnlocalizedName("galianiteOre");
        GameRegistry.registerBlock(galianiteOre, "galianiteOre");

        //Village Stone
        villageStone = new BlockVillageStone().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("villageStone");
        GameRegistry.registerBlock(villageStone, "villageStone");
        GameRegistry.registerTileEntity(TileEntityVillageStone.class, "tileEntityVillageStone");

        //StoredPosition
        storedPosition = new StoredPosition().setCreativeTab(Millenaire.tabMillenaire).setUnlocalizedName("storedPosition");
        GameRegistry.registerBlock(storedPosition, "storedPosition");
    }

    public static void recipes () {
        GameRegistry.addSmelting(mudBrick, new ItemStack(blockDecorativeStone, 1, 1), 0.3f);
        GameRegistry.addRecipe(new ItemStack(byzantineStoneTile, 6),
                               "AAA",
                               "BBB",
                               'A', new ItemStack(byzantineTile), 'B', new ItemStack(Blocks.stone));
        GameRegistry.addRecipe(new ItemStack(byzantineTileStairs, 4),
                               "A  ",
                               "BA ",
                               "BBA",
                               'A', new ItemStack(byzantineTile), 'B', new ItemStack(Blocks.stone));

        //Paths
        for (int i = 0; i < BlockMillPath.EnumType.values().length; i++) {
            GameRegistry.addRecipe(new ItemStack(blockMillPathSlab, 6, i),
                                   "AAA",
                                   'A', new ItemStack(blockMillPath, 1, i));
            GameRegistry.addRecipe(new ItemStack(blockMillPath, 1, i),
                                   "A",
                                   "A",
                                   'A', new ItemStack(blockMillPathSlab, 1, i));
        }
    }


}
