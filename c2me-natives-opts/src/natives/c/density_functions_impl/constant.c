#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    double constant;
} dfi_constant_data;

long c2me_natives_sizeof_dfi_constant_data() {
    return sizeof(dfi_constant_data);
}

double c2me_natives_dfi_constant_single_op(void *instance, int x, int y, int z){
    dfi_constant_data *data = instance;
    return data->constant;
}

void c2me_natives_dfi_constant_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_constant_data *data = instance;
    for (size_t i = 0; i < length; i++) {
        res[i] = data->constant;
    }
}

density_function_impl_data *c2me_natives_create_dfi_constant(double constant) {
    void* ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_constant_data));

    dfi_constant_data *data = ptr + sizeof(density_function_impl_data);
    data->constant = constant;

    density_function_impl_data *dfi = ptr;
    dfi->instance = data;
    dfi->single_op = c2me_natives_dfi_constant_single_op;
    dfi->multi_op = c2me_natives_dfi_constant_multi_op;
    return dfi;
}
