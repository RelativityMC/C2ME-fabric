#include "../../include/density_functions.h"
#include "../../include/common_maths.h"

typedef struct {
    double fromY, toY;
    double fromValue, toValue;
} dfi_y_clamped_gradient_data;

long c2me_natives_sizeof_dfi_y_clamped_gradient_data() {
    return sizeof(dfi_y_clamped_gradient_data);
}

static double c2me_natives_dfi_y_clamped_gradient_single_op(void *instance, int x, int y, int z) {
    dfi_y_clamped_gradient_data *data = (dfi_y_clamped_gradient_data *)instance;
    return math_clampedLerpFromProgress(y, data->fromY, data->toY, data->fromValue, data->toValue);
}

static void c2me_natives_dfi_y_clamped_gradient_multi_op(void *instance, double *res, noise_pos *poses, size_t length) {
    dfi_y_clamped_gradient_data *data = (dfi_y_clamped_gradient_data *)instance;
    for (size_t i = 0; i < length; i++) {
        res[i] = math_clampedLerpFromProgress(poses[i].y, data->fromY, data->toY, data->fromValue, data->toValue);
    }
}

density_function_impl_data *c2me_natives_create_dfi_y_clamped_gradient_data(double fromY, double toY, double fromValue, double toValue) {
    void* ptr = malloc(sizeof(density_function_impl_data) + sizeof(dfi_y_clamped_gradient_data));

    dfi_y_clamped_gradient_data *data = ptr + sizeof(density_function_impl_data);
    data->fromY = fromY;
    data->toY = toY;
    data->fromValue = fromValue;
    data->toValue = toValue;

    density_function_impl_data *dfi = ptr;
    dfi->instance = data;
    dfi->single_op = c2me_natives_dfi_y_clamped_gradient_single_op;
    dfi->multi_op = c2me_natives_dfi_y_clamped_gradient_multi_op;

    return dfi;
}
