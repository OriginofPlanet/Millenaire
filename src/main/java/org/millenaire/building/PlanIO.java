package org.millenaire.building;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.BlockFluidBase;
import org.millenaire.MillCulture;
import org.millenaire.Millenaire;
import org.millenaire.VillageGeography;
import org.millenaire.networking.PacketSayTranslatedMessage;

import java.io.*;
import java.util.*;

public class PlanIO {

    private static final String FILE_VERSION = "2";

    /**
     * Called by the {@link org.millenaire.networking.PacketExportBuilding} on the server-side.
     * Reads a sign specified by the given BlockPos, and exports to a file based on those params.
     *
     * @param player The player exporting the building.
     * @param signPos The position of the sign containing the export parameters.
     */
    public static void exportBuilding(EntityPlayer player, BlockPos signPos) {
        try {
            TileEntitySign sign = (TileEntitySign) player.getEntityWorld().getTileEntity(signPos);

            String buildingName = sign.signText[0].getUnformattedText();
            boolean saveSnow = (sign.signText[3].getUnformattedText().toLowerCase().equals("snow"));

            int buildingLevel = 1;

            if (sign.signText[1] != null && sign.signText[1].getUnformattedText().length() > 0) {
                buildingLevel = Integer.parseInt(sign.signText[1].getUnformattedText());
            }

            if (buildingLevel < 0) {
                PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.error.exporting.level0");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
            }

            int startLevel = -1;

            if (sign.signText[2] != null && sign.signText[2].getUnformattedText().length() > 0) {
                startLevel = Integer.parseInt(sign.signText[2].getUnformattedText());
            }

            if (buildingName == null || buildingName.length() == 0) {
                PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.error.exporting.noname");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                throw new Exception("exporting.noname");
            }

            PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.export.start.read");
            Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);

            boolean foundEnd = false;
            int xEnd = signPos.getX() + 1;
            while (xEnd < signPos.getX() + 257) {
                final IBlockState block = player.getEntityWorld().getBlockState(new BlockPos(xEnd, signPos.getY(), signPos.getZ()));

                if (block.getBlock() == Blocks.standing_sign) {
                    foundEnd = true;
                    break;
                }
                xEnd++;
            }
            if (!foundEnd) {
                packet = new PacketSayTranslatedMessage("message.error.exporting.xaxis");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                throw new Exception("exporting.xaxis");
            }
            foundEnd = false;
            int zEnd = signPos.getZ() + 1;
            while (zEnd < signPos.getZ() + 257) {
                final IBlockState block = player.getEntityWorld().getBlockState(new BlockPos(signPos.getX(), signPos.getY(), zEnd));

                if (block.getBlock() == Blocks.standing_sign) {
                    foundEnd = true;
                    break;
                }
                zEnd++;
            }
            if (!foundEnd) {
                packet = new PacketSayTranslatedMessage("message.error.exporting.zaxis");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                throw new Exception("Ahhh!");
            }

            final int width = xEnd - signPos.getX() - 1;
            final int length = zEnd - signPos.getZ() - 1;

            boolean stop = false;
            int y = 0;

            final Map<Integer, IBlockState[][]> ex = new HashMap<>();

            while (!stop) {

                IBlockState[][] level = new IBlockState[width][length];

                boolean blockFound = false;

                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        IBlockState block = player.getEntityWorld().getBlockState(new BlockPos(x + signPos.getX() + 1, y + signPos.getY() + startLevel, z + signPos.getZ() + 1));

                        if (block.getBlock() != Blocks.air) {
                            blockFound = true;
                        }
                        if (saveSnow || block.getBlock() != Blocks.snow) {
                            level[x][z] = block;
                        } else {
                            level[x][z] = Blocks.air.getDefaultState();
                        }
                    }
                }

                if (blockFound) {
                    ex.put(y, level);
                } else {
                    stop = true;
                }

                y++;

                if (y + signPos.getY() + startLevel >= 256) {
                    stop = true;
                }
            }

            IBlockState[][][] ex2 = new IBlockState[ex.size()][length][width];

            for (int i = 0; i < ex.size(); i++) {
                IBlockState[][] level = ex.get(i);
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < length; z++) {
                        ex2[i][z][x] = level[x][z];
                    }
                }
            }

            packet = new PacketSayTranslatedMessage("message.export.start.save");
            Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);

            exportToSchematic(ex2, (short) width, (short) ex.size(), (short) length, (short) startLevel, buildingName, buildingLevel, player);

            packet = new PacketSayTranslatedMessage("message.export.finish");
            Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
        } catch (Exception e) {
            e.printStackTrace();
            PacketSayTranslatedMessage packet2 = new PacketSayTranslatedMessage("message.notcompleted");
            Millenaire.simpleNetworkWrapper.sendTo(packet2, (EntityPlayerMP) player);
        }
    }

    /**
     * Imports a building from the exports folder based on the parameters on the sign at the given BlockPos.
     *
     * @param player The player importing the building.
     * @param signPos The sign containing import parameters.
     */
    public static void importBuilding(EntityPlayer player, BlockPos signPos) {
        try {
            TileEntitySign te = (TileEntitySign) player.getEntityWorld().getTileEntity(signPos);
            String name = te.signText[0].getUnformattedText();
            int level = 1;
            if (te.signText[1] != null && te.signText[1].getUnformattedText().length() > 0) {
                level = Integer.parseInt(te.signText[1].getUnformattedText());
            }

            if (name == null || name.length() == 0) {
                PacketSayTranslatedMessage message = new PacketSayTranslatedMessage("message.error.exporting.noname");
                Millenaire.simpleNetworkWrapper.sendTo(message, (EntityPlayerMP) player);
                PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.notcompleted");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                return;
            }

            World world = MinecraftServer.getServer().getEntityWorld();

            File schem = getBuildingFile(name);
            if (!schem.exists()) {
                PacketSayTranslatedMessage message = new PacketSayTranslatedMessage("message.error.importing.nofile");
                Millenaire.simpleNetworkWrapper.sendTo(message, (EntityPlayerMP) player);
                PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.notcompleted");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                return;
            }
            FileInputStream fis = new FileInputStream(schem);

            Building plan = loadSchematic(CompressedStreamTools.readCompressed(fis), MillCulture.normanCulture, level);

            placeBuilding(plan, new BuildingLocation(plan, signPos, EnumFacing.EAST), world);
        } catch (IOException e) {
            e.printStackTrace();
            PacketSayTranslatedMessage message = new PacketSayTranslatedMessage("message.error.unknown");
            Millenaire.simpleNetworkWrapper.sendTo(message, (EntityPlayerMP) player);
        }
    }

    /**
     * Flattens terrain for a building.
     *
     * @param building The building to flatten for.
     * @param loc The location we are flattening.
     * @param geo The geography of the village
     */
    public static void flattenTerrainForBuilding(Building building, BuildingLocation loc, VillageGeography geo) {
        //System.out.println("Flattening Terrain");
        int ylevel = loc.position.getY();

        World world = geo.world;

        int margin = 3; //TODO: Tweak?

        BlockPos corner1 = loc.position.subtract(new Vec3i(margin, 0, margin));
        BlockPos corner2 = loc.position.add(building.width + margin, 0, building.length + margin);

        //for (int xPos = corner1.getX(); xPos <= corner2.getX(); xPos++) {
        for (int xPos = loc.minxMargin; xPos <= loc.maxxMargin; xPos++) {
            //for (int zPos = corner1.getZ(); zPos <= corner2.getZ(); zPos++) {
            for (int zPos = loc.minzMargin; zPos <= loc.maxzMargin; zPos++) {
                BlockPos highestY = world.getHeight(new BlockPos(xPos, ylevel, zPos));

                while(world.getBlockState(highestY).getBlock() instanceof BlockFluidBase
                        || world.getBlockState(highestY).getBlock() instanceof BlockLiquid) {
                    highestY.down();
                }

                IBlockState topBlock = world.getBiomeGenForCoords(highestY).topBlock;
                IBlockState fillerBlock = world.getBiomeGenForCoords(highestY).fillerBlock.getBlock() == Blocks.sand ? Blocks.sandstone.getDefaultState() : world.getBiomeGenForCoords(highestY).fillerBlock;

                /*if (geo.buildingLoc[xPos - geo.mapStartX][zPos - geo.mapStartZ]) {
                    //Skip flattening if it would overlap
                    continue;
                }*/

                //System.out.println("Flattening at " + xPos + ", " + zPos);

                //Find highest actual surface block, to see if we need to block up.
                Block b = world.getBlockState(highestY).getBlock();
                while (b == Blocks.water || b == Blocks.flowing_water || b == Blocks.leaves || b == Blocks.leaves2
                        || b == Blocks.log || b == Blocks.log2) {
                    highestY = highestY.subtract(new Vec3i(0, 1, 0));
                    b = world.getBlockState(highestY).getBlock();
                }

                //System.out.println("Highest actual y is at " + highestY + ", target y is " + ylevel);

                //Do we need to block up? If so, do so.
                if (highestY.getY() < ylevel - 1) {
                    for (int yPos = highestY.getY(); yPos <= ylevel - 2; yPos++) {
                        //System.out.println("Building up at level " + yPos);
                        world.setBlockState(new BlockPos(xPos, yPos, zPos), fillerBlock);
                    }
                }

                //Now find the genuine highest block at this point and break down if we need to - e.g. to remove trees.
                highestY = world.getHeight(new BlockPos(xPos, ylevel, zPos));

                if (highestY.getY() > ylevel) {
                    for (int yPos = ylevel - 1; yPos <= highestY.getY(); yPos++) {
                        //System.out.println("Digging down at level " + yPos);
                        world.setBlockState(new BlockPos(xPos, yPos, zPos), Blocks.air.getDefaultState());
                    }
                }

                //System.out.println("Flattening: Setting top block (" + topBlock.getBlock() + ") at " + (ylevel - 1));
                //Finish the flattening for the actual level we're targeting
                world.setBlockState(new BlockPos(xPos, ylevel - 1, zPos), topBlock);

                //System.out.println("Creating foundation below building at level " + (ylevel + building.depth - 1) + " out of " + fillerBlock.getBlock());
                //Ensure we have grass under anything we build to prevent gravity-affected blocks from falling.
                world.setBlockState(new BlockPos(xPos, ylevel + building.depth - 1, zPos), fillerBlock);
            }

        }

    }

    /**
     * Places a building from the schematic file.
     *
     * @param plan The building to place
     * @param loc Where to place it
     * @param world The world to place it in.
     */
    public static void placeBuilding(Building plan, BuildingLocation loc, World world) {
        IBlockState[][][] blocks = plan.blocksInBuilding;

        //The list of blocks that must be placed last.
        //TODO: Any others? Any way to auto-detect? Config Value?

        List<Block> blocksToPlaceLast = Arrays.asList(Blocks.torch, Blocks.redstone_torch, Blocks.vine, Blocks.bed, Blocks.wall_sign, Blocks.standing_sign,
                Blocks.standing_banner, Blocks.wall_banner);

        //Some blocks must be placed last in order to not drop onto the floor. This stores their locations.
        LinkedHashMap<BlockPos, IBlockState> placeLast = new LinkedHashMap<>();

        for (int x = loc.minx; x < loc.maxx; x++) {
            for (int y = loc.miny; y < loc.maxy; y++) {
                for (int z = loc.minz; z < loc.maxz; z++) {
                    if (blocksToPlaceLast.contains(blocks[y][z][x].getBlock())) {
                        placeLast.put(new BlockPos(x, y, z), blocks[y][z][x]);
                    } else {
                        //System.out.println("Placing a " + blocks[y][z][x].getBlock());
                        world.setBlockState(new BlockPos(x, y, z), blocks[y][z][x], 2);
                    }
                }
            }
        }

        for (Map.Entry<BlockPos, IBlockState> entry : placeLast.entrySet()) {
            //System.out.println("Placing a " + entry.getValue().getBlock());
            world.setBlockState(entry.getKey(), entry.getValue(), 2);
        }
    }

    /**
     * Reads a Building from a schematic NBT tag.
     *
     * @param nbt The schematic file.
     * @param culture The culture of the building.
     * @param level What level of building we are placing.
     * @return The created {@link Building} object.
     */
    public static Building loadSchematic(NBTTagCompound nbt, MillCulture culture, int level) {
        //Convert Stream to NBTTagCompound

        //width = x-axis, height = y-axis, length = z-axis
        short width, height, length;
        Block[] blocks;
        int[] data;

        String version = nbt.getString("Version");

        width = nbt.getShort("Width");
        //height = nbt.getShort("Height");
        length = nbt.getShort("Length");

        NBTTagList list = nbt.getTagList("level_" + level, Constants.NBT.TAG_COMPOUND);

        switch (version) {
            case "1":   //Version 1 uses numeric IDs and should not be used
                System.out.println("Warning! A Building (" + nbt.getString("BuildingName") + ") is using an old mlplan file format (version 1). Update it!");

                String blockdata = list.getCompoundTagAt(0).getString("BlockData");
                height = list.getCompoundTagAt(0).getShort("Height");
                //System.out.println(blockdata);
                String[] split = blockdata.split(";");
                blocks = new Block[split.length];
                data = new int[split.length];
                for (int i = 0; i <= split.length - 1; i++) {
                    String s = split[i];
                    String[] s1 = s.split(":");
                    blocks[i] = Block.getBlockById(Integer.parseInt(s1[0]));
                    data[i] = Integer.parseInt(s1[1]);
                }
                break;
            case "2": //Version 2 uses string IDs
                NBTTagList rawNBTBlockData = list.getCompoundTagAt(0).getTagList("BlockData", Constants.NBT.TAG_STRING);

                ArrayList<String> blockData = new ArrayList<>();

                int i = 0;
                String block;
                while ((block = rawNBTBlockData.getStringTagAt(i)).length() > 0) {
                    blockData.add(block);
                    i++;
                }

                height = list.getCompoundTagAt(0).getShort("Height");
                //System.out.println(rawNBTBlockData);
                blocks = new Block[blockData.size()];
                data = new int[blockData.size()];
                i = 0;
                for (String s : blockData) {
                    String[] s1 = s.replace(";", "").split(":");
                    blocks[i] = Block.getBlockFromName(s1[0] + ":" + s1[1]);
                    data[i] = Integer.parseInt(s1[2]);
                    i++;
                }
                break;
            default:
                System.err.println("UNABLE TO LOAD BUILDING! UNKNOWN FORMAT VERSION " + version + "!");
                new Throwable().printStackTrace();
                return null;
        }

        IBlockState[] states = new IBlockState[width * length * height];

        //turn block ids and data into blockstates.
        for (int i = 0; i < states.length; i++) {
            states[i] = blocks[i].getStateFromMeta(data[i]);
        }

        //turn into a 3D block array for use with Building
        //in format [y][z][x]! IMPORTANT!
        IBlockState[][][] organized = new IBlockState[height][length][width];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    organized[y][z][x] = states[(y * length + z) * width + x];
                }
            }
        }

        //load milleniare extra data
        short depth = list.getCompoundTagAt(0).getShort("StartLevel");

        String name = nbt.getString("BuildingName");

        return new Building(culture, level)
                .setHeightDepth(height, depth).setDistance(0, 5).setOrientation(EnumFacing.EAST).setActualContents(organized).setLengthWidth(length, width)
                .setNameAndType(name, new String[]{}, new String[]{});
    }

    /**
     * Reads an NBT tag from a schematic file.
     * @param name The name of the building.
     * @param culture The name of the culture to which the building belongs.
     * @param packaged Whether the building is contained within our JAR file. If true it will be loaded as a resource,
     *                 otherwise from the exports folder.
     * @return The schematic NBT tag - BLANK, NOT NULL, if the tag could not be loaded.
     */
    public static NBTTagCompound getBuildingTag(final String name, MillCulture culture, final boolean packaged) {
        if (packaged) {
            InputStream x = MillCulture.class.getClassLoader().getResourceAsStream("assets/millenaire/cultures/" + culture.cultureName.toLowerCase() + "/buildings/" + name + ".mlplan");
            try {
                return CompressedStreamTools.readCompressed(x);
            } catch (IOException e) {
                e.printStackTrace();
                return new NBTTagCompound();
            }
        } else {
            try {
                File f1 = getBuildingFile(name);
                if (!f1.exists()) {
                    return new NBTTagCompound();
                } else {
                    FileInputStream fis = new FileInputStream(f1);
                    return CompressedStreamTools.readCompressed(fis);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return new NBTTagCompound();
            }
        }
    }

    /**
     * Gets the path to the file with the given name in the exports folder, creating the exports folder if it doesn't exist.
     * @param name The name of the file to find.
     * @return The file. May not exist.
     */
    public static File getBuildingFile(final String name) {
        File f = new File(MinecraftServer.getServer().getDataDirectory().getAbsolutePath() + File.separator + "millenaire" + File.separator + "exports" + File.separator);
        if (!f.exists()) {
            f.mkdirs();
        }

        return new File(f, name + ".mlplan");
    }

    /**
     * Checks that the given dimensions match those in the given NBT tag. Logs if this is not the case.
     * @param width The actual width
     * @param height The actual height
     * @param length The actual length
     * @param tag The schematic to check against.
     * @return True if ALL the dimensions match, else false.
     */
    private static boolean dimensionsMatch(short width, short height, short length, NBTTagCompound tag) {
        boolean valid = true;
        if (tag.getShort("Width") != width && tag.getShort("Width") != 0) {
            System.out.println("Width Mismatch: Expecting " + tag.getShort("Width") + ". Actual: " + width);
            valid = false;
        } else if (tag.getShort("Height") != height && tag.getShort("Height") != 0) {
            System.out.println("Height Mismatch: Expecting " + tag.getShort("Height") + ". Actual: " + height);
            valid = false;
        } else if (tag.getShort("Length") != length && tag.getShort("Length") != 0) {
            System.out.println("Length Mismatch: Expecting " + tag.getShort("Length") + ". Actual: " + length);
            valid = false;
        }
        return valid;
    }

    /**
     * Exports the IBlockState[y][z][x] to a file
     *
     * @param blocks the blocks to export
     * @param width  the width
     * @param height the height
     * @param length the length
     * @param depth  the depth of the building
     * @param name   the name of the building
     * @return the file that is saved to disk
     * @throws Exception If something goes wrong, such as a dimension mismatch
     */
    private static File exportToSchematic(IBlockState[][][] blocks, short width, short height, short length, short depth, String name, int level, EntityPlayer player) throws Exception {
        File f1 = getBuildingFile(name);

        NBTTagCompound tag = getBuildingTag(name, null, false);

        if (!dimensionsMatch(width, height, length, tag)) {
            PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.error.exporting.dimensions");
            Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
            return null;
        }

        String[] s = new String[width * height * length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    s[(y * length + z) * width + x] = blocks[y][z][x].getBlock().getRegistryName() + ":" + blocks[y][z][x].getBlock().getMetaFromState(blocks[y][z][x]) + ";";
                }
            }
        }

        NBTTagList blockdata = new NBTTagList();

        for (String s1 : s) {
            //blocklist.append(s1);
            blockdata.appendTag(new NBTTagString(s1));
        }

        NBTTagList LevelTagComp = new NBTTagList();
        NBTTagCompound tag2 = new NBTTagCompound();
        tag2.setTag("BlockData", blockdata);
        tag2.setShort("Height", height);
        tag2.setShort("StartLevel", depth);
        //tag2.setByteArray("Blocks", blockids);
        //tag2.setByteArray("Data", data);
        LevelTagComp.appendTag(tag2);

        tag.setTag("level_" + level, LevelTagComp);

        tag.setString("Version", FILE_VERSION);

        tag.setShort("Width", width);
        //tag.setShort("Height", height);
        tag.setShort("Length", length);
        //tag.setShort("StartLevel", depth);
        tag.setString("BuildingName", name);
        try {
            CompressedStreamTools.writeCompressed(tag, new FileOutputStream(f1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.completed");
        Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
        return f1;
    }
}
