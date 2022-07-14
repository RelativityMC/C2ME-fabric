#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    density_function_impl_data *input;
    double minValue, maxValue;
} dfi_clamp_data;

long c2me_natives_sizeof_dfi_clamp_data() {
    return sizeof(dfi_clamp_data);
}

static double c2me_natives_dfi_clamp_single_op(void *instance, int x, int y, int z) {
    dfi_clamp_data *data = (dfi_clamp_data *) instance;
    double value = c2me_natives_dfi_bindings_single_op(data->input, x, y, z);
    return math_clamp(value, data->minValue, data->maxValue);
}

static void c2me_natives_dfi_clamp_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_clamp_data *data = (dfi_clamp_data *) instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);
    for (size_t i = 0; i < length; i++) {
        res[i] = math_clamp(res[i], data->minValue, data->maxValue);
    }
}

density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_clamp(density_function_impl_data *input, double minValue, double maxValue) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_clamp_data));

    dfi_clamp_data *data = (dfi_clamp_data *) (ptr + sizeof(density_function_impl_data));
    data->input = input;
    data->minValue = minValue;
    data->maxValue = maxValue;

    density_function_impl_data *dfi = (density_function_impl_data *) ptr;
    dfi->instance = data;
    dfi->single_op = c2me_natives_dfi_clamp_single_op;
    dfi->multi_op = c2me_natives_dfi_clamp_multi_op;

    return dfi;
}
