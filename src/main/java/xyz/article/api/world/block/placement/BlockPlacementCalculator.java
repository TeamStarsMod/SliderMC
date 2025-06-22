package xyz.article.api.world.block.placement;

import org.cloudburstmc.math.vector.Vector3i;
import xyz.article.api.entities.player.Player;
import xyz.article.api.world.block.BlockFace;

import java.util.concurrent.ConcurrentHashMap;

public class BlockPlacementCalculator {
    public static ConcurrentHashMap<String, String> calculateProperties(Player player, BlockFace placedFace, Vector3i position, String blockType) {
        ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();

        if (blockType == null) {
            return properties;
        }

        // Button
        if (blockType.endsWith("_button")) {
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
        
        // Bed
        else if (blockType.endsWith("_bed")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("occupied", "false");
            properties.put("part", "head"); // 默认放置头部，后续会处理脚部
        }
        
        // Door
        else if (blockType.endsWith("_door")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("half", "lower");
            properties.put("hinge", "left");
            properties.put("open", "false");
            properties.put("powered", "false");
        }
        
        // Stairs
        else if (blockType.endsWith("_stairs")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("half", "bottom");
            properties.put("shape", "straight");
            properties.put("waterlogged", "false");
        }
        
        // Trapdoor
        else if (blockType.endsWith("_trapdoor")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("half", "bottom");
            properties.put("open", "false");
            properties.put("powered", "false");
        }
        
        // Fence Gate
        else if (blockType.endsWith("_fence_gate")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("in_wall", "false");
            properties.put("open", "false");
            properties.put("powered", "false");
        }
        
        // Sign
        else if (blockType.endsWith("_sign")) {
            properties.put("rotation", "0");
            properties.put("waterlogged", "false");
        }
        
        // Banner
        else if (blockType.endsWith("_banner")) {
            properties.put("rotation", "0");
        }
        
        // Torch
        else if (blockType.endsWith("_torch")) {
            if (placedFace == BlockFace.UP) {
                properties.put("facing", "up");
            } else {
                properties.put("facing", placedFace.getOpposite().name().toLowerCase());
            }
        }
        
        // Wall Torch
        else if (blockType.endsWith("_wall_torch")) {
            properties.put("facing", placedFace.getOpposite().name().toLowerCase());
            properties.put("lit", "true");
        }
        
        // Lever
        else if (blockType.equals("minecraft:lever")) {
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
        
        // Pressure Plate
        else if (blockType.endsWith("_pressure_plate")) {
            properties.put("powered", "false");
        }
        
        // Redstone Wire
        else if (blockType.equals("minecraft:redstone_wire")) {
            properties.put("east", "none");
            properties.put("north", "none");
            properties.put("power", "0");
            properties.put("south", "none");
            properties.put("west", "none");
        }
        
        // Repeater
        else if (blockType.endsWith("_repeater")) {
            properties.put("delay", "1");
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("locked", "false");
            properties.put("powered", "false");
        }
        
        // Comparator
        else if (blockType.endsWith("_comparator")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("mode", "compare");
            properties.put("powered", "false");
        }
        
        // Piston
        else if (blockType.endsWith("_piston")) {
            properties.put("extended", "false");
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
        }
        
        // Sticky Piston
        else if (blockType.equals("minecraft:sticky_piston")) {
            properties.put("extended", "false");
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
        }
        
        // Observer
        else if (blockType.equals("minecraft:observer")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("powered", "false");
        }
        
        // Dispenser
        else if (blockType.equals("minecraft:dispenser")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("triggered", "false");
        }
        
        // Dropper
        else if (blockType.equals("minecraft:dropper")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("triggered", "false");
        }
        
        // Hopper
        else if (blockType.equals("minecraft:hopper")) {
            properties.put("enabled", "true");
            properties.put("facing", "down");
        }
        
        // Furnace
        else if (blockType.endsWith("_furnace")) {
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("lit", "false");
        }
        
        // Chest
        else if (blockType.equals("minecraft:chest")) {
            properties.put("type", "single");
            properties.put("facing", player.getHorizontalFacing().name().toLowerCase());
            properties.put("waterlogged", "false");
        }
        
        // Glass Pane, Iron Bars, Fence
        else if (blockType.endsWith("_pane") || blockType.endsWith("_bars") || 
                 blockType.endsWith("_fence") || blockType.equals("minecraft:nether_brick_wall") ||
                 blockType.endsWith("_wall")) {
            properties.put("east", "false");
            properties.put("north", "false");
            properties.put("south", "false");
            properties.put("west", "false");
            if (blockType.endsWith("_pane")) {
                properties.put("waterlogged", "false");
            }
        }

        return properties;
    }
}