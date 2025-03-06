package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundForgetLevelChunkPacket;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.Settings;
import xyz.article.api.Slider;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.worldgen.WorldGenerator;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class World {
    private static final Logger log = LoggerFactory.getLogger(World.class);
    private final Key key;
    private final ConcurrentHashMap<Vector2i, ChunkData> chunkDataMap;
    private final WorldTick worldTick;
    private final List<Player> players;
    private final WorldGenerator generator;

    private static final int TPS = 20; // 目标TPS
    private static final long TICK_INTERVAL = 1000 / TPS; // 每次 tick 的时间间隔(ms)
    private final ScheduledExecutorService scheduler;

    /**
     * 创建一个新世界
     * @param key 新世界标识符
     */
    public World(Key key, WorldGenerator generator) {
        this.key = key;
        this.chunkDataMap = new ConcurrentHashMap<>();
        this.worldTick = new WorldTick(this);
        this.players = new CopyOnWriteArrayList<>();
        this.generator = generator;

        log.info("正在初始化世界 {}", key);
        //preGenerationWorld(); //会导致存档问题
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
    private void stopTicking() {
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
     * 停止并保存此世界
     * @param dir 保存世界的文件夹
     */
    public void stop(File dir) {
        stopTicking();
        log.info("正在保存世界 {}", key);
        NbtMapBuilder nbtMapBuilder = NbtMap.builder();
        nbtMapBuilder.putInt("worldAge", worldTick.getWorldAge());
        nbtMapBuilder.putInt("worldTime", worldTick.getWorldTime());
        nbtMapBuilder.putString("worldKey", key.value());
        File chunksDir = new File(dir, "chunks");
        if (chunksDir.mkdir()) log.info("正在为世界 {} 创建区块文件夹", key);
        chunkDataMap.forEach((chunkPos, chunkData) -> {
            File chunkDataFile = new File(chunksDir, "chunk_" + chunkPos.getX() + "_" + chunkPos.getY() + ".slider");
            try {
                chunkData.serializeToFile(chunkDataFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("世界 {} 保存完成", key);
    }

    /**
     * 从存档中获取一个区块
     * @param pos 区块坐标
     * @return 区块数据
     */
    public @Nullable ChunkData getChunkFromSave(Vector2i pos) throws IOException {
        File chunksDir = new File("./" + Settings.SAVE_FOLDER + "/" + key.namespace() + "_" + key.value() + "/chunks");

        if (!chunksDir.exists() || !chunksDir.isDirectory()) {
            return null;
        }

        File[] files = chunksDir.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.getName().endsWith(".slider")) {
                String fileName = file.getName().replace(".slider", "");
                String[] part = fileName.split("_");

                if (part.length == 3 && Integer.parseInt(part[1]) == pos.getX() && Integer.parseInt(part[2]) == pos.getY()) {
                    return ChunkData.deserializeFromFile(file);
                }
            }
        }

        return null;
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

    public void preGenerationWorld () {
        int low = - (generator.getPRE_WORLD_SIZE() / 2);
        int high = generator.getPRE_WORLD_SIZE() / 2;
        for (int x = low; x < high; x++) {
            for (int z = low; z < high; z++) {
                Vector2i vector2i = Vector2i.from(x, z);
                ChunkPos pos = new ChunkPos(this, vector2i);
                chunkDataMap.put(vector2i, generator.generateChunk(pos));
            }
        }
    }

    public void generationWorldArea (Vector2i currentLocation, int radius) {
        double circumference = 2 * Math.PI * radius;
        int numPoints = (int) (circumference / 0.7);

        for (int i = 0; i < numPoints; i++) {
            double theta = (2 * Math.PI / numPoints) * i;
            int x = (int) (radius * Math.cos(theta));
            int y = (int) (radius * Math.sin(theta));
            Vector2i vector2i = Vector2i.from(currentLocation.getX() + x, currentLocation.getY() + y);
            if (!chunkDataMap.containsKey(vector2i)) chunkDataMap.put(vector2i, generator.generateChunk(new ChunkPos(this, vector2i)));
        }
    }

    public WorldGenerator getGenerator () {
        return generator;
    }
}
