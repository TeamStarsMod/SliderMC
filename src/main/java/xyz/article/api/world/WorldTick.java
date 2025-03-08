package xyz.article.api.world;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkBiomeData;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.SingletonPalette;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundChunksBiomesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;
import xyz.article.Settings;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 用于控制世界的Tick逻辑
 */
public class WorldTick {
    private final World world;
    private int worldTime = 0;
    private int worldAge = 0;
    private long timeSetCacheTime = 0;
    private long chunkSaveCacheTime = 0;

    public WorldTick(World world) {
        this.world = world;
    }

    /**
     * Tick逻辑
     */
    public void tick() {
        // 更新世界时间
        worldTime++;
        if (worldTime > 24000) {
            worldTime = 0;
            worldAge++;
        }
        // 每隔一分钟向所有此世界的玩家发送时间更新包
        if ((System.currentTimeMillis() - timeSetCacheTime) > 60000) {
            timeSetCacheTime = System.currentTimeMillis();

            for (Player player : world.getPlayers()) {
                player.sendPacket(new ClientboundSetTimePacket(worldAge, worldTime));
            }
        }

        // 每隔设定时间保存一次未加载区块
        if ((System.currentTimeMillis() - chunkSaveCacheTime) > (60000L * Settings.CHUNK_SAVE_TIME_MINUTE)) {
            chunkSaveCacheTime = System.currentTimeMillis();

            new Thread(() -> {
                Thread.currentThread().setName(world.getKey() + " Save Thread");
                world.saveAndUnloadUnusedChunks();
            }).start();
        }

        for (Player player : world.getPlayers()) {
            int viewDistance = Settings.VIEW_DISTANCE;
            long maxSquared = (long) viewDistance * viewDistance;

            ChunkPos playerChunkPos = Slider.getChunkPos(player);
            // 确保正确获取区块的x和z坐标
            int playerChunkX = playerChunkPos.pos().getX();
            int playerChunkZ = playerChunkPos.pos().getY();

            // 圆形遍历加载区块
            for (int x = playerChunkX - viewDistance; x <= playerChunkX + viewDistance; x++) {
                int dx = x - playerChunkX;
                long xSquared = (long) dx * dx;
                if (xSquared > maxSquared) continue;

                int maxDz = (int) Math.sqrt(maxSquared - xSquared);
                for (int z = playerChunkZ - maxDz; z <= playerChunkZ + maxDz; z++) {
                    Vector2i chunkKey = Vector2i.from(x, z);
                    ChunkData chunkData = null;
                    boolean notFound = true;
                    if (world.getChunkDataMap().containsKey(chunkKey)) {
                        chunkData = world.getChunkDataMap().get(chunkKey);
                        notFound = false;
                    }
                    if (notFound) {
                        try {
                            chunkData = world.getChunkFromSave(chunkKey);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (chunkData == null) {
                            chunkData = world.getGenerator().generateChunk(new ChunkPos(world, chunkKey));
                        }

                        world.getChunkDataMap().put(chunkKey, chunkData);
                    }
                    if (!player.getLoadedChunks().containsKey(chunkKey)) {
                        player.getLoadedChunks().put(chunkKey, chunkData);
                        player.sendPacket(chunkData.getPacket());
                    }
                }
            }

            // 卸载超出视距的区块
            Iterator<Map.Entry<Vector2i, ChunkData>> it = player.getLoadedChunks().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Vector2i, ChunkData> entry = it.next();
                Vector2i chunkPos = entry.getKey();
                int dx = chunkPos.getX() - playerChunkX;
                int dz = chunkPos.getY() - playerChunkZ; // Vector2i的y存储的是z坐标
                long squaredDistance = (long) dx * dx + (long) dz * dz;

                if (squaredDistance > maxSquared) {
                    player.sendPacket(new ClientboundForgetLevelChunkPacket(chunkPos.getX(), chunkPos.getY()));
                    it.remove();
                }
            }

            // 更新客户端中心区块
            ChunkPos currentChunk = new ChunkPos(player.getWorld(), Vector2i.from(playerChunkX, playerChunkZ));
            if (!currentChunk.equals(player.getLastChunkPos())) {
                player.setLastChunkPos(currentChunk);
                player.sendPacket(new ClientboundSetChunkCacheCenterPacket(playerChunkX, playerChunkZ));
            }
        }
    }

    /**
     * 设置世界的时间 (0-24000)
     * @param worldTime 要设置的世界时间
     */
    public void setWorldTime(int worldTime) {
        if (worldTime < 0 || worldTime > 24000) {
            throw new IllegalArgumentException("Unknown world time! need 0-24000 but received " + worldTime);
        }
        this.worldTime = worldTime;
    }

    /**
     * 获取世界的时间
     * @return 世界时间 (0-24000)
     */
    public int getWorldTime() {
        return worldTime;
    }

    /**
     * 获取世界年龄
     * @return 世界年龄
     */
    public int getWorldAge() {
        return worldAge;
    }
}
