#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    density_function_impl_data *input;
    double minInclusive, maxExclusive;
    density_function_impl_data *whenInRange, *whenOutOfRange;
} dfi_range_choice_data;

long c2me_natives_sizeof_dfi_range_choice_data() {
    return sizeof(dfi_range_choice_data);
}

static double c2me_natives_dfi_range_choice_single_op(void *instance, int x, int y, int z) {
    dfi_range_choice_data *data = instance;
    double input = c2me_natives_dfi_bindings_single_op(data->input, x, y, z);
    return input >= data->minInclusive && input < data->maxExclusive
           ? c2me_natives_dfi_bindings_single_op(data->whenInRange, x, y, z)
           : c2me_natives_dfi_bindings_single_op(data->whenOutOfRange, x, y, z);
}

static void c2me_natives_dfi_range_choice_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_range_choice_data *data = instance;
    c2me_natives_dfi_bindings_multi_op_provided(data->input, poses, res, length);
    for (size_t i = 0; i < length; i++) {
        double input = res[i];
        res[i] = input >= data->minInclusive && input < data->maxExclusive
                 ? c2me_natives_dfi_bindings_single_op(data->whenInRange, poses[i].x, poses[i].y, poses[i].z)
                 : c2me_natives_dfi_bindings_single_op(data->whenOutOfRange, poses[i].x, poses[i].y, poses[i].z);
    }
}

density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_range_choice_data(density_function_impl_data *input,
                                          double minInclusive, double maxExclusive,
                                          density_function_impl_data *whenInRange,
                                          density_function_impl_data *whenOutOfRange) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_range_choice_data));

    dfi_range_choice_data *data = ptr + sizeof(density_function_impl_data);
    data->input = input;
    data->minInclusive = minInclusive;
    data->maxExclusive = maxExclusive;
    data->whenInRange = whenInRange;
    data->whenOutOfRange = whenOutOfRange;

    density_function_impl_data *dfi = ptr;
    dfi->instance = data;
    dfi->single_op = c2me_natives_dfi_range_choice_single_op;
    dfi->multi_op = c2me_natives_dfi_range_choice_multi_op;

    return dfi;
}
