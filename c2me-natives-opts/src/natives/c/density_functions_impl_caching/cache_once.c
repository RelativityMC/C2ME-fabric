#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    density_function_impl_data *delegate;
    // TODO figure out how to do indexed cache
    int32_t lastX, lastY, lastZ;
    double lastSamplingResult;
} dfi_caching_cache_once_data;

long c2me_natives_sizeof_dfi_caching_cache_once_data() {
    return sizeof(dfi_caching_cache_once_data);
}

static double c2me_natives_dfi_caching_cache_once_single_op(void *instance, int x, int y, int z) {
    dfi_caching_cache_once_data *data = instance;

    if (x == data->lastX && y == data->lastY && z == data->lastZ) {
        return data->lastSamplingResult;
    } else {
        data->lastX = x;
        data->lastY = y;
        data->lastZ = z;
        return data->lastSamplingResult = c2me_natives_dfi_bindings_single_op(data->delegate, x, y, z);
    }
}

static void c2me_natives_dfi_caching_cache_once_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_caching_cache_once_data *data = instance;

//    for (size_t i = 0; i < length; ++i) {
//        int32_t x = poses[i].x, y = poses[i].y, z = poses[i].z;
//        if (x == data->lastX && y == data->lastY && z == data->lastZ) {
//            res[i] = data->lastSamplingResult;
//        } else {
//            data->lastX = x;
//            data->lastY = y;
//            data->lastZ = z;
//            res[i] = data->lastSamplingResult = c2me_natives_dfi_bindings_single_op(data->delegate, x, y, z);
//        }
//    }

    c2me_natives_dfi_bindings_multi_op_provided(data->delegate, poses, res, length);

}

density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_caching_cache_once_data(density_function_impl_data *delegate) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_caching_cache_once_data));

    dfi_caching_cache_once_data *data = ptr + sizeof(density_function_impl_data);
    data->delegate = delegate;
    data->lastX = data->lastY = data->lastZ = INT32_MIN;
    data->lastSamplingResult = 0.0;

    density_function_impl_data *res = ptr;
    res->single_op = c2me_natives_dfi_caching_cache_once_single_op;
    res->multi_op = c2me_natives_dfi_caching_cache_once_multi_op;
    res->instance = data;
    return res;
}

