#include "../../include/density_functions.h"
#include "../../include/common_maths.h"
#include "../../include/noise.h"

typedef struct {
    bool isNull;
    octave_sampler_data *firstSampler, *secondSampler;
    double amplitude;
    double xzScale, yScale;
} dfi_noise_data;

long c2me_natives_sizeof_dfi_noise_data() {
    return sizeof(dfi_noise_data);
}

static double c2me_natives_dfi_noise_single_op(void *instance, int x, int y, int z) {
    dfi_noise_data *data = instance;
    if (data->isNull) {
        return 0;
    } else {
        return math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                 x * data->xzScale, y * data->yScale, z * data->xzScale,
                                                 data->amplitude);
    }
}

static void c2me_natives_dfi_noise_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_noise_data *data = instance;
    if (data->isNull) {
//        for (size_t i = 0; i < length; i++) {
//            res[i] = 0;
//        }
        memset(res, 0, sizeof(double) * length); // assumes IEEE 754 double precision floating point format
    } else {
        for (size_t i = 0; i < length; i++) {
            res[i] = math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                       poses[i].x * data->xzScale, poses[i].y * data->yScale,
                                                       poses[i].z * data->xzScale,
                                                       data->amplitude);
        }
    }
}

density_function_impl_data *c2me_natives_create_dfi_noise_data(
        bool isNull, octave_sampler_data *firstSampler, octave_sampler_data *secondSampler, double amplitude,
        double xzScale, double yScale) {
    void* ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_noise_data));
    dfi_noise_data *data = ptr + sizeof(density_function_impl_data);
    data->isNull = isNull;
    data->firstSampler = firstSampler;
    data->secondSampler = secondSampler;
    data->amplitude = amplitude;
    data->xzScale = xzScale;
    data->yScale = yScale;

    density_function_impl_data *dfi = ptr;
    dfi->instance = data;
    dfi->single_op = c2me_natives_dfi_noise_single_op;
    dfi->multi_op = c2me_natives_dfi_noise_multi_op;
    return dfi;
}


