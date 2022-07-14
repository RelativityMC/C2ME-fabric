#include "../../include/density_functions.h"
#include "../../include/common_maths.h"
#include "../../include/noise.h"

typedef struct {
    bool isNull;
    octave_sampler_data *firstSampler, *secondSampler;
    double amplitude;
} dfi_simple_shifted_noise_data;

static density_function_impl_data __attribute__((malloc)) *
create_data_template(bool isNull, octave_sampler_data *firstSampler,
                     octave_sampler_data *secondSampler, double amplitude);

long c2me_natives_sizeof_dfi_simple_shifted_noise_data() {
    return sizeof(dfi_simple_shifted_noise_data);
}

static __attribute__((pure)) double c2me_natives_dfi_shifted0_single_op(void *instance, int x, int y, int z) {
    dfi_simple_shifted_noise_data *data = instance;
    if (data->isNull) {
        return 0;
    } else {
        return math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                               x * 0.25, y * 0.25, z * 0.25,
                                               data->amplitude) * 4.0;
    }
}

static __attribute__((pure)) void
c2me_natives_dfi_shifted0_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_simple_shifted_noise_data *data = instance;
    if (data->isNull) {
        memset(res, 0, sizeof(double) * length); // assumes IEEE 754 double precision floating point format
    } else {
        for (size_t i = 0; i < length; ++i) {
            res[i] = math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                     poses[i].x * 0.25, poses[i].y * 0.25, poses[i].z * 0.25,
                                                     data->amplitude) * 4.0;
        }
    }
}

static __attribute__((pure)) double c2me_natives_dfi_shiftedA_single_op(void *instance, int x, int y, int z) {
    dfi_simple_shifted_noise_data *data = instance;
    if (data->isNull) {
        return 0;
    } else {
        return math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                               x * 0.25, 0.0, z * 0.25,
                                               data->amplitude) * 4.0;
    }
}

static __attribute__((pure)) void
c2me_natives_dfi_shiftedA_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_simple_shifted_noise_data *data = instance;
    if (data->isNull) {
        memset(res, 0, sizeof(double) * length); // assumes IEEE 754 double precision floating point format
    } else {
        int lastX = NAN, lastZ = NAN;
        double lastVal;
        for (size_t i = 0; i < length; ++i) {
            if (poses[i].x == lastX && poses[i].z == lastZ) {
                res[i] = lastVal;
            } else {
                lastVal = math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                          poses[i].x * 0.25, 0.0, poses[i].z * 0.25,
                                                          data->amplitude) * 4.0;
                lastX = poses[i].x;
                lastZ = poses[i].z;
            }
        }
    }
}

static __attribute__((pure)) double c2me_natives_dfi_shiftedB_single_op(void *instance, int x, int y, int z) {
    dfi_simple_shifted_noise_data *data = instance;
    if (data->isNull) {
        return 0;
    } else {
        return math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                               x * 0.25, y * 0.25, 0.0,
                                               data->amplitude) * 4.0;
    }
}

static __attribute__((pure)) void
c2me_natives_dfi_shiftedB_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_simple_shifted_noise_data *data = instance;
    if (data->isNull) {
        memset(res, 0, sizeof(double) * length); // assumes IEEE 754 double precision floating point format
    } else {
        for (size_t i = 0; i < length; ++i) {
            res[i] = math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                     poses[i].x * 0.25, poses[i].y * 0.25, 0.0,
                                                     data->amplitude) * 4.0;
        }
    }
}

density_function_impl_data __attribute__((malloc)) *c2me_natives_create_dfi_shifted0_data(bool isNull,
                                                                                          octave_sampler_data *firstSampler,
                                                                                          octave_sampler_data *secondSampler,
                                                                                          double amplitude) {
    density_function_impl_data *dfi = create_data_template(isNull, firstSampler, secondSampler, amplitude);

    dfi->single_op = c2me_natives_dfi_shifted0_single_op;
    dfi->multi_op = c2me_natives_dfi_shifted0_multi_op;
    return dfi;
}

density_function_impl_data __attribute__((malloc)) *c2me_natives_create_dfi_shiftedA_data(bool isNull,
                                                                                          octave_sampler_data *firstSampler,
                                                                                          octave_sampler_data *secondSampler,
                                                                                          double amplitude) {
    density_function_impl_data *dfi = create_data_template(isNull, firstSampler, secondSampler, amplitude);

    dfi->single_op = c2me_natives_dfi_shiftedA_single_op;
    dfi->multi_op = c2me_natives_dfi_shiftedA_multi_op;
    return dfi;
}

density_function_impl_data __attribute__((malloc)) *c2me_natives_create_dfi_shiftedB_data(bool isNull,
                                                                                          octave_sampler_data *firstSampler,
                                                                                          octave_sampler_data *secondSampler,
                                                                                          double amplitude) {
    density_function_impl_data *dfi = create_data_template(isNull, firstSampler, secondSampler, amplitude);

    dfi->single_op = c2me_natives_dfi_shiftedB_single_op;
    dfi->multi_op = c2me_natives_dfi_shiftedB_multi_op;
    return dfi;
}

static density_function_impl_data __attribute__((malloc)) *
create_data_template(bool isNull, octave_sampler_data *firstSampler,
                     octave_sampler_data *secondSampler, double amplitude) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_simple_shifted_noise_data));

    dfi_simple_shifted_noise_data *data = ptr + sizeof(density_function_impl_data);
    data->isNull = isNull;
    data->firstSampler = firstSampler;
    data->secondSampler = secondSampler;
    data->amplitude = amplitude;

    density_function_impl_data *dfi = ptr;
    dfi->instance = data;
    return dfi;
}
