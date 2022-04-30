#include <math.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#include "../includes/common_maths.h"

double c2me_natives_simplex_sample(int *permutations, double x, double y) {
    double var0 = (x + y) * SKEW_FACTOR_2D;
    double var1 = floor(x + var0);
    double var2 = floor(y + var0);
    double var3 = (var1 + var2) * UNSKEW_FACTOR_2D;
    double var4 = x - var1 + var3;
    double var5 = y - var2 + var3;
    int var6;
    int var7;
    if (var4 > var5) {
        var6 = 1;
        var7 = 0;
    } else {
        var6 = 0;
        var7 = 1;
    }

    double var8 = var4 - (double) var6 + UNSKEW_FACTOR_2D;
    double var9 = var5 - (double) var7 + UNSKEW_FACTOR_2D;
    double var10 = var4 - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
    double var11 = var5 - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
    int var12 = ((int) var1) & 0xFF;
    int var13 = ((int) var2) & 0xFF;
    int var14 = (var13 + var7) & 255;
    int var15 = (var13 + 1) & 255;
    int var16 = permutations[var13 & 255];
    int var17 = permutations[var14];
    int var18 = permutations[var15];
    int var19 = (var12 + var16) & 255;
    int var20 = (var12 + var6 + var17) & 255;
    int var21 = (var12 + 1 + var18) & 255;
    int var22 = permutations[var19] % 12;
    int var23 = permutations[var20] % 12;
    int ver24 = permutations[var21] % 12;
    double var25 = c2me_natives_simplex_grad(var22, var4, var5, 0.0, 0.5);
    double var26 = c2me_natives_simplex_grad(var23, var8, var9, 0.0, 0.5);
    double var27 = c2me_natives_simplex_grad(ver24, var10, var11, 0.0, 0.5);
    return 70.0 * (var25 + var26 + var27);
}

int *c2me_natives_simplex_generatePermutations() {
    int *permutations = malloc(256 * sizeof(int));

    for (size_t i = 0; i < 256; i++) {
        permutations[i] = i;
    }

    for (size_t i = 0; i < 256; i++) {
        int j = rand() % (256 - i);
        int k = permutations[i];
        permutations[i] = permutations[j + i];
        permutations[j + i] = k;
    }

    return permutations;
}

//double c2me_natives_simplex_benchmark(int *permutations) {
//    struct timespec start;
//    struct timespec end;
//
//    clock_gettime(CLOCK_REALTIME, &start);
//
//    size_t count = (1 << 26);
//
//    for (size_t i = 0; i < count; i++)
//    {
//        volatile double res = c2me_natives_simplex_sample(permutations, 20, 200);
//        res = res;
//    }
//
//    clock_gettime(CLOCK_REALTIME, &end);
//
//    __uint64_t timeElapsed = (end.tv_sec * BILLION + end.tv_nsec) - (start.tv_sec * BILLION + start.tv_nsec);
//
//    return timeElapsed / (double)count;
//}
//
//int main() {
//    c2me_natives_init();
//
//    srand(1024);
//    int *permutations = c2me_natives_simplex_generatePermutations();
//
//    for (size_t i = 0; i < 8; ++i) {
//        printf("%.2f ns/op\n", c2me_natives_simplex_benchmark(permutations));
//    }
//
//    return 0;
//}
