package xyz.article.api.world.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Chunk数据
 */
public class ChunkData {
    private final ChunkPos chunkPos;
    private final ChunkSection[] chunkSections;
    private NbtMap heightMap;
    private BlockEntityInfo[] blockEntityInfos;
    private LightUpdateData lightUpdateData;

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
     * 构建一个空的新区块 (请注意，区块数据并不会自动添加到世界中，需要您手动添加)
     * 需要先将生物群系数据添加到此区块中，否则会导致游戏客户端崩溃
     * @param pos 区块坐标
     */
    public ChunkData(@NotNull ChunkPos pos) {
        ChunkSection[] chunkSections = new ChunkSection[24];
        for (int i = 0; i < 24; i++) {
            chunkSections[i] = new ChunkSection();
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
        ByteBuf byteBuf = Unpooled.buffer();
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
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
}
