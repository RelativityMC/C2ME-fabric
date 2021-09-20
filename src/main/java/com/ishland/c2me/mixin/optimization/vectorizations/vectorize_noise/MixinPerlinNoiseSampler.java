package com.ishland.c2me.mixin.optimization.vectorizations.vectorize_noise;

import com.ishland.c2me.libs.vectorized_algorithms.VectorizedAlgorithms;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PerlinNoiseSampler.class)
public class MixinPerlinNoiseSampler {

    @Shadow @Final private byte[] permutations;

    /**
     * @author ishland
     * @reason vectorized perlin
     */
    @Overwrite
    private double sample(int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalX) {
        return VectorizedAlgorithms.perlinNoiseVectorized(permutations, sectionX, sectionY, sectionZ, localX, localY, localZ, fadeLocalX);
    }

}
