package xyz.article.world;

import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.worldgen.PerlinNoise;
import xyz.article.api.world.worldgen.WorldGenerator;

public class OverWorldGenerator extends WorldGenerator {

    private final PerlinNoise perlinNoise;

    public OverWorldGenerator(long seed) {
        super(12);
        this.perlinNoise = new PerlinNoise(seed);
    }

    @Override
    public ChunkData generateChunk(ChunkPos pos) {
        ChunkData chunkData = new ChunkData(pos);

        for (int i = 0; i < 24; i++) {
            chunkData.getChunkSections()[i].getBiomeData().set(1,1,1,1);
        }

        // 遍历区块中的每个x,z坐标
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 计算全局坐标
                int globalX = pos.pos().getX() * 16 + x;
                int globalZ = pos.pos().getY() * 16 + z;

                // 使用柏林噪声生成地形高度
                double noiseValue = perlinNoise.noise(globalX * 0.05, 0, globalZ * 0.05);
                int height = (int) (noiseValue * 32 + 64); // 将噪声值映射到高度范围

                // 填充方块
                for (int y = 0; y < 256; y++) {
                    if (y < height) {
                        setBlock(chunkData.getChunkSections(), x, y, z, 9); // 假设所有方块都是草方块
                    } else {
                        setBlock(chunkData.getChunkSections(), x, y, z, 0); // 其他部分填充空气
                    }
                }
            }
        }

        // 设置光照数据
        chunkData.setLightUpdateData(createLightUpdateData());

        return chunkData;
    }

    /**
     * 在区块中设置方块
     *
     * @param chunkSections 区块的 ChunkSection 数组
     * @param x             方块的 X 坐标
     * @param y             方块的 Y 坐标
     * @param z             方块的 Z 坐标
     * @param blockId       方块的 ID
     */
    private void setBlock(ChunkSection[] chunkSections, int x, int y, int z, int blockId) {
        if (x < 0 || x >= 16 || y < -63 || y >= 320 || z < 0 || z >= 16) return; // 确保坐标在区块范围内

        int sectionIndex = y / 16;
        int localY = y % 16;
        ChunkSection section = chunkSections[sectionIndex];
        if (section != null) {
            section.setBlock(x, localY, z, blockId);
        }
    }
}