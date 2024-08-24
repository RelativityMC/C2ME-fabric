#include <ext_math.h>
#include <target_macros.h>

TARGET_IMPL(c2me_natives_noise_perlin_sample, double, (const uint8_t *const permutations, const double originX,
                                                       const double originY, const double originZ, const double x,
                                                       const double y, const double z, const double yScale,
                                                       const double yMax) {
    return math_noise_perlin_sample(permutations, originX, originY, originZ, x, y, z, yScale, yMax);
})

TARGET_IMPL(c2me_natives_noise_perlin_double_octave_sample, double, (const double_octave_sampler_data_t *const data,
                                                                     const double x, const double y, const double z) {
    return math_noise_perlin_double_octave_sample(data, x, y, z);
})
