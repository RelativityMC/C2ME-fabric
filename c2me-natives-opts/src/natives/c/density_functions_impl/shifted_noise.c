#include "../../include/density_functions.h"
#include "../../include/common_maths.h"
#include "../../include/noise.h"

typedef struct {
    bool isNull;
    density_function_impl_data *shift_x, *shift_y, *shift_z;
    double xz_scale, y_scale;
    octave_sampler_data *firstSampler, *secondSampler;
    double amplitude;
} dfi_shifted_noise_data;

long c2me_natives_sizeof_dfi_shifted_noise_data() {
    return sizeof(dfi_shifted_noise_data);
}

static double c2me_natives_dfi_shifted_noise_single_op(void *instance, int x, int y, int z) {
    dfi_shifted_noise_data *data = instance;
    if (data->isNull) {
        return 0;
    }
    double d = x * data->xz_scale + c2me_natives_dfi_bindings_single_op(data->shift_x, x, y, z);
    double e = y * data->y_scale + c2me_natives_dfi_bindings_single_op(data->shift_y, x, y, z);
    double f = z * data->xz_scale + c2me_natives_dfi_bindings_single_op(data->shift_z, x, y, z);

    return math_noise_perlin_double_sample(data->firstSampler, data->secondSampler, d, e, f, data->amplitude);
}

static void c2me_natives_dfi_shifted_noise_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_shifted_noise_data *data = instance;
    if (data->isNull) {
        memset(res, 0, sizeof(double) * length); // assumes IEEE 754 double precision floating point format
        return;
    }

    for (size_t i = 0; i < length; i++) {
        noise_pos *pos = poses + i;

        double d = pos->x * data->xz_scale + c2me_natives_dfi_bindings_single_op(data->shift_x, pos->x, pos->y, pos->z);
        double e = pos->y * data->y_scale + c2me_natives_dfi_bindings_single_op(data->shift_y, pos->x, pos->y, pos->z);
        double f = pos->z * data->xz_scale + c2me_natives_dfi_bindings_single_op(data->shift_z, pos->x, pos->y, pos->z);

        res[i] = math_noise_perlin_double_sample(data->firstSampler, data->secondSampler, d, e, f, data->amplitude);
    }
}

density_function_impl_data *c2me_natives_create_dfi_shifted_noise_data(bool isNull,
                                                                       density_function_impl_data *shift_x,
                                                                       density_function_impl_data *shift_y,
                                                                       density_function_impl_data *shift_z,
                                                                       double xz_scale,
                                                                       double y_scale,
                                                                       octave_sampler_data *firstSampler,
                                                                       octave_sampler_data *secondSampler,
                                                                       double amplitude) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_shifted_noise_data));

    dfi_shifted_noise_data *data = ptr + sizeof(density_function_impl_data);
    data->isNull = isNull;
    data->shift_x = shift_x;
    data->shift_y = shift_y;
    data->shift_z = shift_z;
    data->xz_scale = xz_scale;
    data->y_scale = y_scale;
    data->firstSampler = firstSampler;
    data->secondSampler = secondSampler;
    data->amplitude = amplitude;

    density_function_impl_data *impl = ptr;
    impl->instance = data;
    impl->single_op = c2me_natives_dfi_shifted_noise_single_op;
    impl->multi_op = c2me_natives_dfi_shifted_noise_multi_op;

    return impl;
}
