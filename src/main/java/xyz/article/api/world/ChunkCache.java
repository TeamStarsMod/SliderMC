package xyz.article.api.world;

import org.cloudburstmc.math.vector.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.world.chunk.ChunkData;

import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 区块缓存系统 - 优化区块加载性能
 */
public class ChunkCache {
    private static final Logger log = LoggerFactory.getLogger(ChunkCache.class);
    
    // 内存缓存
    private final ConcurrentHashMap<Vector2i, ChunkData> memoryCache;
    
    // 异步加载队列
    private final ExecutorService loadExecutor;
    private final ExecutorService saveExecutor;
    
    // 加载统计
    private final AtomicInteger cacheHits = new AtomicInteger(0);
    private final AtomicInteger cacheMisses = new AtomicInteger(0);
    private final AtomicInteger loadRequests = new AtomicInteger(0);
    
    // 缓存配置
    private final int maxMemoryCacheSize;
    private final int loadQueueSize;
    private final int saveQueueSize;
    
    public ChunkCache() {
        this(1000, 50, 20); // 默认配置
    }
    
    public ChunkCache(int maxMemoryCacheSize, int loadQueueSize, int saveQueueSize) {
        this.maxMemoryCacheSize = maxMemoryCacheSize;
        this.loadQueueSize = loadQueueSize;
        this.saveQueueSize = saveQueueSize;
        
        this.memoryCache = new ConcurrentHashMap<>();
        
        // 创建专用的线程池
        this.loadExecutor = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            r -> {
                Thread t = new Thread(r, "Chunk-Load-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );
        
        this.saveExecutor = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() / 4),
            r -> {
                Thread t = new Thread(r, "Chunk-Save-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            }
        );
        
        log.info("区块缓存系统已初始化 - 内存缓存大小: {}, 加载队列: {}, 保存队列: {}", 
                maxMemoryCacheSize, loadQueueSize, saveQueueSize);
    }
    
    /**
     * 异步加载区块
     * @param world 世界实例
     * @param chunkPos 区块位置
     * @return CompletableFuture<ChunkData>
     */
    public CompletableFuture<ChunkData> loadChunkAsync(World world, Vector2i chunkPos) {
        loadRequests.incrementAndGet();
        
        // 首先检查内存缓存
        ChunkData cached = memoryCache.get(chunkPos);
        if (cached != null) {
            cacheHits.incrementAndGet();
            return CompletableFuture.completedFuture(cached);
        }
        
        cacheMisses.incrementAndGet();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 尝试从存档加载 - 直接实现，避免递归
                String worldPath = world.getKey().namespace() + "_" + world.getKey().value();
                File chunksDir = new File("./" + xyz.article.Settings.SAVE_FOLDER + "/worlds/" + worldPath + "/chunks");
                ChunkData chunkData = null;
                if (chunksDir.exists() && chunksDir.isDirectory()) {
                    File file = new File(chunksDir, "chunk_" + chunkPos.getX() + "_" + chunkPos.getY() + ".slider");
                    if (file.exists()) {
                        chunkData = ChunkData.deserializeFromFile(file);
                        log.debug("从存档加载区块 ({}, {})", chunkPos.getX(), chunkPos.getY());
                    }
                }
                
                if (chunkData != null) {
                    // 添加到内存缓存
                    addToCache(chunkPos, chunkData);
                    return chunkData;
                } else {
                    // 生成新区块
                    chunkData = world.getGenerator().generateChunk(
                        new xyz.article.api.world.chunk.ChunkPos(world, chunkPos)
                    );
                    
                    if (chunkData != null) {
                        // 异步保存新区块
                        saveChunkAsync(world, chunkPos, chunkData);
                        addToCache(chunkPos, chunkData);
                        log.debug("生成新区块 ({}, {})", chunkPos.getX(), chunkPos.getY());
                    }
                    
                    return chunkData;
                }
            } catch (Exception e) {
                log.error("加载区块 ({}, {}) 失败: {}", chunkPos.getX(), chunkPos.getY(), e.getMessage());
                return null;
            }
        }, loadExecutor);
    }
    
    /**
     * 同步加载区块（用于紧急情况）
     */
    public ChunkData loadChunkSync(World world, Vector2i chunkPos) {
        // 检查内存缓存
        ChunkData cached = memoryCache.get(chunkPos);
        if (cached != null) {
            cacheHits.incrementAndGet();
            return cached;
        }
        
        cacheMisses.incrementAndGet();
        
        try {
            // 尝试从存档加载 - 直接实现，避免递归
            String worldPath = world.getKey().namespace() + "_" + world.getKey().value();
            File chunksDir = new File("./" + xyz.article.Settings.SAVE_FOLDER + "/worlds/" + worldPath + "/chunks");
            ChunkData chunkData = null;
            if (chunksDir.exists() && chunksDir.isDirectory()) {
                File file = new File(chunksDir, "chunk_" + chunkPos.getX() + "_" + chunkPos.getY() + ".slider");
                if (file.exists()) {
                    chunkData = ChunkData.deserializeFromFile(file);
                    log.debug("从存档同步加载区块 ({}, {})", chunkPos.getX(), chunkPos.getY());
                }
            }
            
            if (chunkData != null) {
                addToCache(chunkPos, chunkData);
                return chunkData;
            } else {
                // 生成新区块
                chunkData = world.getGenerator().generateChunk(
                    new xyz.article.api.world.chunk.ChunkPos(world, chunkPos)
                );
                
                if (chunkData != null) {
                    addToCache(chunkPos, chunkData);
                }
                
                return chunkData;
            }
        } catch (Exception e) {
            log.error("同步加载区块 ({}, {}) 失败: {}", chunkPos.getX(), chunkPos.getY(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 异步保存区块
     */
    public CompletableFuture<Void> saveChunkAsync(World world, Vector2i chunkPos, ChunkData chunkData) {
        return CompletableFuture.runAsync(() -> {
            try {
                String worldPath = world.getKey().namespace() + "_" + world.getKey().value();
                File chunksDir = new File("./" + xyz.article.Settings.SAVE_FOLDER + "/worlds/" + worldPath + "/chunks");
                if (!chunksDir.exists() && !chunksDir.mkdirs()) {
                    log.error("无法创建区块目录: {}", chunksDir.getAbsolutePath());
                    return;
                }
                
                File chunkFile = new File(chunksDir, "chunk_" + chunkPos.getX() + "_" + chunkPos.getY() + ".slider");
                chunkData.serializeToFile(chunkFile);
                
                log.debug("异步保存区块 ({}, {})", chunkPos.getX(), chunkPos.getY());
            } catch (Exception e) {
                log.error("异步保存区块 ({}, {}) 失败: {}", chunkPos.getX(), chunkPos.getY(), e.getMessage());
            }
        }, saveExecutor);
    }
    
    /**
     * 添加到内存缓存
     */
    private void addToCache(Vector2i chunkPos, ChunkData chunkData) {
        // 如果缓存满了，移除最旧的条目
        if (memoryCache.size() >= maxMemoryCacheSize) {
            // 简单的LRU策略：移除第一个条目
            Vector2i oldestKey = memoryCache.keySet().iterator().next();
            memoryCache.remove(oldestKey);
            log.debug("缓存已满，移除区块 ({}, {})", oldestKey.getX(), oldestKey.getY());
        }
        
        memoryCache.put(chunkPos, chunkData);
    }
    
    /**
     * 从缓存中移除区块
     */
    public void removeFromCache(Vector2i chunkPos) {
        memoryCache.remove(chunkPos);
    }
    
    /**
     * 清空缓存
     */
    public void clearCache() {
        memoryCache.clear();
        log.info("区块缓存已清空");
    }
    
    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        int hits = cacheHits.get();
        int misses = cacheMisses.get();
        int total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;
        
        return String.format(
            "区块缓存统计 - 内存缓存: %d/%d, 命中率: %.1f%%, 总请求: %d, 加载请求: %d",
            memoryCache.size(), maxMemoryCacheSize, hitRate, total, loadRequests.get()
        );
    }
    
    /**
     * 关闭缓存系统
     */
    public void shutdown() {
        log.info("正在关闭区块缓存系统...");
        
        // 关闭线程池
        loadExecutor.shutdown();
        saveExecutor.shutdown();
        
        try {
            if (!loadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                loadExecutor.shutdownNow();
            }
            if (!saveExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                saveExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            loadExecutor.shutdownNow();
            saveExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // 清空缓存
        clearCache();
        
        log.info("区块缓存系统已关闭");
    }
    
    /**
     * 获取内存缓存大小
     */
    public int getCacheSize() {
        return memoryCache.size();
    }
    
    /**
     * 检查区块是否在缓存中
     */
    public boolean isCached(Vector2i chunkPos) {
        return memoryCache.containsKey(chunkPos);
    }
} 