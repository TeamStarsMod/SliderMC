package xyz.article.world;

import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.worldgen.PerlinNoise;
import xyz.article.api.world.worldgen.WorldGenerator;

public class OverWorldGenerator extends WorldGenerator {

    private final PerlinNoise perlinNoise; // 地形噪声
    private final PerlinNoise caveNoise;   // 矿洞噪声

    private final double terrainScale; // 地形缩放因子
    private final double caveScale;    // 矿洞缩放因子

    public OverWorldGenerator(long seed, double terrainScale, double caveScale) {
        super(12);
        this.perlinNoise = new PerlinNoise(seed, 2, 0.4); // 地形噪声
        this.caveNoise = new PerlinNoise(seed + 1, 1, 0.5); // 矿洞噪声
        this.terrainScale = terrainScale;
        this.caveScale = caveScale;
    }

    @Override
    public ChunkData generateChunk(ChunkPos pos) {
        ChunkData chunkData = new ChunkData(pos);

        for (int i = 0; i < 24; i++) {
            chunkData.getChunkSections()[i].getBiomeData().set(1, 1, 1, 1);
        }

        // 遍历区块中的每个x,z坐标
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 计算全局坐标
                int globalX = pos.pos().getX() * 16 + x;
                int globalZ = pos.pos().getY() * 16 + z;

                // 使用柏林噪声生成地形高度
                double noiseValue = perlinNoise.noise(globalX * terrainScale, 0, globalZ * terrainScale);
                int height = (int) (noiseValue * 32 + 64); // 将噪声值映射到高度范围

                // 填充方块
                for (int y = 0; y < 256; y++) {
                    if (y < height) {
                        // 地表部分使用草方块
                        if (y == height - 1) {
                            setBlock(chunkData.getChunkSections(), x, y, z, 9); // 草方块
                        }
                        // 地下部分使用石头方块
                        else {
                            // 使用三维噪声生成矿洞（应用缩放因子）
                            double caveNoiseValue = caveNoise.noise(globalX * caveScale, y * caveScale, globalZ * caveScale);
                            if (caveNoiseValue > 0.2) { // 矿洞阈值
                                setBlock(chunkData.getChunkSections(), x, y, z, 0); // 矿洞部分填充空气
                            } else {
                                setBlock(chunkData.getChunkSections(), x, y, z, 1); // 其他部分填充石头
                            }
                        }
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