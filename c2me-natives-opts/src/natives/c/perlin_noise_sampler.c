#define _POSIX_C_SOURCE 199309L
#include <stdio.h>
#include <math.h>
#include <time.h>
#include <string.h>
#include <stdlib.h>
#define BILLION 1000000000L

typedef unsigned char bool;
static const bool false = 0;
static const bool true = 1;

const double FLAT_SIMPLEX_GRAD[] = {
    1,
    1,
    0,
    0,
    -1,
    1,
    0,
    0,
    1,
    -1,
    0,
    0,
    -1,
    -1,
    0,
    0,
    1,
    0,
    1,
    0,
    -1,
    0,
    1,
    0,
    1,
    0,
    -1,
    0,
    -1,
    0,
    -1,
    0,
    0,
    1,
    1,
    0,
    0,
    -1,
    1,
    0,
    0,
    1,
    -1,
    0,
    0,
    -1,
    -1,
    0,
    1,
    1,
    0,
    0,
    0,
    -1,
    1,
    0,
    -1,
    1,
    0,
    0,
    0,
    -1,
    -1,
    0,
};

inline int __attribute__((always_inline)) c2me_natives_floorDiv(int x, int y)
{
    int r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x))
    {
        r--;
    }
    return r;
}

inline double __attribute__((always_inline)) c2me_natives_octave_maintainPrecision(double value)
{
    __int64_t l = value;
    return value - (double)(l < value ? l - 1L : l) * 3.3554432E7;
}

inline double __attribute__((always_inline)) c2me_natives_lerp(double delta, double start, double end)
{
    return start + delta * (end - start);
}

inline double __attribute__((always_inline)) c2me_natives_clampedLerp(double start, double end, double delta)
{
    if (delta < 0.0)
    {
        return start;
    }
    else
    {
        return delta > 1.0 ? end : c2me_natives_lerp(delta, start, end);
    }
}

double c2me_natives_sampleScalar(__uint8_t *permutations, int sectionX, int sectionY, int sectionZ, double localX, double localY, double localZ, double fadeLocalX)
{
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
    double var87 = FLAT_SIMPLEX_GRAD[(var20) | 0] * localX + FLAT_SIMPLEX_GRAD[(var20) | 1] * localY + FLAT_SIMPLEX_GRAD[(var20) | 2] * localZ;
    double var88 = FLAT_SIMPLEX_GRAD[(var21) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var21) | 1] * localY + FLAT_SIMPLEX_GRAD[(var21) | 2] * localZ;
    double var89 = FLAT_SIMPLEX_GRAD[(var22) | 0] * localX + FLAT_SIMPLEX_GRAD[(var22) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var22) | 2] * localZ;
    double var90 = FLAT_SIMPLEX_GRAD[(var23) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var23) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var23) | 2] * localZ;
    double var91 = FLAT_SIMPLEX_GRAD[(var24) | 0] * localX + FLAT_SIMPLEX_GRAD[(var24) | 1] * localY + FLAT_SIMPLEX_GRAD[(var24) | 2] * var62;
    double var92 = FLAT_SIMPLEX_GRAD[(var25) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var25) | 1] * localY + FLAT_SIMPLEX_GRAD[(var25) | 2] * var62;
    double var93 = FLAT_SIMPLEX_GRAD[(var26) | 0] * localX + FLAT_SIMPLEX_GRAD[(var26) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var26) | 2] * var62;
    double var94 = FLAT_SIMPLEX_GRAD[(var27) | 0] * var60 + FLAT_SIMPLEX_GRAD[(var27) | 1] * var61 + FLAT_SIMPLEX_GRAD[(var27) | 2] * var62;

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

double c2me_natives_sample(__uint8_t *permutations, double originX, double originY, double originZ, double x, double y, double z, double yScale, double yMax)
{
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
    if (yScale != 0.0)
    {
        double m;
        if (yMax >= 0.0 && yMax < h)
        {
            m = yMax;
        }
        else
        {
            m = h;
        }

        o = floor(m / yScale + 1.0E-7F) * yScale;
    }

    return c2me_natives_sampleScalar(permutations, (int)i, (int)j, (int)k, g, h - o, l, h);
}

__uint8_t *c2me_natives_generatePermutations()
{
    __uint8_t *permutations = malloc(256 * sizeof(__uint8_t));

    for (size_t i = 0; i < 256; i++)
    {
        permutations[i] = i;
    }

    for (size_t i = 0; i < 256; i++)
    {
        int j = rand() % (256 - i);
        __uint8_t b = permutations[i];
        permutations[i] = permutations[i + j];
        permutations[i + j] = b;
    }

    return permutations;
}

typedef struct
{
    double lacunarity;
    double persistence;
    size_t size;
    bool *notNull;
    __uint8_t *sampler_permutations;
    double *sampler_originX;
    double *sampler_originY;
    double *sampler_originZ;
    double *amplitudes;
} octave_sampler_data;

octave_sampler_data *c2me_natives_create_octave_sampler_data(
    double lacunarity, double persistence, size_t size, bool *notNull, __uint8_t *sampler_permutations,
    double *sampler_originX, double *sampler_originY, double *sampler_originZ, double *amplitudes)
{
    octave_sampler_data *ptr = malloc(sizeof(octave_sampler_data));
    ptr->lacunarity = lacunarity;
    ptr->persistence = persistence;
    ptr->size = size;
    ptr->notNull = notNull;
    ptr->sampler_permutations = sampler_permutations;
    ptr->sampler_originX = sampler_originX;
    ptr->sampler_originY = sampler_originY;
    ptr->sampler_originZ = sampler_originZ;
    ptr->amplitudes = amplitudes;
    return ptr;
}

double c2me_natives_octave_sample_impl(octave_sampler_data *data, double x, double y, double z, double yScale, double yMax, bool useOrigin)
{
    double d = 0.0;
    double e = data->lacunarity;
    double f = data->persistence;

    for (int i = 0; i < data->size; ++i)
    {
        __uint8_t *permutations = data->sampler_permutations + 256 * i;
        if (data->notNull[i])
        {
            double g = c2me_natives_sample(
                permutations,
                data->sampler_originX[i],
                data->sampler_originY[i],
                data->sampler_originZ[i],
                c2me_natives_octave_maintainPrecision(x * e),
                useOrigin ? -(data->sampler_originY[i]) : c2me_natives_octave_maintainPrecision(y * e),
                c2me_natives_octave_maintainPrecision(z * e),
                yScale * e,
                yMax * e);
            d += data->amplitudes[i] * g * f;
        }

        e *= 2.0;
        f /= 2.0;
    }

    return d;
}

double c2me_natives_octave_sample(octave_sampler_data *data, double x, double y, double z)
{
    return c2me_natives_octave_sample_impl(data, x, y, z, 0.0, 0.0, false);
}

typedef struct
{
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

interpolated_sampler_data *c2me_natives_create_interpolated_sampler_data(
    octave_sampler_data *lowerInterpolatedNoise, octave_sampler_data *upperInterpolatedNoise, octave_sampler_data *interpolationNoise,
    double xzScale, double yScale, double xzMainScale, double yMainScale, int cellWidth, int cellHeight)
{
    interpolated_sampler_data *ptr = malloc(sizeof(interpolated_sampler_data));
    ptr->lowerInterpolatedNoise = lowerInterpolatedNoise;
    ptr->upperInterpolatedNoise = upperInterpolatedNoise;
    ptr->interpolationNoise = interpolationNoise;
    ptr->xzScale = xzScale;
    ptr->yScale = yScale;
    ptr->xzMainScale = xzMainScale;
    ptr->yMainScale = yMainScale;
    ptr->cellWidth = cellWidth;
    ptr->cellHeight = cellHeight;
    return ptr;
}

double c2me_natives_interpolated_sample(interpolated_sampler_data *data, int x, int y, int z)
{
    int i = c2me_natives_floorDiv(x, data->cellWidth);
    int j = c2me_natives_floorDiv(y, data->cellHeight);
    int k = c2me_natives_floorDiv(z, data->cellWidth);
    double d = 0.0;
    double e = 0.0;
    double f = 0.0;
    bool bl = true;
    double g = 1.0;

    for (int l = 0; l < 8; ++l)
    {
        octave_sampler_data *octaveSampler = data->interpolationNoise;
        if (octaveSampler->notNull[l])
        {
            f += c2me_natives_sample(
                     octaveSampler->sampler_permutations + 256 * l,
                     octaveSampler->sampler_originX[l],
                     octaveSampler->sampler_originY[l],
                     octaveSampler->sampler_originZ[l],
                     c2me_natives_octave_maintainPrecision((double)i * data->xzMainScale * g),
                     c2me_natives_octave_maintainPrecision((double)j * data->yMainScale * g),
                     c2me_natives_octave_maintainPrecision((double)k * data->xzMainScale * g),
                     data->yMainScale * g,
                     (double)j * data->yMainScale * g) /
                 g;
        }

        g /= 2.0;
    }

    double h = (f / 10.0 + 1.0) / 2.0;
    g = 1.0;

    for (int m = 0; m < 16; ++m)
    {
        double n = c2me_natives_octave_maintainPrecision((double)i * data->xzScale * g);
        double o = c2me_natives_octave_maintainPrecision((double)j * data->yScale * g);
        double p = c2me_natives_octave_maintainPrecision((double)k * data->xzScale * g);
        double q = data->yScale * g;
        if (!(h >= 1.0))
        {
            octave_sampler_data *octaveSampler = data->lowerInterpolatedNoise;
            if (octaveSampler->notNull[m])
            {
                d += c2me_natives_sample(
                         octaveSampler->sampler_permutations + 256 * m,
                         octaveSampler->sampler_originX[m],
                         octaveSampler->sampler_originY[m],
                         octaveSampler->sampler_originZ[m],
                         n, o, p, q, (double)j * q) /
                     g;
            }
        }

        if (!(h <= 0.0))
        {
            octave_sampler_data *octaveSampler = data->upperInterpolatedNoise;
            if (octaveSampler->notNull[m])
            {
                e += c2me_natives_sample(
                         octaveSampler->sampler_permutations + 256 * m,
                         octaveSampler->sampler_originX[m],
                         octaveSampler->sampler_originY[m],
                         octaveSampler->sampler_originZ[m],
                         n, o, p, q, (double)j * q) /
                     g;
            }
        }

        g /= 2.0;
    }

    return c2me_natives_clampedLerp(d / 512.0, e / 512.0, h) / 128.0;
}

double c2me_natives_double_sample(
    octave_sampler_data *firstSampler, octave_sampler_data *secondSampler,
    double x, double y, double z, double amplitude)
{
    double d = x * 1.0181268882175227;
    double e = y * 1.0181268882175227;
    double f = z * 1.0181268882175227;

    return (c2me_natives_octave_sample(firstSampler, x, y, z) + c2me_natives_octave_sample(secondSampler, d, e, f)) * amplitude;
}
