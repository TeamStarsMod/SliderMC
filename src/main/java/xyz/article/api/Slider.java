package xyz.article.api;

import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.network.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.article.MinecraftServer;
import xyz.article.RunningData;
import xyz.article.api.entities.player.Player;
import xyz.article.api.event.EventManager;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockPos;
import xyz.article.api.world.chunk.ChunkPos;

public class Slider {
    private final Server server = MinecraftServer.apiServer;
    /**
     * 获取玩家实例 (通过Session)
     * @param session 用于获取玩家的Session实例
     * @return 获取到的玩家实例
     */
    public static @Nullable Player getPlayer (Session session) {
        return RunningData.globalSessionPlayerMap.get(session);
    }

    /**
     * 获取玩家实例（通过玩家名或UUID）
     * @param name 玩家名或UUID
     * @return 获取到的玩家实例
     */
    public static @Nullable Player getPlayer (String name) {
        for (Player globalPlayer : RunningData.globalPlayers) {
            if (globalPlayer.getProfile().getName().equals(name)) return globalPlayer;
            else if (globalPlayer.getProfile().getId().toString().equals(name)) return globalPlayer;
        }
        return null;
    }

    public static @NotNull EventManager getEventManager () {
        return MinecraftServer.eventManager;
    }

    public static ChunkPos getChunkPos(int x, int z, World world) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        return new ChunkPos(world, Vector2i.from(chunkX, chunkZ));
    }

    public static ChunkPos getChunkPos(BlockPos blockPos) {
        int chunkX = blockPos.pos().getX() >> 4;
        int chunkZ = blockPos.pos().getZ() >> 4;
        return new ChunkPos(blockPos.world(), Vector2i.from(chunkX, chunkZ));
    }

    public static ChunkPos getChunkPos (Player player) {
        int chunkX = (int) player.getPosition().getX() >> 4;
        int chunkZ = (int) player.getPosition().getZ() >> 4;
        return new ChunkPos(player.getWorld(), Vector2i.from(chunkX, chunkZ));
    }

    public static int getChunkSectionIndex(int y) {
        int sectionHeight = 16;
        int worldBottom = -64;
        return (y - worldBottom) / sectionHeight;
    }

    public static Vector3i getInChunkLocation(int x, int y, int z){
        x = x & 15;
        y = y & 15;
        z = z & 15;
        return Vector3i.from(x, y, z);
    }

    public static Vector3i getInChunkSectionLocation(int x, int y, int z, int sectionIndex) {
        int sectionHeight = 16;
        int worldBottom = -64;
        int x1 = x & 15;
        int y1 = y - (sectionIndex * sectionHeight + worldBottom);
        int z1 = z & 15;
        return Vector3i.from(x1, y1, z1);
    }
}
