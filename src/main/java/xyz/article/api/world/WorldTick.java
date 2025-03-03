package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;
import xyz.article.RunningData;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

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
            int viewDistance = player.getViewDistance(); // 获取玩家的视野距离
            ChunkPos playerChunkPos = Slider.getChunkPos(player); // 获取玩家所在的区块坐标
            int playerChunkX = playerChunkPos.pos().getX();
            int playerChunkZ = playerChunkPos.pos().getY();

            // 遍历圆形范围内的区块
            for (int x = playerChunkX - viewDistance; x <= playerChunkX + viewDistance; x++) {
                for (int z = playerChunkZ - viewDistance; z <= playerChunkZ + viewDistance; z++) {
                    // 计算区块与玩家区块的距离
                    double distance = Math.sqrt(Math.pow(x - playerChunkX, 2) + Math.pow(z - playerChunkZ, 2));
                    if (distance <= viewDistance) {
                        // 此时x和z是玩家所在视野范围内的一个区块，所有区块都会执行一次
                        Vector2i vector2i = Vector2i.from(x, z);
                        ChunkData chunkData = world.getChunkDataMap().get(vector2i);
                        if (chunkData == null) {
                            chunkData = world.getGenerator().generateChunk(new ChunkPos(RunningData.worldMap.get(Key.key("minecraft:overworld")), Vector2i.from(x, z)));
                            world.getChunkDataMap().put(Vector2i.from(x, z), chunkData);
                        }
                        if (!player.getLoadedChunks().containsKey(vector2i)) {
                            player.getLoadedChunks().put(vector2i, chunkData);
                            player.sendPacket(chunkData.getPacket());
                        }
                    }
                }
            }
            // 遍历玩家已加载的区块
            for (Iterator<Map.Entry<Vector2i, ChunkData>> it = player.getLoadedChunks().entrySet().iterator(); it.hasNext();) {
                Map.Entry<Vector2i, ChunkData> entry = it.next();
                Vector2i chunkPos = entry.getKey();

                // 计算区块与玩家区块的距离
                double distance = Math.sqrt(Math.pow(chunkPos.getX() - playerChunkX, 2) + Math.pow(chunkPos.getY() - playerChunkZ, 2));

                // 如果区块超出了视野范围
                if (distance > viewDistance) {
                    // 通知客户端卸载该区块
                    player.sendPacket(new ClientboundForgetLevelChunkPacket(chunkPos.getX(), chunkPos.getY()));

                    // 从玩家的已加载区块列表中移除
                    it.remove();
                }
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
}
