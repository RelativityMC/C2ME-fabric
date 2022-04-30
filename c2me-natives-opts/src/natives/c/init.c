#include "../include/common_maths.h"

bool c2me_natives_isInitialized = false;

double *c2me_natives_pow_of_two_table = NULL;
double SQRT_3;
double SKEW_FACTOR_2D;
double UNSKEW_FACTOR_2D;

void c2me_natives_init() {
    if (c2me_natives_isInitialized) return;

    c2me_natives_pow_of_two_table = malloc(sizeof(double) * 256);
    for (int i = 0; i < 256; i++) {
        c2me_natives_pow_of_two_table[i] = pow(2.0, i);
    }

    SQRT_3 = sqrt(3.0);
    SKEW_FACTOR_2D = 0.5 * (SQRT_3 - 1.0);
    UNSKEW_FACTOR_2D = (3.0 - SQRT_3) / 6.0;

    c2me_natives_isInitialized = true;
}
