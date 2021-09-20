package com.ishland.c2me.libs.vectorized_algorithms;

public class VectorizedAlgorithms {

    public static double perlinNoiseVectorized(byte[] permutations, int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalX) {
        return VectorizedPerlinNoise.sample(permutations, sectionX, sectionY, sectionZ, localX, localY, localZ, fadeLocalX);
    }

}
