#include <stdlib.h>
#include <stdint.h>

#ifndef C2ME_FABRIC_DENSITY_FUNCTIONS_H
#define C2ME_FABRIC_DENSITY_FUNCTIONS_H

typedef struct {
    int32_t x, y, z;
} noise_pos;

typedef noise_pos (*density_function_arg_multi_pos_get_pos)(void *instance, int i);
typedef size_t (*density_function_arg_multi_pos_get_all_pos)(void *instance, noise_pos *array, size_t length);
typedef double (*density_function_single_op)(void *instance, int x, int y, int z);
typedef void (*density_function_multi_op)(void *instance, double *res, noise_pos *poses, size_t length);

typedef struct {
    void *instance;
    density_function_single_op single_op;
    density_function_multi_op multi_op;
} density_function_impl_data;

typedef struct {
    void *instance;
    density_function_arg_multi_pos_get_pos get_pos;
    density_function_arg_multi_pos_get_all_pos get_all_pos;
} density_function_multi_pos_args_data;

extern double c2me_natives_dfi_bindings_single_op(const density_function_impl_data *dfi, int blockX, int blockY, int blockZ);
extern void c2me_natives_dfi_bindings_multi_op(const density_function_impl_data *dfi, density_function_multi_pos_args_data *dfa,
                                               double *res, size_t length);
extern void c2me_natives_dfi_bindings_multi_op_provided(const density_function_impl_data *dfi, noise_pos *poses, double *res,
                                                        size_t length);

#endif //C2ME_FABRIC_DENSITY_FUNCTIONS_H
