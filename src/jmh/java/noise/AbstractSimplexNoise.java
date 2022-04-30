package noise;

import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.random.SimpleRandom;

import java.lang.reflect.Field;

public abstract class AbstractSimplexNoise {

    protected static final double SQRT_3 = Math.sqrt(3.0);
    protected static final double SKEW_FACTOR_2D = 0.5 * (SQRT_3 - 1.0);
    protected static final double UNSKEW_FACTOR_2D = (3.0 - SQRT_3) / 6.0;
    protected static final double[] FLAT_SIMPLEX_GRAD = new double[]{
            1, 1, 0, 0,
            -1, 1, 0, 0,
            1, -1, 0, 0,
            -1, -1, 0, 0,
            1, 0, 1, 0,
            -1, 0, 1, 0,
            1, 0, -1, 0,
            -1, 0, -1, 0,
            0, 1, 1, 0,
            0, -1, 1, 0,
            0, 1, -1, 0,
            0, -1, -1, 0,
            1, 1, 0, 0,
            0, -1, 1, 0,
            -1, 1, 0, 0,
            0, -1, -1, 0,
    };

    protected final SimplexNoiseSampler vanillaSampler = new SimplexNoiseSampler(new SimpleRandom(0xFF));

    protected final int[] permutations;

    {
        try {
            final Field permutationsField = SimplexNoiseSampler.class.getDeclaredField("permutations");
            permutationsField.setAccessible(true);
            permutations = (int[]) permutationsField.get(vanillaSampler);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
