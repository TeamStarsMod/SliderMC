package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;
import xyz.article.RunningData;
import xyz.article.Settings;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * 用于控制世界的Tick逻辑
 */
public class WorldTick {
    private final World world;
    private int worldTime = 0;
    private int worldAge = 0;

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
        // 向所有此世界的玩家发送时间更新包
        for (Player player : world.getPlayers()) {
            player.sendPacket(new ClientboundSetTimePacket(worldAge, worldTime));
        }

        for (Player player : world.getPlayers()) {
            int viewDistance = Settings.VIEW_DISTANCE;
            if (Settings.SHOULD_SEND_MORE_VIEW_DATA) {
                viewDistance += 2; // 如果选项开启，则发送更多数据来让客户端看不到世界边缘，增强体验 (对性能有影响)
            }
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
