#include "../include/common_maths.h"
#include "../include/density_functions.h"

long c2me_natives_sizeof_density_function_data() {
    return sizeof(density_function_impl_data);
}

long c2me_natives_sizeof_density_function_multi_pos_args_data() {
    return sizeof(density_function_multi_pos_args_data);
}

double c2me_natives_dfi_bindings_single_op(density_function_impl_data *dfi, int blockX, int blockY, int blockZ) {
    return dfi->single_op(dfi->instance, blockX, blockY, blockZ);
}

void c2me_natives_dfi_bindings_multi_op(density_function_impl_data *dfi, density_function_multi_pos_args_data *dfa,
                                        double *res, size_t length) {
    noise_pos pos[length];
    dfa->get_all_pos(dfa->instance, pos, length);
    dfi->multi_op(dfi->instance, res, pos, length);
}

void c2me_natives_dfi_bindings_multi_op_provided(density_function_impl_data *dfi, noise_pos *poses, double *res,
                                                 size_t length) {
    dfi->multi_op(dfi->instance, res, poses, length);
}
