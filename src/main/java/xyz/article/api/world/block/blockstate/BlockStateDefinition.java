package xyz.article.api.world.block.blockstate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockStateDefinition {
    private final ConcurrentHashMap<String, List<String>> properties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Map<String, String>, Integer> stateIds = new ConcurrentHashMap<>();
    private int defaultStateId;

    public void addProperty(String key, List<String> values) {
        properties.put(key, values);
    }

    public void addState(int stateId, Map<String, String> properties, boolean isDefault) {
        stateIds.put(properties, stateId);
        if (isDefault) defaultStateId = stateId;
    }

    public int findMatchingState(Map<String, String> targetProperties) {
        return stateIds.entrySet().stream()
                .filter(entry -> targetProperties.entrySet().containsAll(entry.getKey().entrySet()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(defaultStateId);
    }
}