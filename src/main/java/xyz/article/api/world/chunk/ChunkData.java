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
     * @param chunkSections 子区块数组(大小24，对应-64到320y，共24个16x16x16的子区块)
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

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public ChunkSection[] getChunkSections() {
        return chunkSections;
    }

    public NbtMap getHeightMap() {
        return heightMap;
    }
    public void setHeightMap(NbtMap heightMap) {
        this.heightMap = heightMap;
    }

    public LightUpdateData getLightUpdateData() {
        return lightUpdateData;
    }
    public void setLightUpdateData(LightUpdateData lightUpdateData) {
        this.lightUpdateData = lightUpdateData;
    }

    public BlockEntityInfo[] getBlockEntityInfos() {
        return blockEntityInfos;
    }
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
