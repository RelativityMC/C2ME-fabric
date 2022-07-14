#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    density_function_impl_data *input;
} dfi_single_operation_data;

long c2me_natives_sizeof_dfi_single_operation_data() {
    return sizeof(dfi_single_operation_data);
}

static double c2me_natives_dfi_single_operation_abs_single_op(void *instance, int x, int y, int z) {
    dfi_single_operation_data *data = instance;

    return fabs(c2me_natives_dfi_bindings_single_op(data->input, x, y, z));
}

static void
c2me_natives_dfi_single_operation_abs_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_single_operation_data *data = instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);

    for (size_t i = 0; i < length; i++) {
        res[i] = fabs(res[i]);
    }
}

static double c2me_natives_dfi_single_operation_square_single_op(void *instance, int x, int y, int z) {
    dfi_single_operation_data *data = instance;

    double d = c2me_natives_dfi_bindings_single_op(data->input, x, y, z);
    return d * d;
}

static void
c2me_natives_dfi_single_operation_square_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_single_operation_data *data = instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);

    for (size_t i = 0; i < length; i++) {
        res[i] = res[i] * res[i];
    }
}

static double c2me_natives_dfi_single_operation_cube_single_op(void *instance, int x, int y, int z) {
    dfi_single_operation_data *data = instance;

    double d = c2me_natives_dfi_bindings_single_op(data->input, x, y, z);
    return d * d * d;
}

static void
c2me_natives_dfi_single_operation_cube_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_single_operation_data *data = instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);

    for (size_t i = 0; i < length; i++) {
        res[i] = res[i] * res[i] * res[i];
    }
}

static double c2me_natives_dfi_single_operation_half_negative_single_op(void *instance, int x, int y, int z) {
    dfi_single_operation_data *data = instance;

    double d = c2me_natives_dfi_bindings_single_op(data->input, x, y, z);
    return d > 0.0 ? d : d * 0.5;
}

static void
c2me_natives_dfi_single_operation_half_negative_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_single_operation_data *data = instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);

    for (size_t i = 0; i < length; i++) {
        res[i] = res[i] > 0.0 ? res[i] : res[i] * 0.5;
    }
}

static double c2me_natives_dfi_single_operation_quarter_negative_single_op(void *instance, int x, int y, int z) {
    dfi_single_operation_data *data = instance;

    double d = c2me_natives_dfi_bindings_single_op(data->input, x, y, z);
    return d > 0.0 ? d : d * 0.25;
}

static void
c2me_natives_dfi_single_operation_quarter_negative_multi_op(void *instance, double *res, noise_pos *poses,
                                                            size_t length) {
    dfi_single_operation_data *data = instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);

    for (size_t i = 0; i < length; i++) {
        res[i] = res[i] > 0.0 ? res[i] : res[i] * 0.25;
    }
}

static double c2me_natives_dfi_single_operation_squeeze_single_op(void *instance, int x, int y, int z) {
    dfi_single_operation_data *data = instance;

    double d = math_clamp(c2me_natives_dfi_bindings_single_op(data->input, x, y, z), -1.0, 1.0);
    return d / 2.0 - d * d * d / 24.0;
}

static void
c2me_natives_dfi_single_operation_squeeze_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_single_operation_data *data = instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);

    for (size_t i = 0; i < length; i++) {
        double d = math_clamp(res[i], -1.0, 1.0);
        res[i] = d / 2.0 - d * d * d / 24.0;
    }
}

static const density_function_single_op single_ops[] = {
        c2me_natives_dfi_single_operation_abs_single_op,
        c2me_natives_dfi_single_operation_square_single_op,
        c2me_natives_dfi_single_operation_cube_single_op,
        c2me_natives_dfi_single_operation_half_negative_single_op,
        c2me_natives_dfi_single_operation_quarter_negative_single_op,
        c2me_natives_dfi_single_operation_squeeze_single_op,
};

static const density_function_multi_op multi_ops[] = {
        c2me_natives_dfi_single_operation_abs_multi_op,
        c2me_natives_dfi_single_operation_square_multi_op,
        c2me_natives_dfi_single_operation_cube_multi_op,
        c2me_natives_dfi_single_operation_half_negative_multi_op,
        c2me_natives_dfi_single_operation_quarter_negative_multi_op,
        c2me_natives_dfi_single_operation_squeeze_multi_op,
};

density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_single_operation(short operation, density_function_impl_data *input) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_single_operation_data));

    dfi_single_operation_data *data = ptr + sizeof(density_function_impl_data);
    data->input = input;

    density_function_impl_data *impl = ptr;
    impl->instance = data;
    impl->single_op = single_ops[operation];
    impl->multi_op = multi_ops[operation];

    return impl;
}
