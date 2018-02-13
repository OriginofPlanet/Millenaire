package org.millenaire.building;

import net.minecraft.block.Block;
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
import org.millenaire.MillCulture;
import org.millenaire.Millenaire;
import org.millenaire.VillageGeography;
import org.millenaire.networking.PacketSayTranslatedMessage;

import java.io.*;
import java.util.*;

public class PlanIO
{

    private static final String FILE_VERSION = "2";

    //IBlockState[y][z][x]
    public static void exportBuilding(EntityPlayer player, BlockPos startPoint)
    {
        try
        {
            TileEntitySign sign = (TileEntitySign) player.getEntityWorld().getTileEntity(startPoint);

            String buildingName = sign.signText[0].getUnformattedText();
            boolean saveSnow = (sign.signText[3].getUnformattedText().toLowerCase().equals("snow"));

            int buildingLevel = 1;

            if (sign.signText[1] != null && sign.signText[1].getUnformattedText().length() > 0)
            {
                buildingLevel = Integer.parseInt(sign.signText[1].getUnformattedText());
            }

            if (buildingLevel < 0)
            {
                PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.error.exporting.level0");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
            }

            int startLevel = -1;

            if (sign.signText[2] != null && sign.signText[2].getUnformattedText().length() > 0)
            {
                startLevel = Integer.parseInt(sign.signText[2].getUnformattedText());
            }

            if (buildingName == null || buildingName.length() == 0)
            {
                PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.error.exporting.noname");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                throw new Exception("exporting.noname");
            }

            PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.export.start.read");
            Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);

            boolean foundEnd = false;
            int xEnd = startPoint.getX() + 1;
            while (xEnd < startPoint.getX() + 257)
            {
                final IBlockState block = player.getEntityWorld().getBlockState(new BlockPos(xEnd, startPoint.getY(), startPoint.getZ()));

                if (block.getBlock() == Blocks.standing_sign)
                {
                    foundEnd = true;
                    break;
                }
                xEnd++;
            }
            if (!foundEnd)
            {
                packet = new PacketSayTranslatedMessage("message.error.exporting.xaxis");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                throw new Exception("exporting.xaxis");
            }
            foundEnd = false;
            int zEnd = startPoint.getZ() + 1;
            while (zEnd < startPoint.getZ() + 257)
            {
                final IBlockState block = player.getEntityWorld().getBlockState(new BlockPos(startPoint.getX(), startPoint.getY(), zEnd));

                if (block.getBlock() == Blocks.standing_sign)
                {
                    foundEnd = true;
                    break;
                }
                zEnd++;
            }
            if (!foundEnd)
            {
                packet = new PacketSayTranslatedMessage("message.error.exporting.zaxis");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                throw new Exception("Ahhh!");
            }

            final int width = xEnd - startPoint.getX() - 1;
            final int length = zEnd - startPoint.getZ() - 1;

            boolean stop = false;
            int y = 0;

            final Map<Integer, IBlockState[][]> ex = new HashMap<>();

            while (!stop)
            {

                IBlockState[][] level = new IBlockState[width][length];

                boolean blockFound = false;

                for (int x = 0; x < width; x++)
                {
                    for (int z = 0; z < length; z++)
                    {
                        IBlockState block = player.getEntityWorld().getBlockState(new BlockPos(x + startPoint.getX() + 1, y + startPoint.getY() + startLevel, z + startPoint.getZ() + 1));

                        if (block.getBlock() != Blocks.air)
                        {
                            blockFound = true;
                        }
                        if (saveSnow || block.getBlock() != Blocks.snow)
                        {
                            level[x][z] = block;
                        } else
                        {
                            level[x][z] = Blocks.air.getDefaultState();
                        }
                    }
                }

                if (blockFound)
                {
                    ex.put(y, level);
                } else
                {
                    stop = true;
                }

                y++;

                if (y + startPoint.getY() + startLevel >= 256)
                {
                    stop = true;
                }
            }

            IBlockState[][][] ex2 = new IBlockState[ex.size()][length][width];

            for (int i = 0; i < ex.size(); i++)
            {
                IBlockState[][] level = ex.get(i);
                for (int x = 0; x < width; x++)
                {
                    for (int z = 0; z < length; z++)
                    {
                        ex2[i][z][x] = level[x][z];
                    }
                }
            }

            packet = new PacketSayTranslatedMessage("message.export.start.save");
            Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);

            exportToSchem(ex2, (short) width, (short) ex.size(), (short) length, (short) startLevel, buildingName, buildingLevel, player);

            packet = new PacketSayTranslatedMessage("message.export.finish");
            Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
        } catch (Exception e)
        {
            e.printStackTrace();
            PacketSayTranslatedMessage packet2 = new PacketSayTranslatedMessage("message.notcompleted");
            Millenaire.simpleNetworkWrapper.sendTo(packet2, (EntityPlayerMP) player);
        }
    }

    //Called only on the logical server
    public static void importBuilding(EntityPlayer player, BlockPos startPos)
    {
        try
        {
            TileEntitySign te = (TileEntitySign) player.getEntityWorld().getTileEntity(startPos);
            String name = te.signText[0].getUnformattedText();
            int level = 1;
            if (te.signText[1] != null && te.signText[1].getUnformattedText().length() > 0)
            {
                level = Integer.parseInt(te.signText[1].getUnformattedText());
            }

            if (name == null || name.length() == 0)
            {
                PacketSayTranslatedMessage message = new PacketSayTranslatedMessage("message.error.exporting.noname");
                Millenaire.simpleNetworkWrapper.sendTo(message, (EntityPlayerMP) player);
                PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.notcompleted");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                return;
            }

            World world = MinecraftServer.getServer().getEntityWorld();

            File schem = getBuildingFile(name);
            if (!schem.exists())
            {
                PacketSayTranslatedMessage message = new PacketSayTranslatedMessage("message.error.importing.nofile");
                Millenaire.simpleNetworkWrapper.sendTo(message, (EntityPlayerMP) player);
                PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.notcompleted");
                Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
                return;
            }
            FileInputStream fis = new FileInputStream(schem);

            BuildingPlan plan = loadSchematic(CompressedStreamTools.readCompressed(fis), MillCulture.normanCulture, level);

            placeBuilding(plan, new BuildingLocation(plan, startPos, EnumFacing.EAST), world);
        } catch (IOException e)
        {
            e.printStackTrace();
            PacketSayTranslatedMessage message = new PacketSayTranslatedMessage("message.error.unknown");
            Millenaire.simpleNetworkWrapper.sendTo(message, (EntityPlayerMP) player);
        }
    }

    public static void flattenTerrainForBuilding(BuildingPlan plan, BuildingLocation loc, VillageGeography geo)
    {
        //System.out.println("Flattening Terrain");
        int ylevel = loc.position.getY();

        World world = geo.world;

        int margin = 3; //TODO: Tweak?

        BlockPos corner1 = loc.position.subtract(new Vec3i(margin, 0, margin));
        BlockPos corner2 = loc.position.add(plan.width + margin, 0, plan.length + margin);

        for (int xPos = corner1.getX(); xPos <= corner2.getX(); xPos++)
        {
            for (int zPos = corner1.getZ(); zPos <= corner2.getZ(); zPos++)
            {
                BlockPos highestY = world.getTopSolidOrLiquidBlock(new BlockPos(xPos, ylevel, zPos));

                IBlockState topBlock = world.getBiomeGenForCoords(highestY).topBlock;
                IBlockState fillerBlock = world.getBiomeGenForCoords(highestY).fillerBlock.getBlock() == Blocks.sand ? Blocks.sandstone.getDefaultState() : world.getBiomeGenForCoords(highestY).fillerBlock;

                if (geo.buildingLoc[xPos - geo.mapStartX][zPos - geo.mapStartZ])
                {
                    //Skip flattening if it would overlap
                    continue;
                }

                //Find highest actual surface block, to see if we need to block up.
                Block b = world.getBlockState(highestY).getBlock();
                while (b == Blocks.water || b == Blocks.flowing_water || b == Blocks.leaves || b == Blocks.leaves2
                        || b == Blocks.log || b == Blocks.log2)
                {
                    highestY = highestY.subtract(new Vec3i(0, 1, 0));
                    b = world.getBlockState(highestY).getBlock();
                }

                //Do we need to block up? If so, do so.
                if (highestY.getY() < ylevel - 1)
                {
                    for (int yPos = highestY.getY(); yPos <= ylevel - 2; yPos++)
                    {
                        world.setBlockState(new BlockPos(xPos, yPos, zPos), fillerBlock);
                    }
                }

                //Now find the genuine highest block at this point and break down if we need to - e.g. to remove trees.
                highestY = world.getHeight(new BlockPos(xPos, ylevel, zPos));

                if (highestY.getY() > ylevel)
                {
                    for (int yPos = ylevel - 1; yPos <= highestY.getY(); yPos++)
                    {
                        world.setBlockState(new BlockPos(xPos, yPos, zPos), Blocks.air.getDefaultState());
                    }
                }

                //Finish the flattening for the actual level we're targeting
                world.setBlockState(new BlockPos(xPos, ylevel - 1, zPos), topBlock);

                //Ensure we have grass under anything we build to prevent gravity-affected blocks from falling.
                world.setBlockState(new BlockPos(xPos, ylevel + plan.depth - 1, zPos), fillerBlock);
            }

        }

    }

    public static void placeBuilding(BuildingPlan plan, BuildingLocation loc, World world)
    {
        IBlockState[][][] blocks = plan.buildingArray;

        //The list of blocks that must be placed last.
        //TODO: Any others? Any way to auto-detect?

        List<Block> blocksToPlaceLast = Arrays.asList(Blocks.torch, Blocks.redstone_torch, Blocks.vine, Blocks.bed, Blocks.wall_sign, Blocks.standing_sign,
                Blocks.standing_banner, Blocks.wall_banner);

        //Some blocks must be placed last in order to not drop onto the floor. This stores their locations.
        LinkedHashMap<BlockPos, IBlockState> placeLast = new LinkedHashMap<>();

        for (int x = 0; x < plan.width; x++)
        {
            for (int y = 0; y < plan.height; y++)
            {
                for (int z = 0; z < plan.length; z++)
                {
                    if (blocksToPlaceLast.contains(blocks[y][z][x].getBlock()))
                    {
                        placeLast.put(new BlockPos(x + loc.position.getX(), y + loc.position.getY() + plan.depth, z + loc.position.getZ()), blocks[y][z][x]);
                    } else
                    {
                        //System.out.println("Placing a " + blocks[y][z][x].getBlock());
                        world.setBlockState(new BlockPos(x + loc.position.getX(), y + loc.position.getY() + plan.depth, z + loc.position.getZ()), blocks[y][z][x], 2);
                    }
                }
            }
        }

        for (Map.Entry<BlockPos, IBlockState> entry : placeLast.entrySet())
        {
            //System.out.println("Placing a " + entry.getValue().getBlock());
            world.setBlockState(entry.getKey(), entry.getValue(), 2);
        }
    }

    public static BuildingPlan loadSchematic(NBTTagCompound nbt, MillCulture culture, int level)
    {
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

        switch (version)
        {
            case "1":   //Version 1 uses numeric IDs and should not be used
                System.out.println("Warning! A Building (" + nbt.getString("BuildingName") + ") is using an old mlplan file format (version 1). Update it!");

                String blockdata = list.getCompoundTagAt(0).getString("BlockData");
                height = list.getCompoundTagAt(0).getShort("Height");
                //System.out.println(blockdata);
                String[] split = blockdata.split(";");
                blocks = new Block[split.length];
                data = new int[split.length];
                for (int i = 0; i <= split.length - 1; i++)
                {
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
                while ((block = rawNBTBlockData.getStringTagAt(i)).length() > 0)
                {
                    blockData.add(block);
                    i++;
                }

                height = list.getCompoundTagAt(0).getShort("Height");
                //System.out.println(rawNBTBlockData);
                blocks = new Block[blockData.size()];
                data = new int[blockData.size()];
                i = 0;
                for (String s : blockData)
                {
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
        for (int i = 0; i < states.length; i++)
        {
            states[i] = blocks[i].getStateFromMeta(data[i]);
        }

        //turn into a 3D block array for use with BuildingPlan
        //in format [y][z][x]! IMPORTANT!
        IBlockState[][][] organized = new IBlockState[height][length][width];

        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int z = 0; z < length; z++)
                {
                    organized[y][z][x] = states[(y * length + z) * width + x];
                }
            }
        }

        //load milleniare extra data
        short depth = list.getCompoundTagAt(0).getShort("StartLevel");

        String name = nbt.getString("BuildingName");

        return new BuildingPlan(culture, level)
                .setHeightDepth(height, depth).setDistance(0, 5).setOrientation(EnumFacing.EAST).setPlan(organized).setLengthWidth(length, width)
                .setNameAndType(name, new String[]{}, new String[]{});
    }

    public static NBTTagCompound getBuildingTag(final String name, MillCulture culture, final boolean packaged)
    {
        if (packaged)
        {
            InputStream x = MillCulture.class.getClassLoader().getResourceAsStream("assets/millenaire/cultures/" + culture.cultureName.toLowerCase() + "/buildings/" + name + ".mlplan");
            try
            {
                return CompressedStreamTools.readCompressed(x);
            } catch (IOException e)
            {
                e.printStackTrace();
                return new NBTTagCompound();
            }
        } else
        {
            try
            {
                File f1 = getBuildingFile(name);
                if (!f1.exists())
                {
                    return new NBTTagCompound();
                } else
                {
                    FileInputStream fis = new FileInputStream(f1);
                    return CompressedStreamTools.readCompressed(fis);
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                return new NBTTagCompound();
            }
        }
    }

    public static File getBuildingFile(final String name)
    {
        File f = new File(MinecraftServer.getServer().getDataDirectory().getAbsolutePath() + File.separator + "millenaire" + File.separator + "exports" + File.separator);
        if (!f.exists())
        {
            f.mkdirs();
        }

        return new File(f, name + ".mlplan");
    }

    private static boolean valid(short width, short height, short length, short depth, NBTTagCompound tag)
    {
        boolean valid = true;
        if (tag.getShort("Width") != width && tag.getShort("Width") != 0)
        {
            System.out.println("Width: Expecting " + tag.getShort("Width") + ". Actual: " + width);
            valid = false;
        } else if (tag.getShort("Height") != height && tag.getShort("Height") != 0)
        {
            System.out.println("Height: Expecting " + tag.getShort("Height") + ". Actual: " + height);
            valid = false;
        } else if (tag.getShort("Length") != length && tag.getShort("Length") != 0)
        {
            System.out.println("Length: Expecting " + tag.getShort("Length") + ". Actual: " + length);
            valid = false;
        }
        return valid;
    }

    /**
     * Exports the IBlockState[y][z][x] to a file
     *
     * @param blocks the blocks to export
     * @param width  the width (x-axis)
     * @param height the height (y-axis)
     * @param length the length (z-axis)
     * @param depth  the depth of the build
     * @param name   the name of the building
     * @return the file that is outputted to disk
     * @throws Exception
     */
    private static File exportToSchem(IBlockState[][][] blocks, short width, short height, short length, short depth, String name, int level, EntityPlayer player) throws Exception
    {
        File f1 = getBuildingFile(name);

        NBTTagCompound tag = getBuildingTag(name, null, false);

        if (!valid(width, height, length, depth, tag))
        {
            PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.error.exporting.dimensions");
            Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
            throw new Exception("Ahhh!");
        }

        String[] s = new String[width * height * length];
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                for (int z = 0; z < length; z++)
                {
                    s[(y * length + z) * width + x] = blocks[y][z][x].getBlock().getRegistryName() + ":" + blocks[y][z][x].getBlock().getMetaFromState(blocks[y][z][x]) + ";";
                }
            }
        }

        NBTTagList blockdata = new NBTTagList();

        for (String s1 : s)
        {
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
        try
        {
            CompressedStreamTools.writeCompressed(tag, new FileOutputStream(f1));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        PacketSayTranslatedMessage packet = new PacketSayTranslatedMessage("message.completed");
        Millenaire.simpleNetworkWrapper.sendTo(packet, (EntityPlayerMP) player);
        return f1;
    }
}
