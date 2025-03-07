package xyz.article.api.world;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.nbt.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.Settings;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.world.worldgen.WorldGenerator;

import java.io.File;
import java.io.FileOutputStream;
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
        try {
            stopTicking();
            log.info("正在保存世界 {}", key);
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

            NbtMapBuilder nbtMapBuilder = NbtMap.builder();
            nbtMapBuilder.putInt("worldAge", worldTick.getWorldAge());
            nbtMapBuilder.putInt("worldTime", worldTick.getWorldTime());
            nbtMapBuilder.putString("worldKey", key.value());
            File worldSave = new File(dir, "world.slider");
            if (worldSave.createNewFile()) log.info("正在创建世界存档文件");
            // 将 NBT 数据写入文件
            try (FileOutputStream fos = new FileOutputStream(worldSave)) {
                NbtMap nbt = nbtMapBuilder.build();
                NBTOutputStream nbtOutputStream = NbtUtils.createWriter(fos);
                nbtOutputStream.writeValue(nbt);
                nbtOutputStream.close();
                fos.flush();
            } catch (Exception e) {
                log.error(e.toString());
            }
        } catch (IOException e) {
            log.info("在保存世界 {} 时发生错误！{}", key, e.toString());
        }
    }

    /**
     * 从存档中获取一个区块
     * @param pos 区块坐标
     * @return 区块数据
     */
    public @Nullable ChunkData getChunkFromSave(Vector2i pos) throws IOException {
        File chunksDir = new File("./" + Settings.SAVE_FOLDER + "/worlds/" + key.namespace() + "_" + key.value() + "/chunks");
        if (!chunksDir.exists() || !chunksDir.isDirectory()) {
            return null;
        }

        File file = new File(chunksDir, "chunk_" + pos.getX() + "_" + pos.getY() + ".slider");
        if (file.exists()) {
            return ChunkData.deserializeFromFile(file);
        } else {
            return null;
        }
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

    /**
     * 未完成
     * 从世界存档NBT文件中获取世界
     * @param worldSaveFile 世界存档文件 (world.slider)
     * @return 获取到的世界 (可能发生错误并返回null)
     */
    @Deprecated
    public static @Nullable World getWorldFromSave(File worldSaveFile) {
        // TODO: 由于无法获取自定义世界生成器的种子与参数，且无法确定Generator的实例，未制作，请完成此方法的功能

        /*try (FileInputStream fis = new FileInputStream(worldSaveFile)) {
            NBTInputStream nbtInputStream = NbtUtils.createReader(fis);
            NbtMap nbt = nbtInputStream.readValue(NbtType.COMPOUND);
            int worldAge = nbt.getInt("worldAge");
            int worldTime = nbt.getInt("worldTime");
            Key worldKey = Key.key(nbt.getString("worldKey"));
            return new World(worldKey, new OverWorldGenerator(114514L));
        } catch (Exception e) {
            log.error("发生错误！{}", e.toString());
            return null;
        }*/

        return null;
    }

    public int getWorldTime() {
        return worldTick.getWorldTime();
    }

    public int getWorldAge() {
        return worldTick.getWorldAge();
    }

    /**
     * 保存并卸载未被任何玩家加载的区块
     */
    public void saveAndUnloadUnusedChunks() {
        File worldDir = new File(Settings.SAVE_FOLDER, "worlds/" + key.namespace() + "_" + key.value());
        File chunksDir = new File(worldDir, "chunks");

        // 确保区块目录存在
        if (!chunksDir.exists() && !chunksDir.mkdirs()) {
            log.error("无法创建区块目录: {}", chunksDir.getAbsolutePath());
            return;
        }

        List<Vector2i> toRemove = new ArrayList<>();

        // 遍历所有已加载的区块
        chunkDataMap.forEach((pos, chunk) -> {
            boolean isUsed = false;

            // 检查是否有玩家加载了这个区块
            for (Player player : players) {
                if (player.getLoadedChunks().containsKey(pos)) {
                    isUsed = true;
                    break;
                }
            }

            // 如果没有玩家使用则保存并标记移除
            if (!isUsed) {
                File chunkFile = new File(chunksDir, "chunk_" + pos.getX() + "_" + pos.getY() + ".slider");
                try {
                    chunk.serializeToFile(chunkFile);
                    toRemove.add(pos);
                    log.debug("已保存并卸载区块 ({}, {})", pos.getX(), pos.getY());
                } catch (IOException e) {
                    log.error("保存区块 ({}, {}) 失败: {}", pos.getX(), pos.getY(), e.getMessage());
                }
            }
        });

        // 从内存中移除未使用的区块
        toRemove.forEach(chunkDataMap::remove);

        if (!toRemove.isEmpty()) {
            log.info("世界 {} 已保存并卸载 {} 个未使用区块", key, toRemove.size());
        }
    }
}
