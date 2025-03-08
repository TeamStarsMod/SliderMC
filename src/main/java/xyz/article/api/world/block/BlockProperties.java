package xyz.article.api.world.block;

import java.util.HashSet;

public class BlockProperties {
    private static final HashSet<Integer> notSolidBlocks = new HashSet<>() {{
        add(0);
    }};

    public static boolean checkIsSolidBlock(int blockState) {
        return !notSolidBlocks.contains(blockState);
    }
}
