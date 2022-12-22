#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    density_function_impl_data *delegate;
    int32_t lastX, lastZ;
    double lastSamplingResult;
} dfi_caching_cache_2d_data;

long c2me_natives_sizeof_dfi_caching_cache_2d_data() {
    return sizeof(dfi_caching_cache_2d_data);
}

static double c2me_natives_dfi_caching_cache_2d_single_op(void *instance, int x, int y, int z) {
    dfi_caching_cache_2d_data *data = instance;

    if (x == data->lastX && z == data->lastZ) {
        return data->lastSamplingResult;
    } else {
        data->lastX = x;
        data->lastZ = z;
        return data->lastSamplingResult = c2me_natives_dfi_bindings_single_op(data->delegate, x, y, z);
    }
}

static void c2me_natives_dfi_caching_cache_2d_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_caching_cache_2d_data *data = instance;

    for (size_t i = 0; i < length; ++i) {
        int32_t x = poses[i].x, z = poses[i].z;
        if (x == data->lastX && z == data->lastZ) {
            res[i] = data->lastSamplingResult;
        } else {
            data->lastX = x;
            data->lastZ = z;
            res[i] = data->lastSamplingResult = c2me_natives_dfi_bindings_single_op(data->delegate, x, poses[i].y, z);
        }
    }
}

density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_caching_cache_2d_data(density_function_impl_data *delegate) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_caching_cache_2d_data));

    dfi_caching_cache_2d_data *data = ptr + sizeof(density_function_impl_data);
    data->delegate = delegate;
    data->lastX = data->lastZ = INT32_MIN;
    data->lastSamplingResult = 0.0;

    density_function_impl_data *res = ptr;
    res->single_op = c2me_natives_dfi_caching_cache_2d_single_op;
    res->multi_op = c2me_natives_dfi_caching_cache_2d_multi_op;
    res->instance = data;
    return res;
}
