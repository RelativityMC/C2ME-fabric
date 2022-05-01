#include <stdlib.h>
#include <stdint.h>

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

extern double c2me_natives_perlin_double_sample(
        octave_sampler_data *firstSampler, octave_sampler_data *secondSampler,
        double x, double y, double z, double amplitude);

extern double c2me_natives_perlin_interpolated_sample(interpolated_sampler_data *data, int x, int y, int z);

extern double c2me_natives_simplex_sample(const int *permutations, double x, double y);

extern float c2me_natives_end_noise_sample(int *permutations, int i, int j);

#endif //C2ME_FABRIC_NOISE_STRUCTS_H
