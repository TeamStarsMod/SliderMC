package xyz.article.api.world.worldgen;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import xyz.article.RunningData;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.level.block.BlockEntityInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;

import java.util.*;

public class WorldGenerator {

    private static final int CHUNK_SIZE = 16;
    private static final int WORLD_SIZE = 12; // 12x12 chunks
    private static final int WORLD_HEIGHT = 384; // -64 to 320
    private static final int SEA_LEVEL = 62;

    private final PerlinNoise noise;

    public WorldGenerator(long seed) {
        Random random = new Random(seed);
        this.noise = new PerlinNoise(seed);
    }

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

    private LightUpdateData createLightUpdateData() {
        BitSet skyYMask = new BitSet(24);
        skyYMask.set(0, 24, true);

        List<byte[]> skyUpdates = new ArrayList<>(24);
        for (int y = 0; y < 24; y++) {
            byte[] layerData = new byte[2048];
            Arrays.fill(layerData, (byte) 0xFF);
            skyUpdates.add(layerData);
        }

        return new LightUpdateData(
                skyYMask,
                new BitSet(),
                new BitSet(),
                new BitSet(),
                skyUpdates,
                new ArrayList<>()
        );
    }

    public ChunkData[][] generateWorld() {
        ChunkData[][] world = new ChunkData[WORLD_SIZE][WORLD_SIZE];

        for (int x = -6; x < 6; x++) {
            for (int z = -6; z < 6; z++) {
                ChunkPos pos = new ChunkPos(RunningData.worldMap.get(Key.key("minecraft:overworld")), Vector2i.from(x, z));
                world[x+6][z+6] = generateChunk(pos);
            }
        }

        return world;
    }
}