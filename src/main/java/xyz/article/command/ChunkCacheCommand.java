package xyz.article.command;

import xyz.article.api.command.Command;
import xyz.article.api.command.CommandSender;
import xyz.article.api.entities.player.Player;
import xyz.article.RunningData;

/**
 * 区块缓存统计命令
 */
public class ChunkCacheCommand extends Command {
    
    @Override
    public boolean execute(CommandSender sender, String[] args, String textBoard) {
        if (sender instanceof Player player) {
            // 获取玩家所在世界的缓存统计
            String stats = player.getWorld().getChunkCacheStats();
            player.sendMessage("§a" + stats);
            
            // 显示全局统计
            int totalWorlds = RunningData.worldMap.size();
            int totalPlayers = RunningData.globalPlayers.size();
            player.sendMessage(String.format("§e世界数量: %d, 在线玩家: %d", totalWorlds, totalPlayers));
            
        } else {
            // 控制台显示所有世界的统计
            sender.sendMessage("§a=== 区块缓存统计 ===");
            RunningData.worldMap.forEach((key, world) -> {
                String stats = world.getChunkCacheStats();
                sender.sendMessage(String.format("§e世界 %s: %s", key.value(), stats));
            });
        }
        
        return true;
    }
} 