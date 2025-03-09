package xyz.article.api.world.block.blockstate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BlockStateManager {
    private final ConcurrentHashMap<String, BlockStateDefinition> blockDefinitions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, String> itemIdToBlockId = new ConcurrentHashMap<>();

    public void loadBlockDefinitions(JsonObject blocksJson) {
        blocksJson.keySet().forEach(blockId -> {
            JsonObject blockData = blocksJson.getAsJsonObject(blockId);
            BlockStateDefinition def = new BlockStateDefinition();

            // 检查properties是否存在
            if (blockData.has("properties")) {
                JsonObject properties = blockData.getAsJsonObject("properties");
                properties.keySet().forEach(propName -> {
                    JsonArray values = properties.getAsJsonArray(propName);
                    def.addProperty(propName, values.asList().stream()
                            .map(JsonElement::getAsString)
                            .collect(Collectors.toList()));
                });
            }

            // 加载状态列表并记录默认状态
            JsonArray states = blockData.getAsJsonArray("states");
            states.forEach(state -> {
                JsonObject stateObj = state.getAsJsonObject();
                Map<String, String> stateProps = new HashMap<>();

                // 处理state的properties字段
                if (stateObj.has("properties")) {
                    JsonObject propsObj = stateObj.getAsJsonObject("properties");
                    propsObj.keySet().forEach(prop -> {
                        stateProps.put(prop, propsObj.get(prop).getAsString());
                    });
                }

                boolean isDefault = stateObj.has("default") && stateObj.get("default").getAsBoolean();
                int stateId = stateObj.get("id").getAsInt();

                def.addState(stateId, stateProps, isDefault);
            });

            blockDefinitions.put(blockId, def);
        });
    }

    public void loadItemMappings(JsonObject itemsJson) {
        JsonObject entries = itemsJson.getAsJsonObject("minecraft:item").getAsJsonObject("entries");
        entries.keySet().forEach(itemId -> {
            int protocolId = entries.getAsJsonObject(itemId).get("protocol_id").getAsInt();
            itemIdToBlockId.put(protocolId, "minecraft:" + itemId.replace("minecraft:", ""));
        });
    }

    public int getBlockStateId(int itemId, Map<String, String> properties) {
        String blockId = itemIdToBlockId.get(itemId);
        if (blockId == null) return 0;
        BlockStateDefinition def = blockDefinitions.get(blockId);
        return def != null ? def.findMatchingState(properties) : 0;
    }

    public String getBlockIdFromItemId(int itemId) {
        return itemIdToBlockId.getOrDefault(itemId, null);
    }
}