package xyz.article.api.world.block;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 方块状态注册表
 * 管理方块状态ID到方块类型的映射
 */
public class BlockStateRegistry {
    private static final ConcurrentHashMap<Integer, String> stateToTypeMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> typeToDefaultStateMap = new ConcurrentHashMap<>();
    
    /**
     * 注册方块状态
     */
    public static void registerBlockState(int stateId, String blockType) {
        stateToTypeMap.put(stateId, blockType);
        
        // 如果是默认状态，记录到类型映射中
        if (!typeToDefaultStateMap.containsKey(blockType)) {
            typeToDefaultStateMap.put(blockType, stateId);
        }
    }
    
    /**
     * 从方块状态ID获取方块类型
     */
    public static String getBlockType(int stateId) {
        return stateToTypeMap.get(stateId);
    }
    
    /**
     * 从方块类型获取默认状态ID
     */
    public static Integer getDefaultStateId(String blockType) {
        return typeToDefaultStateMap.get(blockType);
    }
    
    /**
     * 检查是否为指定类型的方块
     */
    public static boolean isBlockType(int stateId, String blockType) {
        String actualType = getBlockType(stateId);
        return actualType != null && actualType.equals(blockType);
    }
    
    /**
     * 检查是否为红石相关方块
     */
    public static boolean isRedstoneComponent(int stateId) {
        String blockType = getBlockType(stateId);
        if (blockType == null) return false;
        
        return blockType.contains("redstone") || 
               blockType.contains("lever") || 
               blockType.contains("button") || 
               blockType.contains("pressure_plate") ||
               blockType.contains("repeater") ||
               blockType.contains("comparator") ||
               blockType.contains("piston") ||
               blockType.contains("observer") ||
               blockType.contains("dispenser") ||
               blockType.contains("dropper") ||
               blockType.contains("hopper");
    }
    
    /**
     * 检查是否为红石线
     */
    public static boolean isRedstoneWire(int stateId) {
        return isBlockType(stateId, "minecraft:redstone_wire");
    }
    
    /**
     * 检查是否为重力方块
     */
    public static boolean isGravityBlock(int stateId) {
        String blockType = getBlockType(stateId);
        if (blockType == null) return false;
        
        return blockType.contains("sand") || 
               blockType.contains("gravel") || 
               blockType.contains("anvil") ||
               blockType.contains("concrete_powder");
    }
    
    /**
     * 检查是否为连接方块
     */
    public static boolean isConnectableBlock(int stateId) {
        String blockType = getBlockType(stateId);
        if (blockType == null) return false;
        
        return blockType.contains("_pane") || 
               blockType.contains("_fence") || 
               blockType.contains("_wall") ||
               blockType.equals("minecraft:iron_bars");
    }
    
    /**
     * 检查是否为固体方块
     */
    public static boolean isSolidBlock(int stateId) {
        if (stateId == 0) return false; // 空气不是固体
        
        String blockType = getBlockType(stateId);
        if (blockType == null) return true; // 未知方块默认为固体
        
        // 非固体方块列表
        return !blockType.contains("_pane") && 
               !blockType.contains("_fence") && 
               !blockType.contains("_wall") &&
               !blockType.equals("minecraft:iron_bars") &&
               !blockType.contains("_pressure_plate") &&
               !blockType.contains("_button") &&
               !blockType.contains("_torch") &&
               !blockType.contains("_sign") &&
               !blockType.contains("_banner") &&
               !blockType.contains("_door") &&
               !blockType.contains("_trapdoor") &&
               !blockType.contains("_gate");
    }
} 