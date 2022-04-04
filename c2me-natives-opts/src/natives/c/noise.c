#define _POSIX_C_SOURCE 199309L
#include <stdio.h>
#include <math.h>
#include <time.h>
#include <string.h>
#include <stdlib.h>
#define BILLION 1000000000L

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
