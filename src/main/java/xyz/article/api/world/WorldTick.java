package xyz.article.api.world;

import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;
import xyz.article.api.entities.player.Player;

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
