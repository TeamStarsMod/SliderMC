package xyz.article.api.world.worldgen;

import java.util.Random;

public class PerlinNoise {

    private static final int OCTAVES = 3; // 噪声层数，控制地形细节
    private static final double PERSISTENCE = 0.5; // 持久度，控制每层噪声的影响

    private final int[] permutations; // 排列数组，用于噪声计算

    public PerlinNoise(long seed) {
        Random random = new Random(seed);
        permutations = new int[512];
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        // 打乱数组
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        // 复制到 permutations 数组
        System.arraycopy(p, 0, permutations, 0, 256);
        System.arraycopy(p, 0, permutations, 256, 256);
    }

    /**
     * 计算多层级Perlin噪声值
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 噪声值，范围 [-1, 1]
     */
    public double noise(double x, double y, double z) {
        double total = 0;
        double frequency = 1;
        double amplitude = 1;
        double maxValue = 0;

        for (int i = 0; i < OCTAVES; i++) {
            total += noiseSingle(x * frequency, y * frequency, z * frequency) * amplitude;

            maxValue += amplitude;

            amplitude *= PERSISTENCE;
            frequency *= 2;
        }

        return total / maxValue;
    }

    /**
     * 计算单层Perlin噪声值
     *
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @return 噪声值，范围 [-1, 1]
     */
    private double noiseSingle(double x, double y, double z) {
        // 找到单位立方体的坐标
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        int zi = (int) Math.floor(z) & 255;

        // 计算相对坐标
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double zf = z - Math.floor(z);

        // 计算渐变值
        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);

        // 哈希值
        int a = permutations[xi] + yi;
        int aa = permutations[a] + zi;
        int ab = permutations[a + 1] + zi;
        int b = permutations[xi + 1] + yi;
        int ba = permutations[b] + zi;
        int bb = permutations[b + 1] + zi;

        // 插值
        double x1 = lerp(grad(permutations[aa], xf, yf, zf),
                grad(permutations[ba], xf - 1, yf, zf), u);
        double x2 = lerp(grad(permutations[ab], xf, yf - 1, zf),
                grad(permutations[bb], xf - 1, yf - 1, zf), u);
        double y1 = lerp(x1, x2, v);

        double z1 = lerp(grad(permutations[aa + 1], xf, yf, zf - 1),
                grad(permutations[ba + 1], xf - 1, yf, zf - 1), u);
        double z2 = lerp(grad(permutations[ab + 1], xf, yf - 1, zf - 1),
                grad(permutations[bb + 1], xf - 1, yf - 1, zf - 1), u);
        double z3 = lerp(z1, z2, v);

        return lerp(y1, z3, w);
    }

    /**
     * 线性插值
     */
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    /**
     * 渐变函数
     */
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * 梯度函数
     */
    private double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y,
                v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}