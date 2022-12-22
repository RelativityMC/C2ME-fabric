#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    density_function_impl_data *delegate;
    uint32_t length;
    int32_t biomeX, biomeZ;
    double *cacheFlattened;
} dfi_caching_flat_cache_data;

long c2me_natives_sizeof_dfi_caching_flat_cache_data() {
    return sizeof(dfi_caching_flat_cache_data);
}

static double c2me_natives_dfi_caching_flat_cache_single_op(void *instance, int x, int y, int z) {
    dfi_caching_flat_cache_data *data = instance;

    int32_t offsetX = math_block2biome(x) - data->biomeX;
    int32_t offsetZ = math_block2biome(z) - data->biomeZ;

    if (offsetX >= 0 && offsetX < data->length && offsetZ >= 0 && offsetZ < data->length) {
        return data->cacheFlattened[offsetX * data->length + offsetZ];
    } else {
        return c2me_natives_dfi_bindings_single_op(data->delegate, x, y, z);
    }
}

static void c2me_natives_dfi_caching_flat_cache_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_caching_flat_cache_data *data = instance;

    for (size_t i = 0; i < length; ++i) {
        int32_t x = poses[i].x, z = poses[i].z;
        int32_t offsetX = math_block2biome(x) - data->biomeX;
        int32_t offsetZ = math_block2biome(z) - data->biomeZ;

        if (offsetX >= 0 && offsetX < data->length && offsetZ >= 0 && offsetZ < data->length) {
            res[i] = data->cacheFlattened[offsetX * data->length + offsetZ];
        } else {
            res[i] = c2me_natives_dfi_bindings_single_op(data->delegate, x, poses[i].y, z);
        }
    }
}

density_function_impl_data __attribute__((malloc)) *
c2me_natives_create_dfi_caching_flat_cache_data(density_function_impl_data *delegate, uint32_t length,
                                                int32_t biomeX, int32_t biomeZ, double *cacheFlattened) {
    void *ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_caching_flat_cache_data) +
                       (cacheFlattened == NULL ? length * length * sizeof(double) : 0L));

    dfi_caching_flat_cache_data *data = ptr + sizeof(density_function_impl_data);
    data->delegate = delegate;
    data->length = length;
    data->biomeX = biomeX;
    data->biomeZ = biomeZ;
    if (cacheFlattened == NULL) {
        // generate cache data
        double *cacheFlattenedGen = ptr + sizeof(density_function_impl_data) + sizeof(dfi_caching_flat_cache_data);
        noise_pos poses[length * length];
        size_t counter = 0;
        for (int32_t offsetX = 0; offsetX < length; offsetX++) {
            for (int32_t offsetZ = 0; offsetZ < length; offsetZ++) {
                size_t i = counter++;
                poses[i].x = math_biome2block(biomeX + offsetX);
                poses[i].y = 0;
                poses[i].z = math_biome2block(biomeZ + offsetZ);
//                cacheFlattenedGen[i] = c2me_natives_dfi_bindings_single_op(delegate, math_biome2block(baseX + offsetX), 0, math_biome2block(baseZ + offsetZ));
            }
        }
        c2me_natives_dfi_bindings_multi_op_provided(delegate, poses, cacheFlattenedGen, length * length);
        data->cacheFlattened = cacheFlattenedGen;
    } else {
        data->cacheFlattened = cacheFlattened;
    }

    density_function_impl_data *dfi = ptr;
    dfi->single_op = c2me_natives_dfi_caching_flat_cache_single_op;
    dfi->multi_op = c2me_natives_dfi_caching_flat_cache_multi_op;
    dfi->instance = data;

    return dfi;
}


