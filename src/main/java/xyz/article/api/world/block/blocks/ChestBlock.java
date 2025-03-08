package xyz.article.api.world.block.blocks;

import xyz.article.api.world.block.blocktypes.ChestBlockData;

public class ChestBlock implements Block {
    public static int getState(ChestBlockData blockData) {
        if (blockData.isWaterLogged()) {
            switch (blockData.getType()) {
                case 0 -> { // 0 == single
                    switch (blockData.getBlockFace()) {
                        case NORTH -> {
                            return 2954;
                        }
                        case SOUTH -> {
                            return 2960;
                        }
                        case WEST -> {
                            return 2966;
                        }
                        case EAST -> {
                            return 2972;
                        }
                    }
                }
                case 1 -> { // 1 == left
                    switch (blockData.getBlockFace()) {
                        case NORTH -> {
                            return 2956;
                        }
                        case SOUTH -> {
                            return 2962;
                        }
                        case WEST -> {
                            return 2968;
                        }
                        case EAST -> {
                            return 2974;
                        }
                    }
                }
                case 2 -> { // 2 == right
                    switch (blockData.getBlockFace()) {
                        case NORTH -> {
                            return 2958;
                        }
                        case SOUTH -> {
                            return 2964;
                        }
                        case WEST -> {
                            return 2970;
                        }
                        case EAST -> {
                            return 2976;
                        }
                    }
                }
            }
        } else {
            switch (blockData.getType()) {
                case 0 -> {
                    switch (blockData.getBlockFace()) {
                        case NORTH -> {
                            return 2955;
                        }
                        case SOUTH -> {
                            return 2961;
                        }
                        case WEST -> {
                            return 2967;
                        }
                        case EAST -> {
                            return 2973;
                        }
                    }
                }
                case 1 -> {
                    switch (blockData.getBlockFace()) {
                        case NORTH -> {
                            return 2957;
                        }
                        case SOUTH -> {
                            return 2963;
                        }
                        case WEST -> {
                            return 2969;
                        }
                        case EAST -> {
                            return 2975;
                        }
                    }
                }
                case 2 -> {
                    switch (blockData.getBlockFace()) {
                        case NORTH -> {
                            return 2959;
                        }
                        case SOUTH -> {
                            return 2965;
                        }
                        case WEST -> {
                            return 2971;
                        }
                        case EAST -> {
                            return 2977;
                        }
                    }
                }
            }
        }

        return 0;
    }
}
