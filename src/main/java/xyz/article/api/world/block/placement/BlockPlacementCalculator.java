package xyz.article.api.world.block.placement;

import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.block.BlockFace;

import java.util.concurrent.ConcurrentHashMap;

public class BlockPlacementCalculator {
    public static ConcurrentHashMap<String, String> calculateProperties(Player player, BlockFace placedFace, Vector3i position, String blockType) {
        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();

        // 示例：处理按钮的朝向逻辑
        if (blockType.endsWith("_button")) {
            System.out.println("button");
            // 确定face属性 (floor/wall/ceiling)
            if (placedFace == BlockFace.UP) {
                properties.put("face", "ceiling");
            } else if (placedFace == BlockFace.DOWN) {
                properties.put("face", "floor");
            } else {
                properties.put("face", "wall");
            }

            properties.put("facing", player.getFacing().name());

            // 默认未激活
            properties.put("powered", "false");
        }

        return properties;
    }
}