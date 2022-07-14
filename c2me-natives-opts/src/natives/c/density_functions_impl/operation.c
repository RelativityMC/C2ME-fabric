#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    density_function_impl_data *input;
    double constantArgument;
} dfi_operation_half_data;

typedef struct {
    density_function_impl_data *input1;
    density_function_impl_data *input2;
} dfi_operation_full_data;

long c2me_natives_sizeof_dfi_operation_half_data() {
    return sizeof(dfi_operation_half_data);
}

long c2me_natives_sizeof_dfi_operation_full_data() {
    return sizeof(dfi_operation_full_data);
}

static double c2me_natives_dfi_operation_half_add_single_op(void *instance, int x, int y, int z) {
    dfi_operation_half_data *data = (dfi_operation_half_data *) instance;
    return c2me_natives_dfi_bindings_single_op(data->input, x, y, z) + data->constantArgument;
}

static void c2me_natives_dfi_operation_half_add_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_operation_half_data *data = (dfi_operation_half_data *) instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);
    for (size_t i = 0; i < length; i++) {
        res[i] += data->constantArgument;
    }
}

static double c2me_natives_dfi_operation_half_mul_single_op(void *instance, int x, int y, int z) {
    dfi_operation_half_data *data = (dfi_operation_half_data *) instance;
    return c2me_natives_dfi_bindings_single_op(data->input, x, y, z) * data->constantArgument;
}

static void c2me_natives_dfi_operation_half_mul_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_operation_half_data *data = (dfi_operation_half_data *) instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);
    for (size_t i = 0; i < length; i++) {
        res[i] *= data->constantArgument;
    }
}

static double c2me_natives_dfi_operation_half_max_single_op(void *instance, int x, int y, int z) {
    dfi_operation_half_data *data = (dfi_operation_half_data *) instance;
    return fmax(c2me_natives_dfi_bindings_single_op(data->input, x, y, z), data->constantArgument);
}

static void c2me_natives_dfi_operation_half_max_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_operation_half_data *data = (dfi_operation_half_data *) instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);
    for (size_t i = 0; i < length; i++) {
        res[i] = fmax(res[i], data->constantArgument);
    }
}

static double c2me_natives_dfi_operation_half_min_single_op(void *instance, int x, int y, int z) {
    dfi_operation_half_data *data = (dfi_operation_half_data *) instance;
    return fmin(c2me_natives_dfi_bindings_single_op(data->input, x, y, z), data->constantArgument);
}

static void c2me_natives_dfi_operation_half_min_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_operation_half_data *data = (dfi_operation_half_data *) instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);
    for (size_t i = 0; i < length; i++) {
        res[i] = fmin(res[i], data->constantArgument);
    }
}

static double c2me_natives_dfi_operation_full_add_single_op(void *instance, int x, int y, int z) {
    dfi_operation_full_data *data = (dfi_operation_full_data *) instance;
    return c2me_natives_dfi_bindings_single_op(data->input1, x, y, z) +
           c2me_natives_dfi_bindings_single_op(data->input2, x, y, z);
}

static void c2me_natives_dfi_operation_full_add_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_operation_full_data *data = (dfi_operation_full_data *) instance;
    double *res2 = malloc(sizeof(double) * length);
    c2me_natives_dfi_bindings_multi_op_provided(data->input1, poses, res, length);
    c2me_natives_dfi_bindings_multi_op_provided(data->input2, poses, res2, length);
    for (size_t i = 0; i < length; i++) {
        res[i] += res2[i];
    }
    free(res2);
}

static double c2me_natives_dfi_operation_full_mul_single_op(void *instance, int x, int y, int z) {
    dfi_operation_full_data *data = (dfi_operation_full_data *) instance;
    return c2me_natives_dfi_bindings_single_op(data->input1, x, y, z) *
           c2me_natives_dfi_bindings_single_op(data->input2, x, y, z);
}

static void c2me_natives_dfi_operation_full_mul_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_operation_full_data *data = (dfi_operation_full_data *) instance;
    double *res2 = malloc(sizeof(double) * length);
    c2me_natives_dfi_bindings_multi_op_provided(data->input1, poses, res, length);
    c2me_natives_dfi_bindings_multi_op_provided(data->input2, poses, res2, length);
    for (size_t i = 0; i < length; i++) {
        res[i] *= res2[i];
    }
    free(res2);
}

static double c2me_natives_dfi_operation_full_max_single_op(void *instance, int x, int y, int z) {
    dfi_operation_full_data *data = (dfi_operation_full_data *) instance;
    return fmax(c2me_natives_dfi_bindings_single_op(data->input1, x, y, z),
                c2me_natives_dfi_bindings_single_op(data->input2, x, y, z));
}

static void c2me_natives_dfi_operation_full_max_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_operation_full_data *data = (dfi_operation_full_data *) instance;
    double *res2 = malloc(sizeof(double) * length);
    c2me_natives_dfi_bindings_multi_op_provided(data->input1, poses, res, length);
    c2me_natives_dfi_bindings_multi_op_provided(data->input2, poses, res2, length);
    for (size_t i = 0; i < length; i++) {
        res[i] = fmax(res[i], res2[i]);
    }
    free(res2);
}

static double c2me_natives_dfi_operation_full_min_single_op(void *instance, int x, int y, int z) {
    dfi_operation_full_data *data = (dfi_operation_full_data *) instance;
    return fmin(c2me_natives_dfi_bindings_single_op(data->input1, x, y, z),
                c2me_natives_dfi_bindings_single_op(data->input2, x, y, z));
}

static void c2me_natives_dfi_operation_full_min_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_operation_full_data *data = (dfi_operation_full_data *) instance;
    double *res2 = malloc(sizeof(double) * length);
    c2me_natives_dfi_bindings_multi_op_provided(data->input1, poses, res, length);
    c2me_natives_dfi_bindings_multi_op_provided(data->input2, poses, res2, length);
    for (size_t i = 0; i < length; i++) {
        res[i] = fmin(res[i], res2[i]);
    }
    free(res2);
}

static const density_function_single_op half_single_op[] = {
        c2me_natives_dfi_operation_half_add_single_op,
        c2me_natives_dfi_operation_half_mul_single_op,
        c2me_natives_dfi_operation_half_max_single_op,
        c2me_natives_dfi_operation_half_min_single_op
};

static const density_function_single_op full_single_op[] = {
        c2me_natives_dfi_operation_full_add_single_op,
        c2me_natives_dfi_operation_full_mul_single_op,
        c2me_natives_dfi_operation_full_max_single_op,
        c2me_natives_dfi_operation_full_min_single_op
};

static const density_function_multi_op half_multi_op[] = {
        c2me_natives_dfi_operation_half_add_multi_op,
        c2me_natives_dfi_operation_half_mul_multi_op,
        c2me_natives_dfi_operation_half_max_multi_op,
        c2me_natives_dfi_operation_half_min_multi_op
};

static const density_function_multi_op full_multi_op[] = {
        c2me_natives_dfi_operation_full_add_multi_op,
        c2me_natives_dfi_operation_full_mul_multi_op,
        c2me_natives_dfi_operation_full_max_multi_op,
        c2me_natives_dfi_operation_full_min_multi_op
};


density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_operation_half(short operation, density_function_impl_data *input, double constantArgument) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_operation_half_data));

    dfi_operation_half_data *data = ptr + sizeof(density_function_impl_data);
    data->input = input;
    data->constantArgument = constantArgument;

    density_function_impl_data *impl = ptr;
    impl->instance = data;
    impl->single_op = half_single_op[operation];
    impl->multi_op = half_multi_op[operation];

    return impl;
}

density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_operation_full(short operation, density_function_impl_data *input1,
                                       density_function_impl_data *input2) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_operation_full_data));

    dfi_operation_full_data *data = ptr + sizeof(density_function_impl_data);
    data->input1 = input1;
    data->input2 = input2;

    density_function_impl_data *impl = ptr;
    impl->instance = data;
    impl->single_op = full_single_op[operation];
    impl->multi_op = full_multi_op[operation];

    return impl;
}


