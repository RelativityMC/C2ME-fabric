#include "../../include/density_functions.h"
#include "../../include/common_maths.h"
#include "../../include/noise.h"

typedef struct {
    density_function_impl_data *input;

    bool isNull;
    octave_sampler_data *firstSampler, *secondSampler;
    double amplitude;
} dfi_weird_scaled_sampler;

long c2me_natives_sizeof_dfi_weird_scaled_sampler() {
    return sizeof(dfi_weird_scaled_sampler);
}

static double cave_scaler_tunnels(double value) {
    if (value < -0.5) {
        return 0.75;
    } else if (value < 0.0) {
        return 1.0;
    } else {
        return value < 0.5 ? 1.5 : 2.0;
    }
}

static double cave_scaler_caves(double value) {
    if (value < -0.75) {
        return 0.5;
    } else if (value < -0.5) {
        return 0.75;
    } else if (value < 0.5) {
        return 1.0;
    } else {
        return value < 0.75 ? 2.0 : 3.0;
    }
}

static double c2me_natives_dfi_weird_scaled_samplers_tunnels_single_op(void *instance, int x, int y, int z) {
    dfi_weird_scaled_sampler *data = instance;
    double input = c2me_natives_dfi_bindings_single_op(data->input, x, y, z);
    double scale = cave_scaler_tunnels(input);
    return scale * math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                   x / scale, y / scale,
                                                   z / scale,
                                                   data->amplitude);
}

static double c2me_natives_dfi_weird_scaled_samplers_caves_single_op(void *instance, int x, int y, int z) {
    dfi_weird_scaled_sampler *data = instance;
    double input = c2me_natives_dfi_bindings_single_op(data->input, x, y, z);
    double scale = cave_scaler_caves(input);
    return scale * math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                   x / scale, y / scale,
                                                   z / scale,
                                                   data->amplitude);
}

static void
c2me_natives_dfi_weird_scaled_samplers_tunnels_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_weird_scaled_sampler *data = instance;

    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);

    for (size_t i = 0; i < length; ++i) {
        double scale = cave_scaler_tunnels(res[i]);
        res[i] = scale * math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                         poses[i].x / scale,
                                                         poses[i].y / scale,
                                                         poses[i].z / scale,
                                                         data->amplitude);
    }
}

static void
c2me_natives_dfi_weird_scaled_samplers_caves_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_weird_scaled_sampler *data = instance;

    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);

    for (size_t i = 0; i < length; ++i) {
        double scale = cave_scaler_caves(res[i]);
        res[i] = scale * math_noise_perlin_double_sample(data->firstSampler, data->secondSampler,
                                                         poses[i].x / scale,
                                                         poses[i].y / scale,
                                                         poses[i].z / scale,
                                                         data->amplitude);
    }
}

static const density_function_single_op single_ops[] = {
        c2me_natives_dfi_weird_scaled_samplers_tunnels_single_op,
        c2me_natives_dfi_weird_scaled_samplers_caves_single_op,
};

static const density_function_multi_op multi_ops[] = {
        c2me_natives_dfi_weird_scaled_samplers_tunnels_multi_op,
        c2me_natives_dfi_weird_scaled_samplers_caves_multi_op,
};

density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_weird_scaled_sampler_data(density_function_impl_data *input, short operation,
                                                  bool isNull, octave_sampler_data *firstSampler,
                                                  octave_sampler_data *secondSampler, double amplitude) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_weird_scaled_sampler));

    dfi_weird_scaled_sampler *data = ptr + sizeof(density_function_impl_data);
    data->input = input;
    data->isNull = isNull;
    data->firstSampler = firstSampler;
    data->secondSampler = secondSampler;
    data->amplitude = amplitude;

    density_function_impl_data *dfi = ptr;
    dfi->instance = data;
    dfi->single_op = single_ops[operation];
    dfi->multi_op = multi_ops[operation];

    return dfi;
}

