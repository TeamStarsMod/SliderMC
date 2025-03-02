package xyz.article.api.world.worldgen;

import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import org.geysermc.mcprotocollib.protocol.data.game.level.LightUpdateData;

import java.util.*;

public abstract class WorldGenerator {

    private final int PRE_WORLD_SIZE; // 12x12 chunks

    public WorldGenerator (int PRE_WORLD_SIZE) {
        this.PRE_WORLD_SIZE = PRE_WORLD_SIZE;
    }

    public abstract ChunkData generateChunk(ChunkPos pos);

    protected LightUpdateData createLightUpdateData() {
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

    public int getPRE_WORLD_SIZE() {
        return PRE_WORLD_SIZE;
    }
}