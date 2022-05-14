#include <stdlib.h>
#include <stdint.h>

#include "common_maths.h"

#ifndef C2ME_FABRIC_NOISE_STRUCTS_H
#define C2ME_FABRIC_NOISE_STRUCTS_H

typedef struct {
    double lacunarity;
    double persistence;
    size_t length;
    size_t *indexes;
    __uint8_t *sampler_permutations;
    double *sampler_originX;
    double *sampler_originY;
    double *sampler_originZ;
    double *amplitudes;
} octave_sampler_data;

typedef struct {
    octave_sampler_data *lowerInterpolatedNoise;
    octave_sampler_data *upperInterpolatedNoise;
    octave_sampler_data *interpolationNoise;
    double xzScale;
    double yScale;
    double xzMainScale;
    double yMainScale;
    int cellWidth;
    int cellHeight;
} interpolated_sampler_data;

static inline double __attribute__((always_inline))
math_noise_perlin_sampleScalar(__uint8_t *permutations, int sectionX, int sectionY, int sectionZ, double localX,
                               double localY, double localZ, double fadeLocalX) {
    int var0 = sectionX & 0xFF;
    int var1 = (sectionX + 1) & 0xFF;
    int var2 = permutations[var0] & 0xFF;
    int var3 = permutations[var1] & 0xFF;
    int var4 = (var2 + sectionY) & 0xFF;
    int var5 = (var3 + sectionY) & 0xFF;
    int var6 = (var2 + sectionY + 1) & 0xFF;
    int var7 = (var3 + sectionY + 1) & 0xFF;
    int var8 = permutations[var4] & 0xFF;
    int var9 = permutations[var5] & 0xFF;
    int var10 = permutations[var6] & 0xFF;
    int var11 = permutations[var7] & 0xFF;

    int var12 = (var8 + sectionZ) & 0xFF;
    int var13 = (var9 + sectionZ) & 0xFF;
    int var14 = (var10 + sectionZ) & 0xFF;
    int var15 = (var11 + sectionZ) & 0xFF;
    int var16 = (var8 + sectionZ + 1) & 0xFF;
    int var17 = (var9 + sectionZ + 1) & 0xFF;
    int var18 = (var10 + sectionZ + 1) & 0xFF;
    int var19 = (var11 + sectionZ + 1) & 0xFF;
    int var20 = (permutations[var12] & 15) << 2;
    int var21 = (permutations[var13] & 15) << 2;
    int var22 = (permutations[var14] & 15) << 2;
    int var23 = (permutations[var15] & 15) << 2;
    int var24 = (permutations[var16] & 15) << 2;
    int var25 = (permutations[var17] & 15) << 2;
    int var26 = (permutations[var18] & 15) << 2;
    int var27 = (permutations[var19] & 15) << 2;
    double var60 = localX - 1.0;
    double var61 = localY - 1.0;
    double var62 = localZ - 1.0;
    double var87 = FLAT_SIMPLEX_GRAD[(var20) | 0] * localX + FLAT_SIMPLEX_GRAD[(var20) | 1] * localY +
                   FLAT_SIMPLEX_GRAD[(var20) | 2] * localZ;
    double var88 = FLAT_SIMPLEX_GRAD[(var21) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var21) | 1] * localY +
                   FLAT_SIMPLEX_GRAD[(var21) | 2] * localZ;
    double var89 = FLAT_SIMPLEX_GRAD[(var22) | 0] * localX + FLAT_SIMPLEX_GRAD[(var22) | 1] * var61 +
                   FLAT_SIMPLEX_GRAD[(var22) | 2] * localZ;
    double var90 = FLAT_SIMPLEX_GRAD[(var23) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var23) | 1] * var61 +
                   FLAT_SIMPLEX_GRAD[(var23) | 2] * localZ;
    double var91 = FLAT_SIMPLEX_GRAD[(var24) | 0] * localX + FLAT_SIMPLEX_GRAD[(var24) | 1] * localY +
                   FLAT_SIMPLEX_GRAD[(var24) | 2] * var62;
    double var92 = FLAT_SIMPLEX_GRAD[(var25) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var25) | 1] * localY +
                   FLAT_SIMPLEX_GRAD[(var25) | 2] * var62;
    double var93 = FLAT_SIMPLEX_GRAD[(var26) | 0] * localX + FLAT_SIMPLEX_GRAD[(var26) | 1] * var61 +
                   FLAT_SIMPLEX_GRAD[(var26) | 2] * var62;
    double var94 = FLAT_SIMPLEX_GRAD[(var27) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var27) | 1] * var61 +
                   FLAT_SIMPLEX_GRAD[(var27) | 2] * var62;

    double var95 = localX * 6.0 - 15.0;
    double var96 = fadeLocalX * 6.0 - 15.0;
    double var97 = localZ * 6.0 - 15.0;
    double var98 = localX * var95 + 10.0;
    double var99 = fadeLocalX * var96 + 10.0;
    double var100 = localZ * var97 + 10.0;
    double var101 = localX * localX * localX * var98;
    double var102 = fadeLocalX * fadeLocalX * fadeLocalX * var99;
    double var103 = localZ * localZ * localZ * var100;

    double var113 = var87 + var101 * (var88 - var87);
    double var114 = var93 + var101 * (var94 - var93);
    double var115 = var91 + var101 * (var92 - var91);
    double var116 = var89 + var101 * (var90 - var89);
    double var117 = var114 - var115;
    double var118 = var102 * (var116 - var113);
    double var119 = var102 * var117;
    double var120 = var113 + var118;
    double var121 = var115 + var119;
    return var120 + (var103 * (var121 - var120));
}

static double math_noise_perlin_sample(__uint8_t *permutations, double originX, double originY, double originZ, double x, double y,
                                       double z, double yScale, double yMax) {
    double d = x + originX;
    double e = y + originY;
    double f = z + originZ;
    double i = floor(d);
    double j = floor(e);
    double k = floor(f);
    double g = d - i;
    double h = e - j;
    double l = f - k;
    double o = 0.0;
    if (yScale != 0.0) {
        double m;
        if (yMax >= 0.0 && yMax < h) {
            m = yMax;
        } else {
            m = h;
        }

        o = floor(m / yScale + 1.0E-7F) * yScale;
    }

    return math_noise_perlin_sampleScalar(permutations, (int) i, (int) j, (int) k, g, h - o, l, h);
}

static inline double __attribute__((always_inline))
math_noise_perlin_octave_sample_impl(octave_sampler_data *data, double x, double y, double z, double yScale, double yMax,
                                       bool useOrigin) {
    double d = 0.0;

    for (size_t i = 0; i < data->length; ++i) {
        double e = data->lacunarity * c2me_natives_pow_of_two_table[data->indexes[i]];
        double f = data->persistence / c2me_natives_pow_of_two_table[data->indexes[i]];
        __uint8_t *permutations = data->sampler_permutations + 256 * i;
        double g = math_noise_perlin_sample(
                permutations,
                data->sampler_originX[i],
                data->sampler_originY[i],
                data->sampler_originZ[i],
                math_octave_maintainPrecision(x * e),
                useOrigin ? -(data->sampler_originY[i]) : math_octave_maintainPrecision(y * e),
                math_octave_maintainPrecision(z * e),
                yScale * e,
                yMax * e);
        d += data->amplitudes[i] * g * f;
    }

    return d;
}

static double math_noise_perlin_octave_sample(octave_sampler_data *data, double x, double y, double z) {
    return math_noise_perlin_octave_sample_impl(data, x, y, z, 0.0, 0.0, false);
}

static double math_noise_perlin_interpolated_sample(interpolated_sampler_data *data, int x, int y, int z) {
    int i = math_floorDiv(x, data->cellWidth);
    int j = math_floorDiv(y, data->cellHeight);
    int k = math_floorDiv(z, data->cellWidth);
    double d = 0.0;
    double e = 0.0;
    double f = 0.0;

    octave_sampler_data *interpolationSampler = data->interpolationNoise;
    for (size_t l = 0; l < interpolationSampler->length && interpolationSampler->indexes[l] < 8; ++l) {
        double g = 1.0 / c2me_natives_pow_of_two_table[interpolationSampler->indexes[l]];
        f += math_noise_perlin_sample(
                interpolationSampler->sampler_permutations + 256 * l,
                interpolationSampler->sampler_originX[l],
                interpolationSampler->sampler_originY[l],
                interpolationSampler->sampler_originZ[l],
                math_octave_maintainPrecision((double) i * data->xzMainScale * g),
                math_octave_maintainPrecision((double) j * data->yMainScale * g),
                math_octave_maintainPrecision((double) k * data->xzMainScale * g),
                data->yMainScale * g,
                (double) j * data->yMainScale * g) /
             g;
    }

    double h = (f / 10.0 + 1.0) / 2.0;

    if (!(h >= 1.0)) {
        octave_sampler_data *octaveSampler = data->lowerInterpolatedNoise;
        for (size_t m = 0; m < octaveSampler->length && octaveSampler->indexes[m] < 16; ++m) {
            double g = 1.0 / c2me_natives_pow_of_two_table[octaveSampler->indexes[m]];
            double n = math_octave_maintainPrecision((double) i * data->xzScale * g);
            double o = math_octave_maintainPrecision((double) j * data->yScale * g);
            double p = math_octave_maintainPrecision((double) k * data->xzScale * g);
            double q = data->yScale * g;
            d += math_noise_perlin_sample(
                    octaveSampler->sampler_permutations + 256 * m,
                    octaveSampler->sampler_originX[m],
                    octaveSampler->sampler_originY[m],
                    octaveSampler->sampler_originZ[m],
                    n, o, p, q, (double) j * q) /
                 g;
        }
    }

    if (!(h <= 0.0)) {
        octave_sampler_data *octaveSampler = data->upperInterpolatedNoise;
        for (size_t m = 0; m < octaveSampler->length && octaveSampler->indexes[m] < 16; ++m) {
            double g = 1.0 / c2me_natives_pow_of_two_table[octaveSampler->indexes[m]];
            double n = math_octave_maintainPrecision((double) i * data->xzScale * g);
            double o = math_octave_maintainPrecision((double) j * data->yScale * g);
            double p = math_octave_maintainPrecision((double) k * data->xzScale * g);
            double q = data->yScale * g;
            e += math_noise_perlin_sample(
                    octaveSampler->sampler_permutations + 256 * m,
                    octaveSampler->sampler_originX[m],
                    octaveSampler->sampler_originY[m],
                    octaveSampler->sampler_originZ[m],
                    n, o, p, q, (double) j * q) /
                 g;
        }
    }

    return math_clampedLerp(d / 512.0, e / 512.0, h) / 128.0;
}

static double math_noise_perlin_double_sample(
        octave_sampler_data *firstSampler, octave_sampler_data *secondSampler,
        double x, double y, double z, double amplitude) {
    double d = x * 1.0181268882175227;
    double e = y * 1.0181268882175227;
    double f = z * 1.0181268882175227;

    return (math_noise_perlin_octave_sample(firstSampler, x, y, z) + math_noise_perlin_octave_sample(secondSampler, d, e, f)) *
           amplitude;
}

static double math_noise_simplex_sample(const int *permutations, double x, double y) {
    double var0 = (x + y) * SKEW_FACTOR_2D;
    double var1 = floor(x + var0);
    double var2 = floor(y + var0);
    double var3 = (var1 + var2) * UNSKEW_FACTOR_2D;
    double var4 = x - var1 + var3;
    double var5 = y - var2 + var3;
    int var6 = var4 > var5;
    int var7 = !var6;
//    if (var4 > var5) {
//        var6 = 1;
//        var7 = 0;
//    } else {
//        var6 = 0;
//        var7 = 1;
//    }

    double var8 = var4 - (double) var6 + UNSKEW_FACTOR_2D;
    double var9 = var5 - (double) var7 + UNSKEW_FACTOR_2D;
    double var10 = var4 - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
    double var11 = var5 - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
    int var12 = ((int) var1) & 0xFF;
    int var13 = ((int) var2) & 0xFF;
    int var16 = permutations[var13 & 255];
    int var17 = permutations[(var13 + var7) & 255];
    int var18 = permutations[(var13 + 1) & 255];
    int var22 = permutations[((var12 + var16) & 255)] % 12;
    int var23 = permutations[((var12 + var6 + var17) & 255)] % 12;
    int ver24 = permutations[((var12 + 1 + var18) & 255)] % 12;
    double var25 = math_simplex_grad(var22, var4, var5, 0.0, 0.5);
    double var26 = math_simplex_grad(var23, var8, var9, 0.0, 0.5);
    double var27 = math_simplex_grad(ver24, var10, var11, 0.0, 0.5);
    return 70.0 * (var25 + var26 + var27);
}

static float math_noise_end_noise_sample(int *permutations, int i, int j) {
    int k = i / 2;
    int l = j / 2;
    int m = i % 2;
    int n = j % 2;
    float f = 100.0F - sqrtf(i * i + j * j) * 8.0F;
    f = math_fclamp(f, -100.0F, 80.0F);

    for(int o = -12; o <= 12; ++o) {
        for(int p = -12; p <= 12; ++p) {
            int64_t q = k + o;
            int64_t r = l + p;
            if (q * q + r * r > 4096L && math_noise_simplex_sample(permutations, (double)q, (double)r) < -0.9F) {
                float g = (labs(q) * 3439 + labs(r) * 147) % 13 + 9;
                float h = (float)(m - o * 2);
                float s = (float)(n - p * 2);
                float t = 100.0F - sqrtf(h * h + s * s) * g;
                t = math_fclamp(t, -100.0F, 80.0F);
                f = fmaxf(f, t);
            }
        }
    }

    return f;
}


extern float c2me_natives_end_noise_sample(int *permutations, int i, int j);

#endif //C2ME_FABRIC_NOISE_STRUCTS_H
