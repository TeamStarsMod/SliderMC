package xyz.article.api.world.chunk;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.nbt.*;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.codec.NbtComponentSerializer;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.*;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.RunningData;
import xyz.article.api.world.chunk.palette.PaletteID;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Chunk数据
 */
public class ChunkData {
    private static final Logger log = LoggerFactory.getLogger(ChunkData.class);
    private final ChunkPos chunkPos;
    private final ChunkSection[] chunkSections;
    private NbtMap heightMap;
    private BlockEntityInfo[] blockEntityInfos;
    private LightUpdateData lightUpdateData;

    private final ByteBuf byteBuf = Unpooled.buffer();
    private final MinecraftCodecHelper helper = new MinecraftCodecHelper();

    /**
     * 构建一个新区块 (请注意，区块数据并不会自动添加到世界中，需要您手动添加)
     * 子区块数组的索引0代表世界最底部的子区块，24代表世界最上方的子区块
     * @param pos 区块坐标
     * @param chunkSections 子区块数组 (大小24，对应-64到320y，共24个16x16x16的子区块)
     * @param heightMap 高度图数据
     * @param blockEntityInfos 方块实体数据
     * @param lightUpdateData 光照更新数据
     */
    public ChunkData(@NotNull ChunkPos pos, @NotNull ChunkSection[] chunkSections, @NotNull NbtMap heightMap, @NotNull BlockEntityInfo[] blockEntityInfos, @NotNull LightUpdateData lightUpdateData) {
        if (chunkSections.length != 24) {
            throw new IllegalArgumentException("ChunkSections数组的长度应该为24，但收到了 " + chunkSections.length + " ！");
        }
        this.chunkPos = pos;
        this.chunkSections = chunkSections;
        this.heightMap = heightMap;
        this.blockEntityInfos = blockEntityInfos;
        this.lightUpdateData = lightUpdateData;
    }

    /**
     * 构建一个空的新区块 (请注意，区块数据并不会自动添加到世界中，需要您手动添加)
     * 需要先将生物群系数据添加到此区块中，否则会导致游戏客户端崩溃
     * @param pos 区块坐标
     */
    public ChunkData(@NotNull ChunkPos pos) {
        ChunkSection[] chunkSections = new ChunkSection[24];
        for (int i = 0; i < 24; i++) {
            chunkSections[i] = new ChunkSection(0, DataPalette.createForChunk(), new DataPalette(GlobalPalette.INSTANCE, new BitStorage(16, 16 * 16 * 16), PaletteType.BIOME));
        }

        this.chunkPos = pos;
        this.chunkSections = chunkSections;
        this.heightMap = NbtMap.EMPTY;
        this.blockEntityInfos = new BlockEntityInfo[]{};
        // 天空亮度15
        // 创建标记所有 Y 层需要天空光照更新的 BitSet
        BitSet skyYMask = new BitSet(24);
        skyYMask.set(0, 24, true); // 标记所有 Y 层

        List<byte[]> skyUpdates = new ArrayList<>(24);
        for (int y = 0; y < 24; y++) {
            byte[] layerData = new byte[2048];
            Arrays.fill(layerData, (byte) 0xFF);
            skyUpdates.add(layerData);
        }

        this.lightUpdateData = new LightUpdateData(
                skyYMask, // 标记需要更新的 Y 层
                new BitSet(), // 方块光照 Y 层掩码
                new BitSet(), // 空天空光照 Y 层掩码
                new BitSet(), // 空方块光照 Y 层掩码
                skyUpdates,   // 天空光照数据
                new ArrayList<>() // 方块光照数据
        );
    }

    /**
     * 构建一个仅包含子区块数据的区块 (请注意，区块数据并不会自动添加到世界中，需要您手动添加)
     * @param pos 区块坐标
     * @param chunkSections 子区块数组 (大小24，对应-64到320y，共24个16x16x16的子区块)
     */
    public ChunkData(@NotNull ChunkPos pos, @NotNull ChunkSection[] chunkSections) {
        if (chunkSections.length != 24) {
            throw new IllegalArgumentException("ChunkSections数组的长度应该为24，但收到了 " + chunkSections.length + " ！");
        }
        this.chunkPos = pos;
        this.chunkSections = chunkSections;
        this.heightMap = NbtMap.EMPTY;
        this.blockEntityInfos = new BlockEntityInfo[]{};
        // 天空亮度15
        // 创建标记所有 Y 层需要天空光照更新的 BitSet
        BitSet skyYMask = new BitSet(24);
        skyYMask.set(0, 24, true); // 标记所有 Y 层

        List<byte[]> skyUpdates = new ArrayList<>(24);
        for (int y = 0; y < 24; y++) {
            byte[] layerData = new byte[2048];
            Arrays.fill(layerData, (byte) 0xFF);
            skyUpdates.add(layerData);
        }

        this.lightUpdateData = new LightUpdateData(
                skyYMask, // 标记需要更新的 Y 层
                new BitSet(), // 方块光照 Y 层掩码
                new BitSet(), // 空天空光照 Y 层掩码
                new BitSet(), // 空方块光照 Y 层掩码
                skyUpdates,   // 天空光照数据
                new ArrayList<>() // 方块光照数据
        );
    }

    /**
     * 获取区块的位置
     * @return 区块位置
     */
    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    /**
     * 获取子区块数组
     * @return 子区块数组
     */
    public ChunkSection[] getChunkSections() {
        return chunkSections;
    }

    /**
     * 获取高度图
     * @return 高度图NbtMap数据
     */
    public NbtMap getHeightMap() {
        return heightMap;
    }

    /**
     * 设置高度图数据
     * @param heightMap 高度图数据
     */
    public void setHeightMap(NbtMap heightMap) {
        this.heightMap = heightMap;
    }

    /**
     * 获取光照更新数据
     * @return 光照更新数据
     */
    public LightUpdateData getLightUpdateData() {
        return lightUpdateData;
    }

    /**
     * 设置光照更新数据
     * @param lightUpdateData 光照更新数据
     */
    public void setLightUpdateData(LightUpdateData lightUpdateData) {
        this.lightUpdateData = lightUpdateData;
    }

    /**
     * 获取方块实体数组
     * @return 方块实体数组
     */
    public BlockEntityInfo[] getBlockEntityInfos() {
        return blockEntityInfos;
    }

    /**
     * 设置方块实体数组
     * @param blockEntityInfos 方块实体数组
     */
    public void setBlockEntityInfos(BlockEntityInfo[] blockEntityInfos) {
        this.blockEntityInfos = blockEntityInfos;
    }

    /**
     * 获取此区块的区块数据包
     * @return 区块数据包 (ClientboundLevelChunkWithLightPacket)
     */
    public ClientboundLevelChunkWithLightPacket getPacket() {
        byteBuf.clear();
        for (int i = 0; i < 24; i++) {
            helper.writeChunkSection(byteBuf, chunkSections[i]);
        }
        return new ClientboundLevelChunkWithLightPacket(
                chunkPos.pos().getX(),
                chunkPos.pos().getY(),
                byteBuf.array(),
                heightMap,
                blockEntityInfos,
                lightUpdateData
        );
    }

    /**
     * 将 ChunkData 序列化为 NBT 并写入文件
     * @param file 目标文件
     * @throws IOException 如果写入文件时发生错误
     */
    public void serializeToFile(File file) throws IOException {
        NbtMapBuilder nbtBuilder = NbtMap.builder();

        // 序列化 chunkPos
        nbtBuilder.putCompound("chunkPos", NbtMap.builder()
                .putString("world", chunkPos.world().getKey().toString())
                .putInt("x", chunkPos.pos().getX())
                .putInt("z", chunkPos.pos().getY())
                .build());

        // 序列化 chunkSections
        List<NbtMap> chunkSectionsList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            ChunkSection chunkSection = chunkSections[i];
            NbtMapBuilder sectionBuilder = NbtMap.builder();

            // 序列化方块数据
            DataPalette chunkDataPalette = chunkSection.getChunkData();
            NbtMapBuilder chunkDataBuilder = NbtMap.builder();
            chunkDataBuilder.putInt("bitsPerEntry", chunkDataPalette.getStorage().getBitsPerEntry());
            chunkDataBuilder.putLongArray("chunkData", chunkDataPalette.getStorage().getData());
            chunkDataBuilder.putInt("paletteType", getPaletteTypeNumber(chunkDataPalette.getPalette()));
            sectionBuilder.putCompound("chunkDataPalette", chunkDataBuilder.build());

            // 序列化生物群系数据
            DataPalette biomeDataPalette = chunkSection.getBiomeData();
            NbtMapBuilder biomeDataBuilder = NbtMap.builder();
            biomeDataBuilder.putInt("bitsPerEntry", biomeDataPalette.getStorage().getBitsPerEntry());
            biomeDataBuilder.putLongArray("biomeData", biomeDataPalette.getStorage().getData());
            biomeDataBuilder.putInt("paletteType", getPaletteTypeNumber(biomeDataPalette.getPalette()));
            sectionBuilder.putCompound("biomeDataPalette", biomeDataBuilder.build());

            sectionBuilder.putInt("blockCount", chunkSection.getBlockCount());
            chunkSectionsList.add(sectionBuilder.build());
        }
        nbtBuilder.putList("chunkSections", NbtType.COMPOUND, chunkSectionsList);

        // 序列化 heightMap
        nbtBuilder.putCompound("heightMap", heightMap);

        // 序列化 blockEntityInfos
        List<NbtMap> blockEntitiesList = new ArrayList<>();
        for (BlockEntityInfo blockEntity : blockEntityInfos) {
            NbtMapBuilder blockEntityBuilder = NbtMap.builder();
            blockEntityBuilder.putInt("x", blockEntity.getX());
            blockEntityBuilder.putInt("y", blockEntity.getY());
            blockEntityBuilder.putInt("z", blockEntity.getZ());
            blockEntityBuilder.putString("type", blockEntity.getType().name());
            blockEntityBuilder.putCompound("nbt", blockEntity.getNbt());
            blockEntitiesList.add(blockEntityBuilder.build());
        }
        nbtBuilder.putList("blockEntityInfos", NbtType.COMPOUND, blockEntitiesList);

        // 序列化 lightUpdateData
        NbtMapBuilder lightUpdateDataBuilder = NbtMap.builder();
        lightUpdateDataBuilder.putList("skyUpdates", NbtType.BYTE_ARRAY, lightUpdateData.getSkyUpdates());
        lightUpdateDataBuilder.putList("blockUpdates", NbtType.BYTE_ARRAY, lightUpdateData.getBlockUpdates());
        lightUpdateDataBuilder.putInt("skyYMaskSize", lightUpdateData.getSkyYMask().size());
        lightUpdateDataBuilder.putList("skyYMask", NbtType.BYTE_ARRAY, lightUpdateData.getSkyYMask().toByteArray());
        lightUpdateDataBuilder.putInt("blockYMaskSize", lightUpdateData.getBlockYMask().size());
        lightUpdateDataBuilder.putList("blockYMask", NbtType.BYTE_ARRAY, lightUpdateData.getBlockYMask().toByteArray());
        lightUpdateDataBuilder.putInt("emptySkyYMaskSize", lightUpdateData.getEmptySkyYMask().size());
        lightUpdateDataBuilder.putList("emptySkyYMask", NbtType.BYTE_ARRAY, lightUpdateData.getEmptySkyYMask().toByteArray());
        lightUpdateDataBuilder.putInt("emptyBlockYMaskSize", lightUpdateData.getEmptyBlockYMask().size());
        lightUpdateDataBuilder.putList("emptyBlockYMask", NbtType.BYTE_ARRAY, lightUpdateData.getEmptyBlockYMask().toByteArray());
        nbtBuilder.putCompound("lightUpdateData", lightUpdateDataBuilder.build());

        // 将 NBT 数据写入文件
        try (FileOutputStream fos = new FileOutputStream(file)) {
            NbtMap nbt = nbtBuilder.build();
            NBTOutputStream nbtOutputStream = NbtUtils.createWriter(fos);
            nbtOutputStream.writeValue(nbt);
            nbtOutputStream.close();
            fos.flush();
        } catch (Exception e) {
            log.error(e.toString());
        }
    }

    /**
     * 从文件中读取 NBT 数据并反序列化为 ChunkData
     * @param file 源文件
     * @return 反序列化后的 ChunkData 对象
     * @throws IOException 如果读取文件时发生错误
     */
    public static ChunkData deserializeFromFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            NBTInputStream nbtInputStream = NbtUtils.createReader(fis);
            NbtMap nbt = nbtInputStream.readValue(NbtType.COMPOUND);
            return fromNbt(nbt);
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }

    /**
     * 从 NbtMap 中提取数据并重建 ChunkData
     * @param nbt NbtMap 数据
     * @return 重建后的 ChunkData 对象
     */
    public static ChunkData fromNbt(NbtMap nbt) {
        // 反序列化 chunkPos
        // 忽略这个警告
        NbtMap chunkPosNbt = nbt.getCompound("chunkPos");
        ChunkPos chunkPos = new ChunkPos(RunningData.worldMap.get(Key.key(chunkPosNbt.getString("world"))), Vector2i.from(chunkPosNbt.getInt("x"), chunkPosNbt.getInt("z")));

        // 反序列化 chunkSections
        List<NbtMap> chunkSectionsList = nbt.getList("chunkSections", NbtType.COMPOUND);
        ChunkSection[] chunkSections = new ChunkSection[24];
        for (int i = 0; i < chunkSectionsList.size(); i++) {
            NbtMap sectionNbt = chunkSectionsList.get(i);

            // 反序列化方块数据
            NbtMap chunkDataNbt = sectionNbt.getCompound("chunkDataPalette");
            int chunkDataBitsPerEntry = chunkDataNbt.getInt("bitsPerEntry");
            long[] chunkDataData = chunkDataNbt.getLongArray("chunkData");

            DataPalette chunkDataPalette = PaletteID.getPaletteFromID(chunkDataNbt.getInt("paletteType"), new BitStorage(chunkDataBitsPerEntry, 16 * 16 * 16, chunkDataData), PaletteType.CHUNK);

            // 反序列化生物群系数据
            NbtMap biomeDataNbt = sectionNbt.getCompound("biomeDataPalette");
            int biomeDataBitsPerEntry = biomeDataNbt.getInt("bitsPerEntry");
            long[] biomeDataData = biomeDataNbt.getLongArray("biomeData");
            DataPalette biomeDataPalette = PaletteID.getPaletteFromID(biomeDataNbt.getInt("paletteType"), new BitStorage(biomeDataBitsPerEntry, 16 * 16 * 16, biomeDataData), PaletteType.BIOME);

            // 创建 ChunkSection
            int blockCount = sectionNbt.getInt("blockCount");
            chunkSections[i] = new ChunkSection(blockCount, chunkDataPalette, biomeDataPalette);
        }

        // 反序列化 heightMap
        NbtMap heightMap = nbt.getCompound("heightMap");

        // 反序列化 blockEntityInfos
        List<NbtMap> blockEntitiesList = nbt.getList("blockEntityInfos", NbtType.COMPOUND);
        BlockEntityInfo[] blockEntityInfos = new BlockEntityInfo[blockEntitiesList.size()];
        for (int i = 0; i < blockEntitiesList.size(); i++) {
            NbtMap blockEntityNbt = blockEntitiesList.get(i);
            int x = blockEntityNbt.getInt("x");
            int y = blockEntityNbt.getInt("y");
            int z = blockEntityNbt.getInt("z");
            String type = blockEntityNbt.getString("type");
            NbtMap blockEntityData = blockEntityNbt.getCompound("nbt");
            blockEntityInfos[i] = new BlockEntityInfo(x, y, z, BlockEntityType.valueOf(type), blockEntityData);
        }

        // 反序列化 lightUpdateData
        NbtMap lightUpdateDataNbt = nbt.getCompound("lightUpdateData");
        List<byte[]> skyUpdates = lightUpdateDataNbt.getList("skyUpdates", NbtType.BYTE_ARRAY);
        List<byte[]> blockUpdates = lightUpdateDataNbt.getList("blockUpdates", NbtType.BYTE_ARRAY);
        BitSet skyYMask = BitSet.valueOf(lightUpdateDataNbt.getByteArray("skyYMask"));
        BitSet blockYMask = BitSet.valueOf(lightUpdateDataNbt.getByteArray("blockYMask"));
        BitSet emptySkyYMask = BitSet.valueOf(lightUpdateDataNbt.getByteArray("emptySkyYMask"));
        BitSet emptyBlockYMask = BitSet.valueOf(lightUpdateDataNbt.getByteArray("emptyBlockYMask"));
        LightUpdateData lightUpdateData = new LightUpdateData(
                skyYMask, blockYMask, emptySkyYMask, emptyBlockYMask,
                skyUpdates, blockUpdates
        );

        // 创建并返回 ChunkData 对象
        return new ChunkData(chunkPos, chunkSections, heightMap, blockEntityInfos, lightUpdateData);
    }

    private static int getPaletteTypeNumber(Palette palette) {
        if (palette instanceof GlobalPalette) {
            return 0;
        } else if (palette instanceof ListPalette) {
            return 1;
        } else if (palette instanceof MapPalette) {
            return 2;
        } else if (palette instanceof SingletonPalette) {
            return 3;
        }

        return -1;
    }
}
