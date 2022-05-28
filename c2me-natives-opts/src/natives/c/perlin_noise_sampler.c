#include <math.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <stdio.h>

#include "../include/common_maths.h"
#include "../include/noise.h"

double
c2me_natives_perlin_sample(__uint8_t *permutations, double originX, double originY, double originZ, double x, double y,
                           double z, double yScale, double yMax) {
    return math_noise_perlin_sample(permutations, originX, originY, originZ, x, y, z, yScale, yMax);
}

__uint8_t *c2me_natives_perlin_generatePermutations() {
    __uint8_t *permutations = malloc(256 * sizeof(__uint8_t));

    for (size_t i = 0; i < 256; i++) {
        permutations[i] = i;
    }

    for (size_t i = 0; i < 256; i++) {
        int j = rand() % (256 - i);
        __uint8_t b = permutations[i];
        permutations[i] = permutations[i + j];
        permutations[i + j] = b;
    }

    return permutations;
}

octave_sampler_data *c2me_natives_perlin_create_octave_sampler_data(
        double lacunarity, double persistence, size_t length, size_t octave_length, size_t *indexes, __uint8_t *sampler_permutations,
        double *sampler_originX, double *sampler_originY, double *sampler_originZ, double *amplitudes) {
    octave_sampler_data *ptr = malloc(sizeof(octave_sampler_data));
    ptr->lacunarity = lacunarity;
    ptr->persistence = persistence;
    ptr->length = length;
    ptr->octave_length = octave_length;
    ptr->indexes = indexes;
    ptr->sampler_permutations = sampler_permutations;
    ptr->sampler_originX = sampler_originX;
    ptr->sampler_originY = sampler_originY;
    ptr->sampler_originZ = sampler_originZ;
    ptr->amplitudes = amplitudes;
    return ptr;
}

double c2me_natives_perlin_octave_sample(octave_sampler_data *data, double x, double y, double z) {
    return math_noise_perlin_octave_sample(data, x, y, z);
}

interpolated_sampler_data *c2me_natives_perlin_create_interpolated_sampler_data(
        octave_sampler_data *lowerInterpolatedNoise, octave_sampler_data *upperInterpolatedNoise,
        octave_sampler_data *interpolationNoise,
        double field_38271,
        double field_38272,
        double xzScale,
        double yScale,
        double xzFactor,
        double yFactor,
        double smearScaleMultiplier,
        double maxValue) {
    interpolated_sampler_data *ptr = malloc(sizeof(interpolated_sampler_data));
    ptr->lowerInterpolatedNoise = lowerInterpolatedNoise;
    ptr->upperInterpolatedNoise = upperInterpolatedNoise;
    ptr->interpolationNoise = interpolationNoise;
    ptr->field_38271 = field_38271;
    ptr->field_38272 = field_38272;
    ptr->xzScale = xzScale;
    ptr->yScale = yScale;
    ptr->xzFactor = xzFactor;
    ptr->yFactor = yFactor;
    ptr->smearScaleMultiplier = smearScaleMultiplier;
    ptr->maxValue = maxValue;

    return ptr;
}

double c2me_natives_perlin_interpolated_sample(interpolated_sampler_data *data, int x, int y, int z) {
    return math_noise_perlin_interpolated_sample(data, x, y, z);
}

double c2me_natives_perlin_double_sample(
        octave_sampler_data *firstSampler, octave_sampler_data *secondSampler,
        double x, double y, double z, double amplitude) {
    return math_noise_perlin_double_sample(firstSampler, secondSampler, x, y, z, amplitude);
}

//double c2me_natives_perlin_benchmark(__uint8_t *permutations) {
//    struct timespec start;
//    struct timespec end;
//
//    clock_gettime(CLOCK_REALTIME, &start);
//
//    size_t count = (1 << 24);
//
//    for (size_t i = 0; i < count; i++)
//    {
//        volatile double res = c2me_natives_perlin_sample(permutations, 0, 0, 0, 40, 140, 20, 1.5, 40);
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
//    __uint8_t *permutations = c2me_natives_perlin_generatePermutations();
//
//    for (size_t i = 0; i < 8; ++i) {
//        printf("%.2f ns/op\n", c2me_natives_perlin_benchmark(permutations));
//    }
//
//    return 0;
//}
