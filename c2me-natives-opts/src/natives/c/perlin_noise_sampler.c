#include <math.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <stdio.h>

#include "../include/common_maths.h"
#include "../include/noise.h"

double __attribute__((pure))
c2me_natives_perlin_sample(const __uint8_t *permutations, double originX, double originY, double originZ, double x,
                           double y,
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

octave_sampler_data __attribute__((malloc)) *c2me_natives_perlin_create_octave_sampler_data(
        double lacunarity, double persistence, const size_t length, size_t octave_length, const size_t *indexes,
        const __uint8_t *sampler_permutations,
        const double *sampler_originX, const double *sampler_originY, const double *sampler_originZ,
        const double *amplitudes) {
    octave_sampler_data *ptr = malloc(sizeof(octave_sampler_data));
    octave_sampler_data init = {
            .lacunarity = lacunarity,
            .persistence = persistence,
            .length = length,
            .octave_length = octave_length,
            .indexes = indexes,
            .sampler_permutations = sampler_permutations,
            .sampler_originX = sampler_originX,
            .sampler_originY = sampler_originY,
            .sampler_originZ = sampler_originZ,
            .amplitudes = amplitudes,
    };
    memcpy(ptr, &init, sizeof(octave_sampler_data));

    return ptr;
}

double __attribute__((pure))
c2me_natives_perlin_octave_sample(octave_sampler_data *data, double x, double y, double z) {
    return math_noise_perlin_octave_sample(data, x, y, z);
}

interpolated_sampler_data __attribute__((malloc)) *c2me_natives_perlin_create_interpolated_sampler_data(
        const octave_sampler_data *lowerInterpolatedNoise,
        const octave_sampler_data *upperInterpolatedNoise,
        const octave_sampler_data *interpolationNoise,
        double field_38271,
        double field_38272,
        double xzScale,
        double yScale,
        double xzFactor,
        double yFactor,
        double smearScaleMultiplier,
        double maxValue) {
    interpolated_sampler_data *ptr = malloc(sizeof(interpolated_sampler_data));
    interpolated_sampler_data init = {
            .lowerInterpolatedNoise = lowerInterpolatedNoise,
            .upperInterpolatedNoise = upperInterpolatedNoise,
            .interpolationNoise = interpolationNoise,
            .field_38271 = field_38271,
            .field_38272 = field_38272,
            .xzScale = xzScale,
            .yScale = yScale,
            .xzFactor = xzFactor,
            .yFactor = yFactor,
            .smearScaleMultiplier = smearScaleMultiplier,
            .maxValue = maxValue,
    };
    memcpy(ptr, &init, sizeof(interpolated_sampler_data));

    return ptr;
}

double __attribute__((pure))
c2me_natives_perlin_interpolated_sample(const interpolated_sampler_data *data, int x, int y, int z) {
    return math_noise_perlin_interpolated_sample(data, x, y, z);
}

double __attribute__((pure)) c2me_natives_perlin_double_sample(
        const octave_sampler_data *firstSampler, const octave_sampler_data *secondSampler,
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
