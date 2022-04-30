#ifndef C2ME_COMMON_MATHS_H
#define C2ME_COMMON_MATHS_H

#include <math.h>
#include <string.h>
#include <stdlib.h>

#define BILLION 1000000000L

typedef unsigned char bool;
static const bool false = 0;
static const bool true = 1;

static const double FLAT_SIMPLEX_GRAD[] = {
        1, 1, 0, 0,
        -1, 1, 0, 0,
        1, -1, 0, 0,
        -1, -1, 0, 0,
        1, 0, 1, 0,
        -1, 0, 1, 0,
        1, 0, -1, 0,
        -1, 0, -1, 0,
        0, 1, 1, 0,
        0, -1, 1, 0,
        0, 1, -1, 0,
        0, -1, -1, 0,
        1, 1, 0, 0,
        0, -1, 1, 0,
        -1, 1, 0, 0,
        0, -1, -1, 0,
};

extern bool c2me_natives_isInitialized;

extern double *c2me_natives_pow_of_two_table;
extern double SQRT_3;
extern double SKEW_FACTOR_2D;
extern double UNSKEW_FACTOR_2D;

extern void c2me_natives_init();

static inline int __attribute__((always_inline)) c2me_natives_floorDiv(int x, int y) {
    int r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x)) {
        r--;
    }
    return r;
}

static inline double __attribute__((always_inline)) c2me_natives_octave_maintainPrecision(double value) {
    __int64_t l = value;
    return value - (double) (l < value ? l - 1L : l) * 3.3554432E7;
}

static inline double __attribute__((always_inline)) c2me_natives_simplex_grad(int hash, double x, double y, double z, double distance) {
    double d = distance - x * x - y * y - z * z;
    if (d < 0.0) {
        return 0.0;
    } else {
        int i = hash << 2;
        double var0 = FLAT_SIMPLEX_GRAD[i | 0] * x;
        double var1 = FLAT_SIMPLEX_GRAD[i | 1] * y;
        double var2 = FLAT_SIMPLEX_GRAD[i | 2] * z;
        return d * d * d * d * (var0 + var1 + var2);
    }
}

static inline double __attribute__((always_inline)) c2me_natives_lerp(double delta, double start, double end) {
    return start + delta * (end - start);
}

static inline double __attribute__((always_inline)) c2me_natives_clampedLerp(double start, double end, double delta) {
    if (delta < 0.0) {
        return start;
    } else {
        return delta > 1.0 ? end : c2me_natives_lerp(delta, start, end);
    }
}

static inline float __attribute__((always_inline)) c2me_natives_fclamp(float value, float min, float max) {
//    if (value < min) {
//        return min;
//    } else {
//        return value > max ? max : value;
//    }
    return fminf(fmaxf(value, min), max);
}

#endif //C2ME_COMMON_MATHS_H
