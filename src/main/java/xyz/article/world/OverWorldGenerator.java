package xyz.article.world;

import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.worldgen.PerlinNoise;
import xyz.article.api.world.worldgen.WorldGenerator;

public class OverWorldGenerator extends WorldGenerator {
    private static final int WORLD_HEIGHT = 384; // -64 to 320
    private static final int SEA_LEVEL = 62;
    private static final int CHUNK_SIZE = 16;

    private final PerlinNoise noise;

    public OverWorldGenerator(long seed) {
        super(12);
        this.noise = new PerlinNoise(seed);
    }

    @Override
    public ChunkData generateChunk(ChunkPos pos) {
        ChunkSection[] chunkSections = new ChunkSection[24];
        for (int i = 0; i < 24; i++) {
            chunkSections[i] = new ChunkSection();
            chunkSections[i].getBiomeData().set(1,1,1,1);
        }

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int worldX = pos.pos().getX() * CHUNK_SIZE + x;
                int worldZ = pos.pos().getY() * CHUNK_SIZE + z;

                double height = noise.noise(worldX * 0.05, worldZ * 0.05) * 32 + SEA_LEVEL;

                for (int y = 0; y < WORLD_HEIGHT; y++) {
                    int sectionIndex = y / 16;
                    int sectionY = y % 16;

                    if (y < height) {
                        chunkSections[sectionIndex].setBlock(x, sectionY, z, 1); // 1 is stone
                    } else if (y < SEA_LEVEL) {
                        chunkSections[sectionIndex].setBlock(x, sectionY, z, 2); // 2 is water
                    } else {
                        chunkSections[sectionIndex].setBlock(x, sectionY, z, 0); // 0 is air
                    }
                }
            }
        }

        return new ChunkData(pos, chunkSections, NbtMap.EMPTY, new BlockEntityInfo[]{}, createLightUpdateData());
    }
}
