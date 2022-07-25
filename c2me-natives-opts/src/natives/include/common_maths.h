#ifndef C2ME_COMMON_MATHS_H
#define C2ME_COMMON_MATHS_H

#include <math.h>
#include <string.h>
#include <stdlib.h>
#include <stdint.h>

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

static int __attribute__((const)) math_floorDiv(int x, int y) {
    int r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x)) {
        r--;
    }
    return r;
}

static int64_t __attribute__((const)) math_lfloor(double value) {
    int64_t l = (int64_t) value;
    return value < (double) l ? l - 1L : l;
}

static double __attribute__((const)) math_octave_maintainPrecision(double value) {
    return value - (double) math_lfloor(value / 3.3554432E7 + 0.5) * 3.3554432E7;
}

static double __attribute__((const)) math_simplex_grad(int hash, double x, double y, double z, double distance) {
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

static double __attribute__((const)) math_lerp(double delta, double start, double end) {
    return start + delta * (end - start);
}

static float __attribute__((const)) math_lerpf(float delta, float start, float end) {
    return start + delta * (end - start);
}

static double __attribute__((const)) math_clampedLerp(double start, double end, double delta) {
    if (delta < 0.0) {
        return start;
    } else {
        return delta > 1.0 ? end : math_lerp(delta, start, end);
    }
}

static float __attribute__((const)) math_fclamp(float value, float min, float max) {
//    if (value < min) {
//        return min;
//    } else {
//        return value > max ? max : value;
//    }
    return fminf(fmaxf(value, min), max);
}

static double __attribute__((const)) math_clamp(double value, double min, double max) {
//    if (value < min) {
//        return min;
//    } else {
//        return value > max ? max : value;
//    }
    return fmin(fmax(value, min), max);
}

static double __attribute__((const)) math_getLerpProgress(double value, double start, double end) {
    return (value - start) / (end - start);
}

static double __attribute__((const))
math_clampedLerpFromProgress(double lerpValue, double lerpStart, double lerpEnd, double start, double end) {
    return math_clampedLerp(start, end, math_getLerpProgress(lerpValue, lerpStart, lerpEnd));
}

static int __attribute__((const)) math_floorMod(int x, int y) {
    int mod = x % y;
    // if the signs are different and modulo not zero, adjust result
    if ((mod ^ y) < 0 && mod != 0) {
        mod += y;
    }
    return mod;
}

#endif //C2ME_COMMON_MATHS_H
