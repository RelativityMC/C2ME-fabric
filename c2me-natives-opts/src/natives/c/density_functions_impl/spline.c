#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct spline_data {
    bool isConstant;
    float constantValue;
    density_function_impl_data *locationFunction;
    float *locations;
    uint32_t locations_length;
    struct spline_data **values;
    float *derivatives;
} spline_data;

long c2me_natives_sizeof_spline_data_constant() {
    return sizeof(bool) + sizeof(float);
}

long c2me_natives_sizeof_spline_data_impl() {
    return sizeof(spline_data);
}

spline_data __attribute__((malloc)) *
c2me_natives_create_spline_data_impl(density_function_impl_data *locationFunction, float *locations,
                                     uint32_t locations_length, spline_data **values, float *derivatives) {
    spline_data *data = malloc(sizeof(spline_data));
    data->isConstant = false;
    data->locationFunction = locationFunction;
    data->locations = locations;
    data->locations_length = locations_length;
    data->values = values;
    data->derivatives = derivatives;
    return data;
}

spline_data __attribute__((malloc)) *c2me_natives_create_spline_data_constant(float constantValue) {
    spline_data *data = malloc(sizeof(bool) + sizeof(float));
    data->isConstant = true;
    data->constantValue = constantValue;
    return data;
}

static float spline_impl_method_41297(float f, const float *fs, float g, const float *gs, int i) {
    float h = gs[i];
    return h == 0.0F ? g : g + h * (f - fs[i]);
}

static int spline_impl_method_41300(const float *fs, uint32_t fs_length, float f) {
    int min = 0;
    int i1 = fs_length - min;

    while (i1 > 0) {
        int j = i1 / 2;
        int k = min + j;
        if (f < fs[k]) {
            i1 = j;
        } else {
            min = k + 1;
            i1 -= j + 1;
        }
    }

    return min - 1;
}

static float spline_impl_apply0(spline_data *data, int x, int y, int z);

static float spline_impl_apply(spline_data *data, int x, int y, int z) {
    if (data->isConstant) return data->constantValue;
    else return spline_impl_apply0(data, x, y, z);
}

static inline float __attribute__((always_inline)) spline_impl_apply0(spline_data *data, int x, int y, int z) {
    float f = c2me_natives_dfi_bindings_single_op(data->locationFunction, x, y, z);
    int i = spline_impl_method_41300(data->locations, data->locations_length, f);
    int j = data->locations_length - 1;
    if (i < 0) {
        return spline_impl_method_41297(f, data->locations, spline_impl_apply(data->values[0], x, y, z),
                                        data->derivatives, 0);
    } else if (i == j) {
        return spline_impl_method_41297(f, data->locations, spline_impl_apply(data->values[j], x, y, z),
                                        data->derivatives, j);
    } else {
        float g = data->locations[i];
        float h = data->locations[i + 1];
        float k = (f - g) / (h - g);
        spline_data *toFloatFunction = data->values[i];
        spline_data *toFloatFunction2 = data->values[i + 1];
        float l = data->derivatives[i];
        float m = data->derivatives[i + 1];
        float n = spline_impl_apply(toFloatFunction, x, y, z);
        float o = spline_impl_apply(toFloatFunction2, x, y, z);
        float p = l * (h - g) - (o - n);
        float q = -m * (h - g) + (o - n);
        return math_lerpf(k, n, o) + k * (1.0F - k) * math_lerpf(k, p, q);
    }
}

static double c2me_natives_dfi_spline_single_op(void *instance, int x, int y, int z) {
    spline_data *data = instance;
    return spline_impl_apply(data, x, y, z);
}

static void c2me_natives_dfi_spline_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    spline_data *data = instance;
    for (size_t i = 0; i < length; i++) {
        res[i] = spline_impl_apply(data, poses[i].x, poses[i].y, poses[i].z);
    }
}

density_function_impl_data __attribute__((malloc)) *c2me_natives_create_dfi_spline(spline_data *spline) {
    density_function_impl_data *data = malloc(sizeof(density_function_impl_data));
    data->instance = spline;
    data->single_op = c2me_natives_dfi_spline_single_op;
    data->multi_op = c2me_natives_dfi_spline_multi_op;
    return data;
}

//density_function_impl_data *c2me_natives_create_dfi_spline_constant(float constantValue) {
//    void *ptr = malloc(sizeof(density_function_impl_data) + c2me_natives_sizeof_spline_data_constant());
//
//    spline_data *spline = ptr + sizeof(density_function_impl_data);
//    spline->isConstant = true;
//    spline->constantValue = constantValue;
//
//    density_function_impl_data *data = ptr;
//    data->instance = spline;
//    data->single_op = c2me_natives_dfi_spline_single_op;
//    data->multi_op = c2me_natives_dfi_spline_multi_op;
//    return data;
//}
//
//density_function_impl_data *
//c2me_natives_create_dfi_spline_impl(density_function_impl_data *locationFunction, float *locations,
//                                    uint32_t locations_length, spline_data *values, float *derivatives) {
//    void *ptr = malloc(sizeof(density_function_impl_data) + c2me_natives_sizeof_spline_data_constant());
//
//    spline_data *spline = ptr + sizeof(density_function_impl_data);
//    spline->isConstant = false;
//    spline->locationFunction = locationFunction;
//    spline->locations = locations;
//    spline->locations_length = locations_length;
//    spline->values = values;
//    spline->derivatives = derivatives;
//
//    density_function_impl_data *data = ptr;
//    data->instance = spline;
//    data->single_op = c2me_natives_dfi_spline_single_op;
//    data->multi_op = c2me_natives_dfi_spline_multi_op;
//    return data;
//}


