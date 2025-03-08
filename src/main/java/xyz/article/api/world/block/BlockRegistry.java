package xyz.article.api.world.block;

import xyz.article.api.world.block.behaviors.interfaces.BlockBehavior;

import java.util.concurrent.ConcurrentHashMap;

public class BlockRegistry {
    private static final ConcurrentHashMap<Integer, BlockBehavior> BEHAVIORS = new ConcurrentHashMap<>();
}
