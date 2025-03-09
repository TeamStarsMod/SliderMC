package xyz.article.api.world.block.placement;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.block.BlockFace;

import java.util.concurrent.ConcurrentHashMap;

public class BlockPlacementCalculator {
    public static ConcurrentHashMap<String, String> calculateProperties(Player player, BlockFace placedFace, Vector3i position, String blockType) {
        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();

        // Button
        if (blockType != null && blockType.endsWith("_button")) {
            if (placedFace == BlockFace.UP) {
                properties.put("face", "ceiling");
            } else if (placedFace == BlockFace.DOWN) {
                properties.put("face", "floor");
            } else {
                properties.put("face", "wall");
            }

            if ("wall".equals(properties.get("face"))) {
                properties.put("facing", placedFace.getOpposite().name().toLowerCase());
            } else {
                properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            }

            properties.put("powered", "false");
        }

        return properties;
    }
}