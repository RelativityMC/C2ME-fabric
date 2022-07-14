#include <math.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <time.h>

#include "../include/common_maths.h"
#include "../include/noise.h"

double __attribute__((pure)) c2me_natives_simplex_sample(const int *permutations, double x, double y) {
    return math_noise_simplex_sample(permutations, x, y);
}

float __attribute__((pure)) c2me_natives_end_noise_sample(const int *permutations, int i, int j) {
    return math_noise_end_noise_sample(permutations, i, j);
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

//double c2me_natives_the_end_benchmark(int *permutations) {
//    struct timespec start;
//    struct timespec end;
//
//    clock_gettime(CLOCK_REALTIME, &start);
//
//    size_t count = (1 << 16);
//
//    for (size_t i = 0; i < count; i++)
//    {
//        volatile float res = c2me_natives_end_noise_sample(permutations, 20000, 20000);
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
//        volatile double res = c2me_natives_simplex_sample(permutations, 20000, 20000);
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
//    int *permutations;
//    permutations = c2me_natives_simplex_generatePermutations();
//
//    printf("Simplex noise: \n");
//    for (size_t i = 0; i < 8; ++i) {
//        printf("%.2f ns/op\n", c2me_natives_simplex_benchmark(permutations));
//    }
//
//    srand(1024);
//    permutations = c2me_natives_simplex_generatePermutations();
//    printf("The end noise: \n");
//    for (size_t i = 0; i < 8; ++i) {
//        printf("%.2f ns/op\n", c2me_natives_the_end_benchmark(permutations));
//    }
//
//    return 0;
//}
