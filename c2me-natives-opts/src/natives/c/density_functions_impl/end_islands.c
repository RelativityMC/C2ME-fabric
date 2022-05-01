#include "../../include/density_functions.h"
#include "../../include/common_maths.h"
#include "../../include/noise.h"

typedef struct {
    int *permutations;
} dfi_end_islands_data;

long c2me_natives_sizeof_dfi_end_islands_data() {
    return sizeof(dfi_end_islands_data);
}

double c2me_natives_dfi_end_islands_single_op(void *instance, int x, int y, int z){
    dfi_end_islands_data *data = instance;
    return (c2me_natives_end_noise_sample(data->permutations, x / 8, z / 8) - 8.0) / 128.0;
}

void c2me_natives_dfi_end_islands_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_end_islands_data *data = instance;
    for (size_t i = 0; i < length; i++) {
        res[i] = (c2me_natives_end_noise_sample(data->permutations, poses[i].x / 8, poses[i].z / 8) - 8.0) / 128.0;
    }
}

density_function_impl_data *c2me_natives_create_dfi_end_islands(int* permutations) {
    void* ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_end_islands_data));

    dfi_end_islands_data *data = ptr + sizeof(density_function_impl_data);
    data->permutations = permutations;

    density_function_impl_data *dfi = ptr;
    dfi->instance = data;
    dfi->single_op = c2me_natives_dfi_end_islands_single_op;
    dfi->multi_op = c2me_natives_dfi_end_islands_multi_op;
    return dfi;
}
