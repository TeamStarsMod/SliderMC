package xyz.article.api.world;

import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundLevelChunkWithLightPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.api.Slider;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 区块加载管理器
 * 负责优化区块加载，避免一次性加载过多区块导致卡顿
 */
public class ChunkLoadingManager {
    private static final Logger log = LoggerFactory.getLogger(ChunkLoadingManager.class);
    
    // 每个玩家的区块加载队列
    private final Map<Player, ChunkLoadingTask> playerLoadingTasks = new ConcurrentHashMap<>();
    
    // 区块加载配置
    private static final int CHUNKS_PER_TICK = 2; // 每tick加载的区块数量
    private static final int PRIORITY_CHUNKS_PER_TICK = 4; // 优先区块每tick加载数量
    
    /**
     * 开始为玩家加载区块
     */
    public void startChunkLoading(Player player, int viewDistance) {
        ChunkLoadingTask task = new ChunkLoadingTask(player, viewDistance);
        playerLoadingTasks.put(player, task);
        log.debug("开始为玩家 {} 加载区块，视距: {}", player.getProfile().getName(), viewDistance);
    }
    
    /**
     * 停止玩家的区块加载
     */
    public void stopChunkLoading(Player player) {
        playerLoadingTasks.remove(player);
        log.debug("停止为玩家 {} 加载区块", player.getProfile().getName());
    }
    
    /**
     * 处理区块加载（每tick调用）
     */
    public void processChunkLoading() {
        Iterator<Map.Entry<Player, ChunkLoadingTask>> iterator = playerLoadingTasks.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<Player, ChunkLoadingTask> entry = iterator.next();
            Player player = entry.getKey();
            ChunkLoadingTask task = entry.getValue();
            
            // 检查玩家是否还在线
            if (!player.getSession().isConnected()) {
                iterator.remove();
                continue;
            }
            
            // 处理区块加载
            if (task.processChunkLoading()) {
                // 加载完成，移除任务
                iterator.remove();
                log.debug("玩家 {} 的区块加载完成", player.getProfile().getName());
            }
        }
    }
    
    /**
     * 区块加载任务
     */
    private static class ChunkLoadingTask {
        private final Player player;
        private final int viewDistance;
        private final Queue<Vector2i> chunkQueue = new ConcurrentLinkedQueue<>();
        private final Set<Vector2i> loadedChunks = new HashSet<>();
        private final Set<Vector2i> priorityChunks = new HashSet<>();
        private boolean initialized = false;
        
        public ChunkLoadingTask(Player player, int viewDistance) {
            this.player = player;
            this.viewDistance = viewDistance;
            initializeChunkQueue();
        }
        
        /**
         * 初始化区块队列
         */
        private void initializeChunkQueue() {
            ChunkPos playerChunkPos = Slider.getChunkPos(player);
            int playerChunkX = playerChunkPos.pos().getX();
            int playerChunkZ = playerChunkPos.pos().getY();
            long maxSquared = (long) viewDistance * viewDistance;
            
            // 生成区块加载队列，按距离排序
            List<Vector2i> chunks = new ArrayList<>();
            
            for (int x = playerChunkX - viewDistance; x <= playerChunkX + viewDistance; x++) {
                int dx = x - playerChunkX;
                long xSquared = (long) dx * dx;
                if (xSquared > maxSquared) continue;
                
                int maxDz = (int) Math.sqrt(maxSquared - xSquared);
                for (int z = playerChunkZ - maxDz; z <= playerChunkZ + maxDz; z++) {
                    Vector2i chunkPos = Vector2i.from(x, z);
                    chunks.add(chunkPos);
                    
                    // 标记优先区块（玩家周围的区块）
                    long distance = (long) dx * dx + (long) (z - playerChunkZ) * (z - playerChunkZ);
                    if (distance <= 4) { // 2x2区块范围
                        priorityChunks.add(chunkPos);
                    }
                }
            }
            
            // 按距离排序，优先加载近的区块
            chunks.sort((a, b) -> {
                long distA = getSquaredDistance(a, playerChunkX, playerChunkZ);
                long distB = getSquaredDistance(b, playerChunkX, playerChunkZ);
                return Long.compare(distA, distB);
            });
            
            chunkQueue.addAll(chunks);
            initialized = true;
        }
        
        /**
         * 处理区块加载
         * @return 是否完成加载
         */
        public boolean processChunkLoading() {
            if (!initialized) {
                return false;
            }
            
            int chunksThisTick = 0;
            int priorityChunksThisTick = 0;
            
            // 优先处理优先区块
            Iterator<Vector2i> priorityIterator = priorityChunks.iterator();
            while (priorityIterator.hasNext() && priorityChunksThisTick < PRIORITY_CHUNKS_PER_TICK) {
                Vector2i chunkPos = priorityIterator.next();
                if (loadChunk(chunkPos)) {
                    priorityIterator.remove();
                    priorityChunksThisTick++;
                }
            }
            
            // 处理普通区块
            while (!chunkQueue.isEmpty() && chunksThisTick < CHUNKS_PER_TICK) {
                Vector2i chunkPos = chunkQueue.poll();
                if (loadChunk(chunkPos)) {
                    chunksThisTick++;
                }
            }
            
            // 检查是否完成
            return chunkQueue.isEmpty() && priorityChunks.isEmpty();
        }
        
        /**
         * 加载单个区块
         */
        private boolean loadChunk(Vector2i chunkPos) {
            if (loadedChunks.contains(chunkPos)) {
                return true;
            }
            
            World world = player.getWorld();
            ChunkData chunkData = null;
            
            // 检查内存中是否已有区块
            if (world.getChunkDataMap().containsKey(chunkPos)) {
                chunkData = world.getChunkDataMap().get(chunkPos);
            } else {
                // 使用优化的缓存系统加载区块
                try {
                    // 优先使用异步加载，如果失败则回退到同步加载
                    CompletableFuture<ChunkData> future = world.getChunkAsync(chunkPos);
                    
                    // 设置超时时间，避免长时间等待
                    chunkData = future.get(2, TimeUnit.SECONDS);
                    
                    if (chunkData == null) {
                        // 异步加载失败，尝试同步加载
                        chunkData = world.getChunk(chunkPos);
                    }
                } catch (Exception e) {
                    log.error("加载区块 ({}, {}) 失败: {}", chunkPos.getX(), chunkPos.getY(), e.getMessage());
                    return false;
                }
            }
            
            if (chunkData != null) {
                // 发送区块数据包给玩家
                player.getLoadedChunks().put(chunkPos, chunkData);
                player.sendPacket(chunkData.getPacket());
                loadedChunks.add(chunkPos);
                
                log.debug("为玩家 {} 加载区块 ({}, {})", 
                    player.getProfile().getName(), chunkPos.getX(), chunkPos.getY());
                return true;
            }
            
            return false;
        }
        
        /**
         * 计算区块到玩家的平方距离
         */
        private long getSquaredDistance(Vector2i chunkPos, int playerChunkX, int playerChunkZ) {
            int dx = chunkPos.getX() - playerChunkX;
            int dz = chunkPos.getY() - playerChunkZ;
            return (long) dx * dx + (long) dz * dz;
        }
    }
} 