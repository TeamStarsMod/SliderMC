package xyz.article.api.world.block;

public enum BlockFace {
    NORTH,
    EAST,
    SOUTH,
    WEST,
    UP,
    DOWN;

    public BlockFace getOpposite() {
        switch (this) {
            case NORTH -> {
                return SOUTH;
            }
            case SOUTH -> {
                return NORTH;
            }
            case WEST -> {
                return EAST;
            }
            case EAST -> {
                return WEST;
            }
            case UP -> {
                return DOWN;
            }
            case DOWN -> {
                return UP;
            }
            default -> throw new IllegalArgumentException("未知的方向！");
        }
    }
}
