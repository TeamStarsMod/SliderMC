package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.chunk.ChunkData;

import java.util.List;
import java.util.concurrent.*;

public class World {
    private static final Logger log = LoggerFactory.getLogger(World.class);
    private final Key key;
    private final ConcurrentHashMap<Vector2i, ChunkData> chunkDataMap;
    private final WorldTick worldTick;
    private final List<Player> players;

    private static final int TPS = 20; // 目标TPS
    private static final long TICK_INTERVAL = 1000 / TPS; // 每次 tick 的时间间隔(ms)
    private final ScheduledExecutorService scheduler;

    /**
     * 创建一个新世界
     * @param key 新世界标识符
     */
    public World(Key key) {
        this.key = key;
        this.chunkDataMap = new ConcurrentHashMap<>();
        this.worldTick = new WorldTick(this);
        this.players = new CopyOnWriteArrayList<>();

        log.info("正在初始化世界 {}", key);
        this.scheduler = Executors.newScheduledThreadPool(1); // 开启线程池处理Tick逻辑
        startTicking();
    }

    /**
     * 启动 Tick 循环
     */
    private void startTicking() {
        log.info("世界 {} Tick循环已开始", key);
        scheduler.scheduleAtFixedRate(worldTick::tick, 0, TICK_INTERVAL, TimeUnit.MILLISECONDS); // Tick逻辑在WorldTick类中
    }

    /**
     * 停止 Tick 循环
     */
    public void stopTicking() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        log.info("世界 {} 的Tick循环已停止", key);
    }

    /**
     * 获取世界标识符
     * @return 世界标识符
     */
    public Key getKey() {
        return key;
    }

    /**
     * 获取此世界的区块位置(Vector2i X Z)到区块的映射表
     * @return 区块映射表
     */
    public ConcurrentHashMap<Vector2i, ChunkData> getChunkDataMap() {
        return chunkDataMap;
    }

    /**
     * 获取此世界里的所有玩家
     * @return 玩家List
     */
    public List<Player> getPlayers() {
        return players;
    }
}
