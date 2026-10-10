#pragma once

#include <float.h>
/* Target-specific compiler hints; all targets compile the same scalar kernels.
 */
#if defined(__AVX512F__)
#define NOISEOPT_FLOAT_LANES 16
#define NOISEOPT_DOUBLE_LANES 8
#define NOISEOPT_BIOME_INTERLEAVE 1
#else
#define NOISEOPT_FLOAT_LANES 8
#define NOISEOPT_DOUBLE_LANES 4
#define NOISEOPT_BIOME_INTERLEAVE 2
#endif
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>

/* sizeY is strip-mined in tiles of __MATH_PERLIN_MAX_Y samples, so the Y
   staging stays L1-resident regardless of region height; overridable for cache
   tuning. __MATH_PERLIN_MAX_Z likewise caps the Z staging tile; real regions
   are chunk sections with sizeZ <= 16. */
#ifndef __MATH_PERLIN_MAX_Y
#define __MATH_PERLIN_MAX_Y 64
#endif
#ifndef __MATH_PERLIN_MAX_Z
#define __MATH_PERLIN_MAX_Z 32
#endif

// GCC does not provide __builtin_memcpy_inline (Clang-only); fall back to
// __builtin_memcpy.
#if !defined(__clang__) && defined(__GNUC__)
#define __builtin_memcpy_inline __builtin_memcpy
#endif

// Define UNUSED_ATTR macro based on language standard and compiler support
#if defined(__cplusplus) && __cplusplus >= 201703L
// C++17 or newer
#define UNUSED_ATTR [[maybe_unused]]
#elif defined(__STDC_VERSION__) && __STDC_VERSION__ >= 202311L
// C23 or newer
#define UNUSED_ATTR [[maybe_unused]]
#elif defined(__clang__) || defined(__GNUC__)
// Clang/GCC specific attribute
#define UNUSED_ATTR __attribute__((unused))
#else
// No attribute support - define to nothing
#define UNUSED_ATTR
#endif

__attribute__((aligned(64))) static const double FLAT_SIMPLEX_GRAD_F64[] = {
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

__attribute__((aligned(32))) static const float FLAT_SIMPLEX_GRAD_F32[] = {
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

__attribute__((aligned(32))) static const int8_t FLAT_SIMPLEX_GRAD_I8[] = {
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

#pragma float_control(push)
#pragma clang fp contract(off) reassociate(off)

static const double SQRT_3 = 1.7320508075688772;
// 1 / SQRT_3
static const double INV_SQRT_3 = 0.5773502691896258;
// 0.5 * (SQRT_3 - 1.0)
static const double SKEW_FACTOR_2D = 0.3660254037844386;
// (3.0 - SQRT_3) / 6.0
static const double UNSKEW_FACTOR_2D = 0.21132486540518713;

typedef const double *aligned_double_ptr __attribute__((align_value(64)));
typedef const double *restrict aligned_restrict_double_ptr __attribute__((align_value(64)));
typedef const uint8_t *aligned_uint8_ptr __attribute__((align_value(64)));
typedef const uint32_t *aligned_uint32_ptr __attribute__((align_value(64)));
typedef const uint32_t *restrict aligned_restrict_uint32_ptr __attribute__((align_value(64)));

#define max(a, b) \
   ({ __typeof__ (a) _a = (a); \
       __typeof__ (b) _b = (b); \
     _a >= _b ? _a : _b; })
#define min(a, b) \
   ({ __typeof__ (a) _a = (a); \
       __typeof__ (b) _b = (b); \
     _a <= _b ? _a : _b; })

// #pragma clang attribute push (__attribute__((always_inline)), apply_to = function)

static inline __attribute__((const)) void *ptr_shift(const void * const ptr, const int32_t shift) {
    return (void *) (((uint8_t *) ptr) + shift);
}

static inline __attribute__((const)) float fminf(const float x, const float y) {
    return __builtin_fminf(x, y);
}

static inline __attribute__((const)) float fmaxf(const float x, const float y) {
    return __builtin_fmaxf(x, y);
}

static inline __attribute__((const)) float fabsf(const float x) {
    union {
        float f;
        uint32_t i;
    } u = {x};
    u.i &= 0x7fffffff;
    return u.f;
}

static inline __attribute__((const)) int64_t labs(const int64_t x) {
#ifdef _WIN32
    return __builtin_llabs(x);
#else
    return __builtin_labs(x);
#endif
}

static inline __attribute__((const)) double floor(double x) {
    return __builtin_floor(x);
}

static inline __attribute__((const)) float floorf(float x) {
    return __builtin_floorf(x);
}

static inline __attribute__((const)) float roundf(float x) {
    return __builtin_roundf(x);
}

static inline __attribute__((const)) float sqrtf(float x) {
    return __builtin_sqrtf(x);
}

static inline __attribute__((const)) float fmodf(float x, float y) {
    return __builtin_fmodf(x, y);
}

static inline __attribute__((const)) int32_t math_floorDiv(const int32_t x, const int32_t y) {
    int r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x)) {
        r--;
    }
    return r;
}

static inline __attribute__((const)) float clampf(const float value, const float min, const float max) {
    return fminf(fmaxf(value, min), max);
}

static inline __attribute__((const)) double math_octave_maintainPrecision(const double value) {
    return value - floor(value / 3.3554432E7 + 0.5) * 3.3554432E7;
}

static inline __attribute__((const)) double math_simplex_grad(const int32_t hash, const double x, const double y,
                                                              const double z, const double distance) {
    double d = distance - x * x - y * y - z * z;
    if (d < 0.0) {
        return 0.0;
    } else {
        int32_t i = hash << 2;
        double var0 = FLAT_SIMPLEX_GRAD_F64[i | 0] * x;
        double var1 = FLAT_SIMPLEX_GRAD_F64[i | 1] * y;
        double var2 = FLAT_SIMPLEX_GRAD_F64[i | 2] * z;
        return d * d * d * d * (var0 + var1 + var2);
    }
}

static inline __attribute__((const)) double math_lerp(const double delta, const double start, const double end) {
    return start + delta * (end - start);
}

static inline __attribute__((const)) float math_lerpf(const float delta, const float start, const float end) {
    return start + delta * (end - start);
}

static inline __attribute__((const)) double math_clampedLerp(const double start, const double end, const double delta) {
    if (delta < 0.0) {
        return start;
    } else {
        return delta > 1.0 ? end : math_lerp(delta, start, end);
    }
}

static inline __attribute__((const)) double math_square(const double operand) {
    return operand * operand;
}

static inline __attribute__((const)) double math_lerp2(const double deltaX, const double deltaY, const double x0y0,
                                                       const double x1y0, const double x0y1, const double x1y1) {
    return math_lerp(deltaY, math_lerp(deltaX, x0y0, x1y0), math_lerp(deltaX, x0y1, x1y1));
}

static inline __attribute__((const)) float math_lerp2f(const float deltaX, const float deltaY, const float x0y0,
                                                        const float x1y0, const float x0y1, const float x1y1) {
    return math_lerpf(deltaY, math_lerpf(deltaX, x0y0, x1y0), math_lerpf(deltaX, x0y1, x1y1));
}

static inline __attribute__((const)) double math_lerp3(
        const double deltaX,
        const double deltaY,
        const double deltaZ,
        const double x0y0z0,
        const double x1y0z0,
        const double x0y1z0,
        const double x1y1z0,
        const double x0y0z1,
        const double x1y0z1,
        const double x0y1z1,
        const double x1y1z1
) {
    return math_lerp(deltaZ, math_lerp2(deltaX, deltaY, x0y0z0, x1y0z0, x0y1z0, x1y1z0),
                     math_lerp2(deltaX, deltaY, x0y0z1, x1y0z1, x0y1z1, x1y1z1));
}

static inline __attribute__((const)) float math_lerp3f(
        const float deltaX,
        const float deltaY,
        const float deltaZ,
        const float x0y0z0,
        const float x1y0z0,
        const float x0y1z0,
        const float x1y1z0,
        const float x0y0z1,
        const float x1y0z1,
        const float x0y1z1,
        const float x1y1z1
) {
    return math_lerpf(deltaZ, math_lerp2f(deltaX, deltaY, x0y0z0, x1y0z0, x0y1z0, x1y1z0),
                     math_lerp2f(deltaX, deltaY, x0y0z1, x1y0z1, x0y1z1, x1y1z1));
}

static inline __attribute__((const)) double math_getLerpProgress(const double value, const double start,
                                                                 const double end) {
    return (value - start) / (end - start);
}

static inline __attribute__((const)) double
math_clampedLerpFromProgress(const double lerpValue, const double lerpStart, const double lerpEnd, const double start,
                             const double end) {
    return math_clampedLerp(start, end, math_getLerpProgress(lerpValue, lerpStart, lerpEnd));
}

static inline __attribute__((const)) int32_t math_floorMod(const int32_t x, const int32_t y) {
    int32_t mod = x % y;
    // if the signs are different and modulo not zero, adjust result
    if ((mod ^ y) < 0 && mod != 0) {
        mod += y;
    }
    return mod;
}

static inline __attribute__((const)) int32_t math_biome2block(const int32_t biomeCoord) {
    return biomeCoord << 2;
}

static inline __attribute__((const)) int32_t math_block2biome(const int32_t blockCoord) {
    return blockCoord >> 2;
}

static inline __attribute__((pure, always_inline)) uint32_t
__math_simplex_map(const uint32_t *restrict const permutations, const uint32_t input) {
    uint32_t point = input & 0xFF;
    return (permutations[point >> 2] >> ((input & 3) << 3)) & 0xFF;
}

static inline __attribute__((const, always_inline)) double math_simplex_dot(
    const uint32_t hash, const double x, const double y, const double z) {
    // note: ordering actually doesn't matter because there's only two non-zero values
    // also FMA is possible since multiplied values are *exact*
#pragma clang fp contract(on) reassociate(on)
    __builtin_assume(hash < 16u);
    const uint32_t loc = hash << 2;
    return FLAT_SIMPLEX_GRAD_F64[loc + 0] * x +
           FLAT_SIMPLEX_GRAD_F64[loc + 1] * y +
           FLAT_SIMPLEX_GRAD_F64[loc + 2] * z;
}

static inline __attribute__((const, always_inline)) double
__math_simplex_grad(const uint32_t hash, const double x, const double y,
                    const double z, const double distance) {
    double d = distance - x * x - y * y - z * z;
    double e;
    if (d < 0.0) {
        e = 0.0;
    } else {
        d *= d;
        e = d * d * math_simplex_dot(hash, x, y, z);
    }
    return e;
    // double tmp = d * d; // speculative execution

    // return d < 0.0 ? 0.0 : tmp * tmp * math_simplex_dot(hash, x, y, z);
}

static inline __attribute__((const, always_inline)) double
__noise_simplex_grad2d_fast(uint32_t hash, double x, double y, bool bulk) {
    __builtin_assume(hash < 12u);
    double d = 0.5 - x * x - y * y;
    if (d < 0.0)
        return 0.0;
    d *= d;
    double dot;
    if (bulk) {
        const double sx = (hash & 1) ? -x : x;
        const double sy = (hash & (hash < 4 ? 2 : 1)) ? -y : y;
        dot = (hash < 8 ? sx : 0.0) + ((hash < 4 || hash >= 8) ? sy : 0.0);
    } else {
        const double *const g = FLAT_SIMPLEX_GRAD_F64 + (hash << 2);
        dot = g[0] * x + g[1] * y;
    }
    return d * d * dot;
}

static inline double __attribute__((pure, always_inline))
noise_simplex_sample2d_impl(const uint32_t *restrict const permutations,
                            const double x, const double y,
                            bool bulk) {
    __builtin_assume_dereferenceable(permutations, 64 * sizeof(uint32_t));
    const double d = (x + y) * SKEW_FACTOR_2D;
    const double i = floor(x + d);
    const double j = floor(y + d);
    const double e = (i + j) * UNSKEW_FACTOR_2D;
    const double f = i - e;
    const double g = j - e;
    const double h = x - f;
    const double k = y - g;
    double l;
    int32_t li;
    double m;
    int32_t mi;
    if (h > k) {
        l = 1;
        li = 1;
        m = 0;
        mi = 0;
    } else {
        l = 0;
        li = 1;
        m = 1;
        mi = 1;
    }

    const double n = h - (double) l + UNSKEW_FACTOR_2D;
    const double o = k - (double) m + UNSKEW_FACTOR_2D;
    const double p = h - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
    const double q = k - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
    const uint32_t r = (int32_t) i & 0xFF;
    const uint32_t s = (int32_t) j & 0xFF;
    const uint32_t t = __math_simplex_map(permutations, r + __math_simplex_map(permutations, s)) % 12;
    const uint32_t u = __math_simplex_map(permutations, r + li + __math_simplex_map(permutations, s + mi)) % 12;
    const uint32_t v =__math_simplex_map(permutations, r + 1 + __math_simplex_map(permutations, s + 1)) % 12;
    const double w = __noise_simplex_grad2d_fast(t, h, k, bulk);
    const double z = __noise_simplex_grad2d_fast(u, n, o, bulk);
    const double aa = __noise_simplex_grad2d_fast(v, p, q, bulk);
    return 70.0 * (w + z + aa);
}

static inline __attribute__((pure, always_inline)) double
math_noise_simplex_sample2d(const uint32_t *restrict permutations, double x,
                            double y) {
    return noise_simplex_sample2d_impl(permutations, x, y, false);
}

static inline __attribute__((const)) float math_perlinFade(const float value) {
    return value * value * value * (value * (value * 6.0f - 15.0f) + 10.0f);
}

static inline __attribute__((pure, always_inline)) uint32_t
__math_perlin_perm_index(const uint32_t *restrict const permutations,
                         uint32_t index) {
    __builtin_assume_dereferenceable(permutations, 64 * sizeof(uint32_t));
    uint32_t point = index & 0xFF;
    return (permutations[point >> 2] >> ((index & 3) << 3)) & 0xFF;
}

static inline __attribute__((const)) float
noise_perlin_dot(uint32_t code, float x, float y, float z) {
    __builtin_assume(code < 32u);
    /* Axis predicates and signs were prepared once for the permutation table. */
    union {
        float f;
        uint32_t i;
    } u = {.f = (code & 1u) ? x : y};
    union {
        float f;
        uint32_t i;
    } v = {.f = (code & 2u) ? y : ((code & 4u) ? x : z)};
    u.i ^= (code & 8u) << 28;
    v.i ^= (code & 16u) << 27;
    return u.f + v.f;
}

static inline __attribute__((const, always_inline)) float
noise_perlin_gradient_hash(uint32_t hash, float fx, float fy, float fz) {
    // note: ordering actually doesn't matter because there's only two non-zero values
    // also FMA is possible since multiplied values are *exact*
#pragma clang fp contract(on) reassociate(on)
    __builtin_assume(hash < 16u);
    const float *const grad = FLAT_SIMPLEX_GRAD_F32 + (hash << 2);
    return grad[0] * fx + grad[1] * fy + grad[2] * fz;
}

static inline __attribute__((pure, always_inline)) float
__math_perlin_grad(const uint32_t *restrict const permutations,
                   const int32_t px, const int32_t py, const int32_t pz,
                   const float fx, const float fy, const float fz) {
    const uint32_t hash =
            __math_perlin_perm_index(
                permutations,
                (__math_perlin_perm_index(
                     permutations,
                     (__math_perlin_perm_index(permutations, px & 0xFF) +
                      (py & 0xFF)) &
                     0xFF) +
                 (pz & 0xFF)) &
                0xFF) &
            0xF;
    // Scalar samples benefit from the small coefficient table rather than axis branches.
    return noise_perlin_gradient_hash(hash, fx, fy, fz);
}

static inline __attribute__((pure, flatten, always_inline)) float
math_noise_perlin_sample0(const uint32_t *restrict const permutations,
                          const int32_t px0, const int32_t py0, const int32_t pz0,
                          const float fx0, const float fy0, const float fz0, const float fadeLocalY) {
    const int32_t px1 = px0 + 1;
    const int32_t py1 = py0 + 1;
    const int32_t pz1 = pz0 + 1;
    const float fx1 = fx0 - 1.0f;
    const float fy1 = fy0 - 1.0f;
    const float fz1 = fz0 - 1.0f;

    const float f000 = __math_perlin_grad(permutations, px0, py0, pz0, fx0, fy0, fz0);
    const float f100 = __math_perlin_grad(permutations, px1, py0, pz0, fx1, fy0, fz0);
    const float f010 = __math_perlin_grad(permutations, px0, py1, pz0, fx0, fy1, fz0);
    const float f110 = __math_perlin_grad(permutations, px1, py1, pz0, fx1, fy1, fz0);
    const float f001 = __math_perlin_grad(permutations, px0, py0, pz1, fx0, fy0, fz1);
    const float f101 = __math_perlin_grad(permutations, px1, py0, pz1, fx1, fy0, fz1);
    const float f011 = __math_perlin_grad(permutations, px0, py1, pz1, fx0, fy1, fz1);
    const float f111 = __math_perlin_grad(permutations, px1, py1, pz1, fx1, fy1, fz1);

    const float dx = math_perlinFade(fx0);
    const float dy = math_perlinFade(fadeLocalY);
    const float dz = math_perlinFade(fz0);
    return math_lerp3f(dx, dy, dz, f000, f100, f010, f110, f001, f101, f011, f111);
}

static inline __attribute__((pure, always_inline)) float
math_noise_perlin_sample_legacy(const uint32_t *restrict const permutations,
                                const double originX, const double originY, const double originZ,
                                const double x, const double y, const double z,
                                const double yScale) {
    const double x1 = math_octave_maintainPrecision(x) + originX;
    const double y1 = math_octave_maintainPrecision(y) + originY;
    const double z1 = math_octave_maintainPrecision(z) + originZ;
    const double floorX = floor(x1);
    const double floorY = floor(y1);
    const double floorZ = floor(z1);
    const double relX = x1 - floorX;
    const double relY = y1 - floorY;
    const double relZ = z1 - floorZ;
    const double fy = floor(((y >= 0.0 && y < relY) ? y : relY) / yScale + 1.0E-7) * yScale;

    return math_noise_perlin_sample0(
        permutations, (int32_t) floorX, (int32_t) floorY, (int32_t) floorZ,
        (float) relX, (float) (relY - fy), (float) relZ, (float) relY);
}

static inline __attribute__((pure, always_inline)) float
math_noise_perlin_sample_base(const uint32_t *restrict const permutations,
                              const double originX, const double originY, const double originZ,
                              const double x, const double y, const double z) {
    const double x1 = math_octave_maintainPrecision(x) + originX;
    const double y1 = math_octave_maintainPrecision(y) + originY;
    const double z1 = math_octave_maintainPrecision(z) + originZ;
    const double floorX = floor(x1);
    const double floorY = floor(y1);
    const double floorZ = floor(z1);
    const float relX = (float) (x1 - floorX);
    const float relY = (float) (y1 - floorY);
    const float relZ = (float) (z1 - floorZ);

    return math_noise_perlin_sample0(permutations, (int32_t) floorX,
                                     (int32_t) floorY, (int32_t) floorZ, relX, relY,
                                     relZ, relY);
}

typedef struct sampling_region {
    const uint32_t sizeX;
    const uint32_t sizeY;
    const uint32_t sizeZ;
    const int32_t minBlockX;
    const int32_t minBlockY;
    const int32_t minBlockZ;
    const uint32_t stepBlockX;
    const uint32_t stepBlockY;
    const uint32_t stepBlockZ;
} sampling_region_t;

typedef struct pos_i32 {
    const int32_t x;
    const int32_t y;
    const int32_t z;
} pos_i32_t;

static inline __attribute__((const)) pos_i32_t math_sampling_region_index(const sampling_region_t region, const uint32_t index) {
    // order: z, x, y

    if (index >= (region.sizeX * region.sizeY * region.sizeZ)) {
        __builtin_trap();
        __builtin_unreachable();
    }

    uint32_t i = index;

    const uint32_t y = i % region.sizeY;
    i /= region.sizeY;

    const uint32_t x = i % region.sizeX;
    const uint32_t z = i / region.sizeX;

    return (pos_i32_t){
        .x = region.minBlockX + (int32_t) x * (int32_t) region.stepBlockX,
        .y = region.minBlockY + (int32_t) y * (int32_t) region.stepBlockY,
        .z = region.minBlockZ + (int32_t) z * (int32_t) region.stepBlockZ
    };
}

typedef struct {
    uint32_t x, y, z;
    uint32_t sizeX, sizeY, sizeZ;
    int32_t minX, minY, minZ;
    uint32_t stepX, stepY, stepZ;
} coord_iter_t;

static inline coord_iter_t math_coord_iter_begin(const sampling_region_t region) {
    return (coord_iter_t){
        .x = 0, .y = 0, .z = 0,
        .sizeX = region.sizeX, .sizeY = region.sizeY, .sizeZ = region.sizeZ,
        .minX = region.minBlockX, .minY = region.minBlockY, .minZ = region.minBlockZ,
        .stepX = region.stepBlockX, .stepY = region.stepBlockY, .stepZ = region.stepBlockZ,
    };
}

static inline uint32_t
math_coord_iter_next64(coord_iter_t *restrict it,
                       int32_t *restrict xs, int32_t *restrict ys, int32_t *restrict zs,
                       uint32_t remaining) {
    const uint32_t n = remaining < 64u ? remaining : 64u;
    if (n == 0) return 0;

    const uint32_t sizeX = it->sizeX;
    const uint32_t sizeY = it->sizeY;

    if (it->y + n < sizeY) {
        const int32_t baseX = it->minX + (int32_t) it->x * (int32_t) it->stepX;
        const int32_t baseZ = it->minZ + (int32_t) it->z * (int32_t) it->stepZ;
        const int32_t baseY = it->minY + (int32_t) it->y * (int32_t) it->stepY;
        for (uint32_t j = 0; j < n; j++) {
            xs[j] = baseX;
            ys[j] = baseY + (int32_t) j * (int32_t) it->stepY;
            zs[j] = baseZ;
        }
        it->y += n;
        return n;
    }

    uint32_t cx = it->x, cy = it->y, cz = it->z;
    for (uint32_t j = 0; j < n;) {
        while (cy < sizeY && j < n) {
            xs[j] = it->minX + (int32_t) cx * (int32_t) it->stepX;
            ys[j] = it->minY + (int32_t) cy * (int32_t) it->stepY;
            zs[j] = it->minZ + (int32_t) cz * (int32_t) it->stepZ;
            cy++;
            j++;
        }
        if (cy == sizeY) {
            cy = 0;
            if (++cx == sizeX) {
                cx = 0;
                ++cz;
            }
        }
    }
    it->x = cx;
    it->y = cy;
    it->z = cz;
    return n;
}

// p2c[i] is the 5-bit dot code of perm value i
static inline void noise_perlin_build_p2c(const uint32_t *restrict packed,
                                          uint8_t *restrict p2c) {
    __builtin_assume_dereferenceable(packed, 64 * sizeof(uint32_t));
#pragma clang loop vectorize(enable)
    for (uint32_t n = 0; n < 256; ++n) {
        const uint32_t h = __math_perlin_perm_index(packed, n) & 15u;
        p2c[n] =
                (uint8_t) ((uint32_t) (h < 8u) | ((uint32_t) (h < 4u) << 1) |
                           ((uint32_t) (h == 12u || h == 14u) << 2) | ((h & 3u) << 3));
    }
    // adds 8 additional value for unaligned loads instead of multiple loads
    for (uint32_t j = 0; j < 8; ++j)
        p2c[256 + j] = p2c[j];
}

static inline __attribute__((pure, flatten)) float
noise_perlin_corners(const uint8_t *restrict p2c, uint32_t q00, uint32_t q10,
                     uint32_t q01, uint32_t q11, uint32_t pz, float fx0,
                     float fx1, float dx, float fy0, float fy1, float dy,
                     float fz0, float fz1, float dz, bool legacy) {
    __builtin_assume_dereferenceable(p2c, 264);
    uint32_t w00, w10, w01, w11;
    __builtin_memcpy_inline(&w00, p2c + ((q00 + pz) & 255u), 4);
    __builtin_memcpy_inline(&w10, p2c + ((q10 + pz) & 255u), 4);
    __builtin_memcpy_inline(&w01, p2c + ((q01 + pz) & 255u), 4);
    __builtin_memcpy_inline(&w11, p2c + ((q11 + pz) & 255u), 4);
    const float f000 = noise_perlin_dot(w00 & 0xFFu, fx0, fy0, fz0);
    const float f100 = noise_perlin_dot(w10 & 0xFFu, fx1, fy0, fz0);
    const float f010 = noise_perlin_dot(w01 & 0xFFu, fx0, fy1, fz0);
    const float f110 = noise_perlin_dot(w11 & 0xFFu, fx1, fy1, fz0);
    const float f001 = noise_perlin_dot((w00 >> 8) & 0xFFu, fx0, fy0, fz1);
    const float f101 = noise_perlin_dot((w10 >> 8) & 0xFFu, fx1, fy0, fz1);
    const float f011 = noise_perlin_dot((w01 >> 8) & 0xFFu, fx0, fy1, fz1);
    const float f111 = noise_perlin_dot((w11 >> 8) & 0xFFu, fx1, fy1, fz1);
    /* A zero gradient component still propagates a nonfinite legacy Y offset. */
    const float result = math_lerp3f(dx, dy, dz, f000, f100, f010, f110, f001, f101, f011, f111);
    return legacy ? result + 0.0f * fy0 : result;
}

static inline void noise_perlin_prepare(const uint32_t *restrict packed,
                                        uint32_t *restrict perm) {
    __builtin_assume_dereferenceable(packed, 64 * sizeof(uint32_t));
    perm = __builtin_assume_aligned(perm, 64);
    __builtin_assume_dereferenceable(perm, 257 * sizeof(uint32_t));
#pragma clang loop vectorize(enable)
    for (uint32_t n = 0; n < 256; ++n)
        perm[n] = __math_perlin_perm_index(packed, n);
    // allow adjacent unaligned loads for the last element, see below
    perm[256] = perm[0];
}

static inline __attribute__((pure, always_inline)) uint64_t
noise_perlin_perm2(const uint32_t *restrict perm, uint32_t i) {
    __builtin_assume_dereferenceable(perm, 257 * sizeof(uint32_t));
    uint64_t w;
    __builtin_memcpy_inline(&w, perm + (i & 255u), 8);
    return w;
}

static const uint32_t
__attribute__((aligned(64))) noise_perlin_gradient_codes[16] = {
    /* ( 1,  1,  0) */ ( 1 + 1) | (( 1 + 1) << 2) | (( 0 + 1) << 4),
    /* (-1,  1,  0) */ (-1 + 1) | (( 1 + 1) << 2) | (( 0 + 1) << 4),
    /* ( 1, -1,  0) */ ( 1 + 1) | ((-1 + 1) << 2) | (( 0 + 1) << 4),
    /* (-1, -1,  0) */ (-1 + 1) | ((-1 + 1) << 2) | (( 0 + 1) << 4),
    /* ( 1,  0,  1) */ ( 1 + 1) | (( 0 + 1) << 2) | (( 1 + 1) << 4),
    /* (-1,  0,  1) */ (-1 + 1) | (( 0 + 1) << 2) | (( 1 + 1) << 4),
    /* ( 1,  0, -1) */ ( 1 + 1) | (( 0 + 1) << 2) | ((-1 + 1) << 4),
    /* (-1,  0, -1) */ (-1 + 1) | (( 0 + 1) << 2) | ((-1 + 1) << 4),
    /* ( 0,  1,  1) */ ( 0 + 1) | (( 1 + 1) << 2) | (( 1 + 1) << 4),
    /* ( 0, -1,  1) */ ( 0 + 1) | ((-1 + 1) << 2) | (( 1 + 1) << 4),
    /* ( 0,  1, -1) */ ( 0 + 1) | (( 1 + 1) << 2) | ((-1 + 1) << 4),
    /* ( 0, -1, -1) */ ( 0 + 1) | ((-1 + 1) << 2) | ((-1 + 1) << 4),
    /* ( 1,  1,  0) */ ( 1 + 1) | (( 1 + 1) << 2) | (( 0 + 1) << 4),
    /* ( 0, -1,  1) */ ( 0 + 1) | ((-1 + 1) << 2) | (( 1 + 1) << 4),
    /* (-1,  1,  0) */ (-1 + 1) | (( 1 + 1) << 2) | (( 0 + 1) << 4),
    /* ( 0, -1, -1) */ ( 0 + 1) | ((-1 + 1) << 2) | ((-1 + 1) << 4),
};

static inline void noise_perlin_build_pairs(const uint32_t *restrict perm,
                                            uint32_t *restrict pairs) {
    perm = __builtin_assume_aligned(perm, 64);
    pairs = __builtin_assume_aligned(pairs, 64);
    __builtin_assume_separate_storage(perm, pairs);
#pragma clang loop vectorize(enable)
    for (uint32_t n = 0; n < 256; ++n)
        pairs[n] = noise_perlin_gradient_codes[perm[n] & 15u];
    const uint32_t first = pairs[0];
#pragma clang loop vectorize(enable)
    for (uint32_t n = 0; n < 255; ++n)
        pairs[n] |= pairs[n + 1] << 8;
    pairs[255] |= first << 8;
}

static inline void noise_perlin_cell_prepare(
    const uint32_t *restrict pairs, uint32_t q00, uint32_t q10, uint32_t q01,
    uint32_t q11, uint32_t pz, float fx0, float fx1, float fz0, float fz1,
    float *restrict ca, float *restrict cb, float *restrict cc) {
    const uint32_t p00 = pairs[(q00 + pz) & 255u];
    const uint32_t p10 = pairs[(q10 + pz) & 255u];
    const uint32_t p01 = pairs[(q01 + pz) & 255u];
    const uint32_t p11 = pairs[(q11 + pz) & 255u];
    const uint32_t code[8] = {
        p00 & 63u, p10 & 63u, p01 & 63u, p11 & 63u,
        p00 >> 8, p10 >> 8, p01 >> 8, p11 >> 8
    };
#pragma clang loop vectorize(enable)
    for (uint32_t corner = 0; corner < 8; ++corner) {
        ca[corner] =
                (float) ((int32_t) (code[corner] & 3u) - 1) * ((corner & 1u) ? fx1 : fx0);
        cb[corner] = (float) ((int32_t) ((code[corner] >> 2) & 3u) - 1);
        cc[corner] = (float) ((int32_t) ((code[corner] >> 4) & 3u) - 1) *
                     ((corner & 4u) ? fz1 : fz0);
    }
}

static inline __attribute__((const, always_inline)) float
noise_perlin_cached_dot(float ca, float cb, float cc, float y) {
    // fma is allowed because mul is exact
#pragma clang fp contract(on)
    return (ca + cb * y) + cc;
}

static inline __attribute__((pure, always_inline)) float
noise_perlin_cell_corners(const float *restrict ca, const float *restrict cb,
                          const float *restrict cc, float dx, float fy0,
                          float dy, float dz) {
    const float fy1 = fy0 - 1.0f;
    const float f000 = noise_perlin_cached_dot(ca[0], cb[0], cc[0], fy0);
    const float f100 = noise_perlin_cached_dot(ca[1], cb[1], cc[1], fy0);
    const float f010 = noise_perlin_cached_dot(ca[2], cb[2], cc[2], fy1);
    const float f110 = noise_perlin_cached_dot(ca[3], cb[3], cc[3], fy1);
    const float f001 = noise_perlin_cached_dot(ca[4], cb[4], cc[4], fy0);
    const float f101 = noise_perlin_cached_dot(ca[5], cb[5], cc[5], fy0);
    const float f011 = noise_perlin_cached_dot(ca[6], cb[6], cc[6], fy1);
    const float f111 = noise_perlin_cached_dot(ca[7], cb[7], cc[7], fy1);
    return math_lerp3f(dx, dy, dz, f000, f100, f010, f110, f001, f101, f011,
                       f111);
}

static inline __attribute__((flatten, hot, noinline,
    min_vector_width(NOISEOPT_FLOAT_LANES * 32))) void
noise_perlin_area_unshifted_direct(const uint32_t *restrict permutations,
                                   double originX, double originY,
                                   double originZ, double yScale, bool legacy,
                                   float *restrict output,
                                   sampling_region_t region, double scaleXz,
                                   double scaleY, float outputScale) {
    /* Zero-sized regions are filtered by the caller. */
    __builtin_assume(region.sizeX != 0);
    __builtin_assume(region.sizeY != 0);
    __builtin_assume(region.sizeZ != 0);
    __builtin_assume(permutations != NULL && output != NULL);
    __builtin_assume_dereferenceable(permutations, 64 * sizeof(uint32_t));
    __builtin_assume_dereferenceable(output, sizeof(float));
    __attribute__((aligned(64))) uint32_t perm[257];
    __attribute__((aligned(64))) uint8_t p2c[264];
    __builtin_assume_separate_storage(perm, p2c);
    __builtin_assume_separate_storage(p2c, output);
    noise_perlin_prepare(permutations, perm);
    noise_perlin_build_p2c(permutations, p2c);
    __attribute__((aligned(64))) uint32_t py[__MATH_PERLIN_MAX_Y],
            q00[__MATH_PERLIN_MAX_Y], q01[__MATH_PERLIN_MAX_Y],
            q10[__MATH_PERLIN_MAX_Y], q11[__MATH_PERLIN_MAX_Y];
    __attribute__((aligned(64))) float yf0[__MATH_PERLIN_MAX_Y],
            yd[__MATH_PERLIN_MAX_Y];
    __attribute__((aligned(64))) uint32_t zp[__MATH_PERLIN_MAX_Z];
    __attribute__((aligned(64))) float zf0[__MATH_PERLIN_MAX_Z],
            zf1[__MATH_PERLIN_MAX_Z], zd[__MATH_PERLIN_MAX_Z];
    for (uint32_t yBase = 0; yBase < region.sizeY;) {
        const uint32_t count =
                min(region.sizeY - yBase, (uint32_t)__MATH_PERLIN_MAX_Y);
        __builtin_assume(count > 0 && count <= __MATH_PERLIN_MAX_Y);
#pragma clang loop vectorize(enable)
        for (uint32_t j = 0; j < count; ++j) {
            const int32_t block =
                    region.minBlockY + (int32_t) (yBase + j) * (int32_t) region.stepBlockY;
            const double y = (double) block * scaleY;
            const double yy = math_octave_maintainPrecision(y) + originY;
            const double fl = floor(yy), rel = yy - fl;
            const double fy =
                    legacy
                        ? floor(((y >= 0.0 && y < rel) ? y : rel) / yScale + 1.0E-7) * yScale
                        : 0.0;
            py[j] = (uint32_t) (int32_t) fl & 255u;
            yf0[j] = (float) (rel - fy);
            yd[j] = math_perlinFade((float) rel);
        }
        for (uint32_t zBase = 0; zBase < region.sizeZ;) {
            const uint32_t zCount =
                    min(region.sizeZ - zBase, (uint32_t)__MATH_PERLIN_MAX_Z);
            __builtin_assume(zCount > 0 && zCount <= __MATH_PERLIN_MAX_Z);
#pragma clang loop vectorize(enable)
            for (uint32_t j = 0; j < zCount; ++j) {
                const int32_t block = region.minBlockZ +
                                      (int32_t) (zBase + j) * (int32_t) region.stepBlockZ;
                const double zz =
                        math_octave_maintainPrecision((double) block * scaleXz) + originZ;
                const double fl = floor(zz);
                zp[j] = (uint32_t) (int32_t) fl;
                zf0[j] = (float) (zz - fl);
                zf1[j] = zf0[j] - 1.0f;
                zd[j] = math_perlinFade(zf0[j]);
            }
            for (uint32_t ix = 0; ix < region.sizeX; ++ix) {
                const int32_t block =
                        region.minBlockX + (int32_t) ix * (int32_t) region.stepBlockX;
                const double xx =
                        math_octave_maintainPrecision((double) block * scaleXz) + originX;
                const double fl = floor(xx);
                const uint32_t px = (uint32_t) (int32_t) fl;
                const float fx0 = (float) (xx - fl), fx1 = fx0 - 1.0f,
                        dx = math_perlinFade(fx0);
                const uint64_t aa = noise_perlin_perm2(perm, px);
                const uint32_t a0 = (uint32_t) aa, a1 = (uint32_t) (aa >> 32);
#pragma clang loop vectorize(enable)
                for (uint32_t j = 0; j < count; ++j) {
                    const uint64_t w0 = noise_perlin_perm2(perm, a0 + py[j]);
                    const uint64_t w1 = noise_perlin_perm2(perm, a1 + py[j]);
                    q00[j] = (uint32_t) w0;
                    q10[j] = (uint32_t) w1;
                    q01[j] = (uint32_t) (w0 >> 32);
                    q11[j] = (uint32_t) (w1 >> 32);
                }
                for (uint32_t iz = 0; iz < zCount; ++iz) {
                    const uint32_t pz = zp[iz];
                    const float fz0 = zf0[iz], fz1 = zf1[iz], dz = zd[iz];
                    float *restrict out =
                            output +
                            ((size_t) (zBase + iz) * region.sizeX + ix) * region.sizeY + yBase;
#pragma clang loop vectorize_width(NOISEOPT_FLOAT_LANES) interleave_count(1)
                    for (uint32_t j = 0; j < count; ++j) {
                        const float v = noise_perlin_corners(
                            p2c, q00[j], q10[j], q01[j], q11[j], pz, fx0, fx1, dx, yf0[j],
                            yf0[j] - 1.0f, yd[j], fz0, fz1, dz, legacy);
                        out[j] += v * outputScale;
                    }
                }
            }
            zBase += zCount;
        }
        yBase += count;
    }
}

static inline __attribute__((flatten, hot, always_inline,
    min_vector_width(NOISEOPT_FLOAT_LANES * 32))) void
noise_perlin_area_paired_core(const uint32_t *restrict permutations,
                              double originX, double originY, double originZ,
                              double yScale, bool legacy,
                              float *restrict output, sampling_region_t region,
                              double scaleXz, double scaleY,
                              float outputScale) {
    __builtin_assume(region.sizeX != 0 && region.sizeY != 0 && region.sizeZ != 0);
    __builtin_assume(permutations != NULL && output != NULL);
    __builtin_assume_dereferenceable(permutations, 64 * sizeof(uint32_t));
    __builtin_assume_dereferenceable(output, sizeof(float));
    __attribute__((aligned(64))) uint32_t perm[257], pairs[256];
    __attribute__((aligned(64))) uint8_t p2c[264];
    __builtin_assume_separate_storage(perm, pairs);
    __builtin_assume_separate_storage(pairs, p2c);
    __builtin_assume_separate_storage(p2c, output);
    noise_perlin_prepare(permutations, perm);
    noise_perlin_build_p2c(permutations, p2c);
    __attribute__((aligned(64))) uint32_t py[__MATH_PERLIN_MAX_Y],
            q00[__MATH_PERLIN_MAX_Y], q10[__MATH_PERLIN_MAX_Y],
            q01[__MATH_PERLIN_MAX_Y], q11[__MATH_PERLIN_MAX_Y];
    __attribute__((aligned(64))) float yf0[__MATH_PERLIN_MAX_Y],
            yd[__MATH_PERLIN_MAX_Y];
    __attribute__((aligned(64))) uint32_t zp[__MATH_PERLIN_MAX_Z];
    __attribute__((aligned(64))) float zf0[__MATH_PERLIN_MAX_Z],
            zf1[__MATH_PERLIN_MAX_Z], zd[__MATH_PERLIN_MAX_Z];
    uint16_t run_begin[__MATH_PERLIN_MAX_Y], run_end[__MATH_PERLIN_MAX_Y];
    for (uint32_t yBase = 0; yBase < region.sizeY;) {
        const uint32_t count =
                min(region.sizeY - yBase, (uint32_t)__MATH_PERLIN_MAX_Y);
        __builtin_assume(count != 0 && count <= __MATH_PERLIN_MAX_Y);
#pragma clang loop vectorize(enable)
        for (uint32_t j = 0; j < count; ++j) {
            const int32_t block =
                    region.minBlockY + (int32_t) (yBase + j) * (int32_t) region.stepBlockY;
            const double y = (double) block * scaleY;
            const double yy = math_octave_maintainPrecision(y) + originY;
            const double fl = floor(yy), rel = yy - fl;
            const double fy =
                    legacy
                        ? floor(((y >= 0.0 && y < rel) ? y : rel) / yScale + 1.0E-7) * yScale
                        : 0.0;
            py[j] = (uint32_t) (int32_t) fl & 255u;
            yf0[j] = (float) (rel - fy);
            yd[j] = math_perlinFade((float) rel);
        }
        uint32_t runs = 0, longest = 0;
        for (uint32_t begin = 0; begin < count;) {
            uint32_t end = begin + 1;
            while (end < count && py[end] == py[begin])
                ++end;
            run_begin[runs] = (uint16_t) begin;
            run_end[runs++] = (uint16_t) end;
            longest = max(longest, end - begin);
            begin = end;
        }
        const bool cached = longest >= 16u && runs * 8u <= count;
        if (cached) {
            noise_perlin_build_pairs(perm, pairs);
        }
        for (uint32_t zBase = 0; zBase < region.sizeZ;) {
            const uint32_t zCount =
                    min(region.sizeZ - zBase, (uint32_t)__MATH_PERLIN_MAX_Z);
            __builtin_assume(zCount != 0 && zCount <= __MATH_PERLIN_MAX_Z);
#pragma clang loop vectorize(enable)
            for (uint32_t j = 0; j < zCount; ++j) {
                const int32_t block = region.minBlockZ +
                                      (int32_t) (zBase + j) * (int32_t) region.stepBlockZ;
                const double zz =
                        math_octave_maintainPrecision((double) block * scaleXz) + originZ;
                const double fl = floor(zz);
                zp[j] = (uint32_t) (int32_t) fl;
                zf0[j] = (float) (zz - fl);
                zf1[j] = zf0[j] - 1.0f;
                zd[j] = math_perlinFade(zf0[j]);
            }
            for (uint32_t ix = 0; ix < region.sizeX; ++ix) {
                const int32_t block =
                        region.minBlockX + (int32_t) ix * (int32_t) region.stepBlockX;
                const double xx =
                        math_octave_maintainPrecision((double) block * scaleXz) + originX;
                const double fl = floor(xx);
                const uint32_t px = (uint32_t) (int32_t) fl;
                const float fx0 = (float) (xx - fl), fx1 = fx0 - 1.0f,
                        dx = math_perlinFade(fx0);
                const uint64_t aa = noise_perlin_perm2(perm, px);
                const uint32_t a0 = (uint32_t) aa, a1 = (uint32_t) (aa >> 32);
                const uint32_t qCount = cached ? runs : count;
#pragma clang loop vectorize(enable)
                for (uint32_t j = 0; j < qCount; ++j) {
                    const uint32_t y = py[cached ? run_begin[j] : j];
                    const uint64_t w0 = noise_perlin_perm2(perm, a0 + y);
                    const uint64_t w1 = noise_perlin_perm2(perm, a1 + y);
                    q00[j] = (uint32_t) w0;
                    q10[j] = (uint32_t) w1;
                    q01[j] = (uint32_t) (w0 >> 32);
                    q11[j] = (uint32_t) (w1 >> 32);
                }
                for (uint32_t iz = 0; iz < zCount; ++iz) {
                    const uint32_t pz = zp[iz];
                    const float fz0 = zf0[iz], fz1 = zf1[iz], dz = zd[iz];
                    float *restrict out =
                            output +
                            ((size_t) (zBase + iz) * region.sizeX + ix) * region.sizeY + yBase;
                    if (cached) {
                        __attribute__((aligned(64))) float ca[8], cb[8], cc[8];
                        for (uint32_t r = 0; r < runs; ++r) {
                            noise_perlin_cell_prepare(pairs, q00[r], q10[r], q01[r], q11[r],
                                                      pz, fx0, fx1, fz0, fz1, ca, cb, cc);
                            const uint32_t begin = run_begin[r], end = run_end[r];
#pragma clang loop vectorize_width(NOISEOPT_FLOAT_LANES) interleave_count(1)
                            for (uint32_t j = begin; j < end; ++j) {
                                out[j] += noise_perlin_cell_corners(ca, cb, cc, dx, yf0[j],
                                                                    yd[j], dz) *
                                        outputScale;
                            }
                        }
                    } else {
#pragma clang loop vectorize_width(NOISEOPT_FLOAT_LANES) interleave_count(1)
                        for (uint32_t j = 0; j < count; ++j) {
                            out[j] +=
                                    noise_perlin_corners(p2c, q00[j], q10[j], q01[j], q11[j], pz,
                                                         fx0, fx1, dx, yf0[j], yf0[j] - 1.0f,
                                                         yd[j], fz0, fz1, dz, legacy) *
                                    outputScale;
                        }
                    }
                }
            }
            zBase += zCount;
        }
        yBase += count;
    }
}

static __attribute__((noinline, hot,
    min_vector_width(NOISEOPT_FLOAT_LANES * 32))) void
noise_perlin_area_paired_base(const uint32_t *restrict permutations,
                              double originX, double originY, double originZ,
                              float *restrict output, sampling_region_t region,
                              double scaleXz, double scaleY,
                              float outputScale) {
    noise_perlin_area_paired_core(permutations, originX, originY, originZ, 1.0,
                                  false, output, region, scaleXz, scaleY,
                                  outputScale);
}

static __attribute__((noinline, hot,
    min_vector_width(NOISEOPT_FLOAT_LANES * 32))) void
noise_perlin_area_paired_legacy(const uint32_t *restrict permutations,
                                double originX, double originY, double originZ,
                                double yScale, float *restrict output,
                                sampling_region_t region, double scaleXz,
                                double scaleY, float outputScale) {
    noise_perlin_area_paired_core(permutations, originX, originY, originZ, yScale,
                                  true, output, region, scaleXz, scaleY,
                                  outputScale);
}

static inline __attribute__((always_inline)) void
noise_perlin_area_unshifted(const uint32_t *restrict permutations,
                            double originX, double originY, double originZ,
                            double yScale, bool legacy, float *restrict output,
                            sampling_region_t region, double scaleXz,
                            double scaleY, float outputScale) {
    if (region.sizeY < 16u)
        noise_perlin_area_unshifted_direct(permutations, originX, originY, originZ,
                                           yScale, legacy, output, region, scaleXz,
                                           scaleY, outputScale);
    else if (legacy)
        noise_perlin_area_paired_legacy(permutations, originX, originY, originZ,
                                        yScale, output, region, scaleXz, scaleY,
                                        outputScale);
    else
        noise_perlin_area_paired_base(permutations, originX, originY, originZ,
                                      output, region, scaleXz, scaleY, outputScale);
}

static inline __attribute__((flatten, hot, noinline,
    min_vector_width(NOISEOPT_FLOAT_LANES * 32))) void
noise_perlin_area_shifted_xyz(const uint32_t *restrict permutations, double originX,
                              double originY, double originZ, double yScale,
                              bool legacy, float *restrict output,
                              sampling_region_t region,
                              const double *restrict shiftX,
                              const double *restrict shiftY,
                              const double *restrict shiftZ, double scaleXz,
                              double scaleY, float outputScale) {
    __builtin_assume(region.sizeX != 0);
    __builtin_assume(region.sizeY != 0);
    __builtin_assume(region.sizeZ != 0);
    __builtin_assume(permutations != NULL && output != NULL);
    __builtin_assume(shiftX != NULL && shiftY != NULL && shiftZ != NULL);
    __builtin_assume_dereferenceable(permutations, 64 * sizeof(uint32_t));
    __builtin_assume_dereferenceable(output, sizeof(float));
    __builtin_assume_dereferenceable(shiftX, sizeof(double));
    __builtin_assume_dereferenceable(shiftY, sizeof(double));
    __builtin_assume_dereferenceable(shiftZ, sizeof(double));
    __attribute__((aligned(64))) uint32_t perm[257];
    __attribute__((aligned(64))) uint8_t p2c[264];
    noise_perlin_prepare(permutations, perm);
    noise_perlin_build_p2c(permutations, p2c);

    const uint32_t size = region.sizeX * region.sizeY * region.sizeZ;
    coord_iter_t it = math_coord_iter_begin(region);
    __attribute__((aligned(64))) int32_t xs[64], ys[64], zs[64];
    __attribute__((aligned(64))) int32_t pxa[64], pya[64], pza[64];
    __attribute__((aligned(64))) float fx0a[64], fy0a[64], fz0a[64], dya[64];
    uint32_t i = 0;
    while (i < size) {
        const uint32_t n = math_coord_iter_next64(&it, xs, ys, zs, size - i);
#pragma clang loop vectorize(enable)
        for (uint32_t j = 0; j < n; ++j) {
            const double x = (double) xs[j] * scaleXz + shiftX[i + j];
            const double y = (double) ys[j] * scaleY + shiftY[i + j];
            const double z = (double) zs[j] * scaleXz + shiftZ[i + j];
            const double xx = math_octave_maintainPrecision(x) + originX;
            const double yy = math_octave_maintainPrecision(y) + originY;
            const double zz = math_octave_maintainPrecision(z) + originZ;
            const double flx = floor(xx), fly = floor(yy), flz = floor(zz);
            const double rely = yy - fly;
            const double fy =
                    legacy
                        ? floor(((y >= 0.0 && y < rely) ? y : rely) / yScale +1.0E-7) * yScale
                        : 0.0;
            pxa[j] = (int32_t) flx;
            pya[j] = (int32_t) fly;
            pza[j] = (int32_t) flz;
            fx0a[j] = (float) (xx - flx);
            fy0a[j] = (float) (rely - fy);
            fz0a[j] = (float) (zz - flz);
            dya[j] = math_perlinFade((float) rely);
        }
        // vectorization factor is pinned because clang is weird
#pragma clang loop vectorize_width(NOISEOPT_FLOAT_LANES) interleave_count(1)
        for (uint32_t j = 0; j < n; ++j) {
            const uint32_t px = (uint32_t) pxa[j], py = (uint32_t) pya[j],
                    pz = (uint32_t) pza[j];
            const float fx0 = fx0a[j], fy0 = fy0a[j], fz0 = fz0a[j];
            const uint64_t aa = noise_perlin_perm2(perm, px);
            const uint32_t a0 = (uint32_t) aa, a1 = (uint32_t) (aa >> 32);
            const uint64_t w0 = noise_perlin_perm2(perm, a0 + (py & 255u));
            const uint64_t w1 = noise_perlin_perm2(perm, a1 + (py & 255u));
            const uint32_t q00 = (uint32_t) w0, q01 = (uint32_t) (w0 >> 32);
            const uint32_t q10 = (uint32_t) w1, q11 = (uint32_t) (w1 >> 32);
            const float v =
                    noise_perlin_corners(p2c, q00, q10, q01, q11, pz, fx0, fx0 - 1.0f,
                                         math_perlinFade(fx0), fy0, fy0 - 1.0f, dya[j],
                                         fz0, fz0 - 1.0f, math_perlinFade(fz0), legacy); {
                output[i + j] += v * outputScale;
            }
        }
        i += n;
    }
}

static inline __attribute__((flatten, hot, noinline,
    min_vector_width(NOISEOPT_FLOAT_LANES * 32))) void
noise_perlin_area_shifted_xz(const uint32_t *restrict permutations, double originX,
                             double originY, double originZ, double yScale,
                             bool legacy, float *restrict output,
                             sampling_region_t region,
                             const double *restrict shiftX,
                             const double *restrict shiftZ, double scaleXz,
                             double scaleY, float outputScale) {
    __builtin_assume(region.sizeX != 0);
    __builtin_assume(region.sizeY != 0);
    __builtin_assume(region.sizeZ != 0);
    __builtin_assume(permutations != NULL && output != NULL);
    __builtin_assume(shiftX != NULL && shiftZ != NULL);
    __builtin_assume_dereferenceable(permutations, 64 * sizeof(uint32_t));
    __builtin_assume_dereferenceable(output, sizeof(float));
    __builtin_assume_dereferenceable(shiftX, sizeof(double));
    __builtin_assume_dereferenceable(shiftZ, sizeof(double));
    __attribute__((aligned(64))) uint32_t perm[257];
    __attribute__((aligned(64))) uint8_t p2c[264];
    noise_perlin_prepare(permutations, perm);
    noise_perlin_build_p2c(permutations, p2c);

    const uint32_t size = region.sizeX * region.sizeY * region.sizeZ;
    coord_iter_t it = math_coord_iter_begin(region);
    __attribute__((aligned(64))) int32_t xs[64], ys[64], zs[64];
    __attribute__((aligned(64))) int32_t pxa[64], pya[64], pza[64];
    __attribute__((aligned(64))) float fx0a[64], fy0a[64], fz0a[64], dya[64];
    uint32_t i = 0;
    while (i < size) {
        const uint32_t n = math_coord_iter_next64(&it, xs, ys, zs, size - i);
#pragma clang loop vectorize(enable)
        for (uint32_t j = 0; j < n; ++j) {
            const double x = (double) xs[j] * scaleXz + shiftX[i + j];
            const double y = (double) ys[j] * scaleY;
            const double z = (double) zs[j] * scaleXz + shiftZ[i + j];
            const double xx = math_octave_maintainPrecision(x) + originX;
            const double yy = math_octave_maintainPrecision(y) + originY;
            const double zz = math_octave_maintainPrecision(z) + originZ;
            const double flx = floor(xx), fly = floor(yy), flz = floor(zz);
            const double rely = yy - fly;
            const double fy =
                    legacy
                        ? floor(((y >= 0.0 && y < rely) ? y : rely) / yScale +1.0E-7) * yScale
                        : 0.0;
            pxa[j] = (int32_t) flx;
            pya[j] = (int32_t) fly;
            pza[j] = (int32_t) flz;
            fx0a[j] = (float) (xx - flx);
            fy0a[j] = (float) (rely - fy);
            fz0a[j] = (float) (zz - flz);
            dya[j] = math_perlinFade((float) rely);
        }
        // vectorization factor is pinned because clang is weird
#pragma clang loop vectorize_width(NOISEOPT_FLOAT_LANES) interleave_count(1)
        for (uint32_t j = 0; j < n; ++j) {
            const uint32_t px = (uint32_t) pxa[j], py = (uint32_t) pya[j],
                    pz = (uint32_t) pza[j];
            const float fx0 = fx0a[j], fy0 = fy0a[j], fz0 = fz0a[j];
            const uint64_t aa = noise_perlin_perm2(perm, px);
            const uint32_t a0 = (uint32_t) aa, a1 = (uint32_t) (aa >> 32);
            const uint64_t w0 = noise_perlin_perm2(perm, a0 + (py & 255u));
            const uint64_t w1 = noise_perlin_perm2(perm, a1 + (py & 255u));
            const uint32_t q00 = (uint32_t) w0, q01 = (uint32_t) (w0 >> 32);
            const uint32_t q10 = (uint32_t) w1, q11 = (uint32_t) (w1 >> 32);
            const float v =
                    noise_perlin_corners(p2c, q00, q10, q01, q11, pz, fx0, fx0 - 1.0f,
                                         math_perlinFade(fx0), fy0, fy0 - 1.0f, dya[j],
                                         fz0, fz0 - 1.0f, math_perlinFade(fz0), legacy); {
                output[i + j] += v * outputScale;
            }
        }
        i += n;
    }
}

static inline __attribute__((always_inline)) void
noise_perlin_area(const uint32_t *restrict permutations, double originX,
                  double originY, double originZ, double yScale, bool legacy,
                  float *restrict output, sampling_region_t region,
                  const double *restrict shiftX, const double *restrict shiftY,
                  const double *restrict shiftZ, double scaleXz, double scaleY,
                  float outputScale) {
    if (shiftX && shiftY && shiftZ)
        noise_perlin_area_shifted_xyz(permutations, originX, originY, originZ, yScale,
                                  legacy, output, region, shiftX, shiftY, shiftZ,
                                  scaleXz, scaleY, outputScale);
    else if (shiftX && !shiftY && shiftZ)
        noise_perlin_area_shifted_xz(permutations, originX, originY, originZ, yScale,
                                  legacy, output, region, shiftX, shiftZ,
                                  scaleXz, scaleY, outputScale);
    else if (!shiftX && !shiftY && !shiftZ)
        noise_perlin_area_unshifted(permutations, originX, originY, originZ, yScale,
                                    legacy, output, region, scaleXz, scaleY,
                                    outputScale);
    else {
        __builtin_trap();
        __builtin_unreachable();
    }
}

static inline __attribute__((always_inline)) void
math_noise_perlin_sample_legacy_area0(
    const uint32_t *restrict permutations, double originX, double originY,
    double originZ, double yScale, float *restrict output,
    sampling_region_t region, const double *restrict shiftX,
    const double *restrict shiftY, const double *restrict shiftZ,
    double scaleXz, double scaleY, float outputScale) {
    noise_perlin_area(permutations, originX, originY, originZ, yScale, true,
                      output, region, shiftX, shiftY, shiftZ, scaleXz, scaleY,
                      outputScale);
}

static inline __attribute__((always_inline)) void
math_noise_perlin_sample_base_area0(
    const uint32_t *restrict permutations, double originX, double originY,
    double originZ, float *restrict output, sampling_region_t region,
    const double *restrict shiftX, const double *restrict shiftY,
    const double *restrict shiftZ, double scaleXz, double scaleY,
    float outputScale) {
    noise_perlin_area(permutations, originX, originY, originZ, 1.0, false, output,
                      region, shiftX, shiftY, shiftZ, scaleXz, scaleY,
                      outputScale);
}

static inline __attribute__((always_inline)) void
math_noise_perlin_sample_legacy_area(
    const uint32_t *restrict permutations, double originX, double originY,
    double originZ, double yScale, float *restrict output, int32_t sizeX,
    int32_t sizeY, int32_t sizeZ, int32_t minBlockX, int32_t minBlockY,
    int32_t minBlockZ, int32_t stepBlockX, int32_t stepBlockY,
    int32_t stepBlockZ, const double *restrict shiftX,
    const double *restrict shiftY, const double *restrict shiftZ,
    double scaleXz, double scaleY, float outputScale) {
    __builtin_assume(sizeX != 0);
    __builtin_assume(sizeY != 0);
    __builtin_assume(sizeZ != 0);
    const sampling_region_t region = {
        sizeX, sizeY, sizeZ,
        minBlockX, minBlockY, minBlockZ,
        stepBlockX, stepBlockY, stepBlockZ
    };
    math_noise_perlin_sample_legacy_area0(permutations, originX, originY, originZ,
                                          yScale, output, region, shiftX, shiftY,
                                          shiftZ, scaleXz, scaleY, outputScale);
}

static __attribute__((always_inline)) void math_noise_perlin_sample_base_area(
    const uint32_t *restrict permutations, double originX, double originY,
    double originZ, float *restrict output, int32_t sizeX, int32_t sizeY,
    int32_t sizeZ, int32_t minBlockX, int32_t minBlockY, int32_t minBlockZ,
    int32_t stepBlockX, int32_t stepBlockY, int32_t stepBlockZ,
    const double *restrict shiftX, const double *restrict shiftY,
    const double *restrict shiftZ, double scaleXz, double scaleY,
    float outputScale) {
    __builtin_assume(sizeX != 0);
    __builtin_assume(sizeY != 0);
    __builtin_assume(sizeZ != 0);
    const sampling_region_t region = {
        sizeX, sizeY, sizeZ,
        minBlockX, minBlockY, minBlockZ,
        stepBlockX, stepBlockY, stepBlockZ
    };
    math_noise_perlin_sample_base_area0(permutations, originX, originY, originZ,
                                        output, region, shiftX, shiftY, shiftZ,
                                        scaleXz, scaleY, outputScale);
}

static __attribute__((aligned(32))) const int8_t noise_end_island_m[625] = {
    -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -12,
    -12, -12, -12, -12, -12, -12, -12, -12, -12, -12, -11, -11, -11, -11, -11,
    -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11, -11,
    -11, -11, -11, -11, -11, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10,
    -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10,
    -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,
    -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -8, -8, -8, -8, -8,
    -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8,
    -8, -8, -8, -8, -8, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7,
    -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7,
    -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -6,
    -6, -6, -6, -6, -6, -6, -6, -6, -6, -6, -5, -5, -5, -5, -5,
    -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5,
    -5, -5, -5, -5, -5, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4,
    -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4,
    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
    -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2,
    -2, -2, -2, -2, -2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
    1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
    3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
    3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
    4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
    5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
    6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
    9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9,
    9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10,
    10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
    10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
    11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
    12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
    12, 12, 12, 12, 12, 12, 12, 12, 12, 12
};
static __attribute__((aligned(32))) const int8_t noise_end_island_n[625] = {
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -12, -11, -10, -9, -8,
    -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, 10, 11, 12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
    -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -12, -11, -10, -9, -8,
    -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, 10, 11, 12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
    -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -12, -11, -10, -9, -8,
    -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, 10, 11, 12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
    -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -12, -11, -10, -9, -8,
    -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, 10, 11, 12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
    -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -12, -11, -10, -9, -8,
    -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, 10, 11, 12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
    -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -12, -11, -10, -9, -8,
    -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, 10, 11, 12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
    -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -12, -11, -10, -9, -8,
    -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, 10, 11, 12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
    -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12, -12, -11, -10, -9, -8,
    -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7,
    8, 9, 10, 11, 12, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
    -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
    -12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2,
    3, 4, 5, 6, 7, 8, 9, 10, 11, 12
};

static inline
__attribute__((pure, flatten, hot,
    min_vector_width(NOISEOPT_DOUBLE_LANES * 64))) float
math_end_islands_sample(const uint32_t *restrict const simplex_permutations,
                        const int32_t x, const int32_t z) {
    const int32_t i = x / 2;
    const int32_t j = z / 2;
    const int32_t k = x % 2;
    const int32_t l = z % 2;
    float f = -100.0F;

    __attribute__((aligned(64))) int8_t hit[25 * 25];
    __builtin_assume_separate_storage(hit, simplex_permutations);
    const int64_t omin = labs(i) - 12LL;
    const int64_t pmin = labs(j) - 12LL;

    if (omin * omin + pmin * pmin > 4096LL) {
#pragma clang loop vectorize_width(NOISEOPT_DOUBLE_LANES) interleave_count(1)
        for (uint32_t idx = 0; idx < 25 * 25; idx++) {
            const int32_t o = i + (int32_t) noise_end_island_m[idx];
            const int32_t p = j + (int32_t) noise_end_island_n[idx];
            const double sample = noise_simplex_sample2d_impl(
                simplex_permutations, (double) o, (double) p, true);
            hit[idx] = (int8_t) (sample < -0.9F);
        }
    } else {
#pragma clang loop vectorize_width(NOISEOPT_DOUBLE_LANES) interleave_count(1)
        for (uint32_t idx = 0; idx < 25 * 25; idx++) {
            const int32_t o = i + (int32_t) noise_end_island_m[idx];
            const int32_t p = j + (int32_t) noise_end_island_n[idx];
            const bool outside = (int64_t) o * o + (int64_t) p * p > 4096LL;
            const double sample =
                    outside
                        ? noise_simplex_sample2d_impl(simplex_permutations, (double) o, (double) p, true)
                        : 0.0;
            hit[idx] = (int8_t) (outside && sample < -0.9F);
        }
    }

    for (uint32_t idx = 0; idx < 25 * 25; idx++) {
        if (hit[idx]) {
            const int32_t m = (int32_t) noise_end_island_m[idx];
            const int32_t n = (int32_t) noise_end_island_n[idx];
            const int32_t o = i + m;
            const int32_t p = j + n;
            const float g1 = fabsf((float) o) * 3439.0F;
            const float g2 = fabsf((float) p) * 147.0F;
            const float g = fmodf((g1 + g2), 13.0F) + 9.0F;
            const float h = (float) (k - m * 2);
            const float q = (float) (l - n * 2);
            float r = 100.0F - sqrtf(h * h + q * q) * g;
            r = clampf(r, -100.0F, 80.0F);
            f = fmaxf(f, r);
        }
    }

    return f;
}

static inline __attribute__((const)) uint32_t
math_biome_access_sample(const int64_t theSeed, const int32_t x, const int32_t y, const int32_t z) {
    const int32_t var0 = x - 2;
    const int32_t var1 = y - 2;
    const int32_t var2 = z - 2;
    const int32_t var3 = var0 >> 2;
    const int32_t var4 = var1 >> 2;
    const int32_t var5 = var2 >> 2;
    const double var6 = (double) (var0 & 3) / 4.0;
    const double var7 = (double) (var1 & 3) / 4.0;
    const double var8 = (double) (var2 & 3) / 4.0;
    uint32_t var9 = 0;
    double var10 = DBL_MAX;

    double var28s[8];

#pragma clang loop vectorize_width(NOISEOPT_DOUBLE_LANES) interleave_count(NOISEOPT_BIOME_INTERLEAVE)
    for (uint32_t var11 = 0; var11 < 8; ++var11) {
        uint32_t var12 = var11 & 4;
        uint32_t var13 = var11 & 2;
        uint32_t var14 = var11 & 1;
        int64_t var15 = var12 ? var3 + 1 : var3;
        int64_t var16 = var13 ? var4 + 1 : var4;
        int64_t var17 = var14 ? var5 + 1 : var5;
        double var18 = var12 ? var6 - 1.0 : var6;
        double var19 = var13 ? var7 - 1.0 : var7;
        double var20 = var14 ? var8 - 1.0 : var8;
        int64_t var21 = theSeed * (theSeed * 6364136223846793005L + 1442695040888963407L) + var15;
        var21 = var21 * (var21 * 6364136223846793005L + 1442695040888963407L) + var16;
        var21 = var21 * (var21 * 6364136223846793005L + 1442695040888963407L) + var17;
        var21 = var21 * (var21 * 6364136223846793005L + 1442695040888963407L) + var15;
        var21 = var21 * (var21 * 6364136223846793005L + 1442695040888963407L) + var16;
        var21 = var21 * (var21 * 6364136223846793005L + 1442695040888963407L) + var17;
        double var22 = (double) ((var21 >> 24) & 1023) / 1024.0;
        double var23 = (var22 - 0.5) * 0.9;
        var21 = var21 * (var21 * 6364136223846793005L + 1442695040888963407L) + theSeed;
        double var24 = (double) ((var21 >> 24) & 1023) / 1024.0;
        double var25 = (var24 - 0.5) * 0.9;
        var21 = var21 * (var21 * 6364136223846793005L + 1442695040888963407L) + theSeed;
        double var26 = (double) ((var21 >> 24) & 1023) / 1024.0;
        double var27 = (var26 - 0.5) * 0.9;
        double var28 = math_square(var20 + var27) + math_square(var19 + var25) + math_square(var18 + var23);
        var28s[var11] = var28;
    }

    for (int i = 0; i < 8; ++i) {
        if (var10 > var28s[i]) {
            var9 = i;
            var10 = var28s[i];
        }
    }

    return var9;
}

typedef const struct aquifer_data {
    int32_t startX;
    int32_t startY;
    int32_t startZ;
    int32_t sizeX;
    int32_t sizeZ;
} aquifer_data_t;

static inline __attribute__((const)) uint32_t
math_aquifer_index(const aquifer_data_t *restrict const aquiferData, const int32_t x, const int32_t y,
                   const int32_t z) {
    int i = x - aquiferData->startX;
    int j = y - aquiferData->startY;
    int k = z - aquiferData->startZ;
    return (j * aquiferData->sizeZ + k) * aquiferData->sizeX + i;
}

static inline __attribute__((const)) int32_t
math_aquifer_unpackPackedX(uint32_t packed) {
    return packed >> 8;
}

static inline __attribute__((const)) int32_t
math_aquifer_unpackPackedY(uint32_t packed) {
    return (packed >> 4) & 0b1111;
}

static inline __attribute__((const)) int32_t
math_aquifer_unpackPackedZ(uint32_t packed) {
    return packed & 0b1111;
}

static inline void
math_aquifer_refreshDistPosIdx(const uint16_t *restrict const packedBlockPositions, uint32_t *restrict const res,
                               const aquifer_data_t *restrict const aquiferData,
                               const int32_t x, const int32_t y, const int32_t z) {
    int32_t gx = (x - 5) >> 4;
    int32_t gy = math_floorDiv(y + 1, 12) - 1;
    int32_t gz = (z - 5) >> 4;
    uint32_t A = UINT32_MAX;
    uint32_t B = UINT32_MAX;
    uint32_t C = UINT32_MAX;
    uint32_t D = UINT32_MAX;

    uint32_t ps[12];

#pragma clang loop vectorize(enable)
    for (uint32_t n = 0; n < 12; ++n) {
        const int32_t ox = n & 1u;
        const int32_t oz = (n >> 1) & 1u;
        const int32_t oy = n >> 2;
        const uint32_t pos = math_aquifer_index(aquiferData, gx + ox, gy + oy, gz + oz);
        const uint32_t packed = packedBlockPositions[pos];
        const int32_t dx = (gx + ox) * 16 + math_aquifer_unpackPackedX(packed) - x;
        const int32_t dy = (gy + oy) * 12 + math_aquifer_unpackPackedY(packed) - y;
        const int32_t dz = (gz + oz) * 16 + math_aquifer_unpackPackedZ(packed) - z;
        const uint32_t dist = dx * dx + dy * dy + dz * dz;
        ps[n] = (dist << 20) | ((11u - n) << 16) | pos;
    }

    A = ps[0];

    for (uint32_t i = 1; i < 12; i++) {
        uint32_t p1 = ps[i];
        if (p1 <= C) {
            uint32_t n11 = max(A, p1);
            A = min(A, p1);

            uint32_t n12 = max(B, n11);
            B = min(B, n11);

            uint32_t n13 = max(C, n12);
            C = min(C, n12);

            D = min(D, n13);
        }
    }

    res[0] = A;
    res[1] = B;
    res[2] = C;
    res[3] = D;
}

// branch node: occupies two slots, first with node_minmacs, second with
// branch_children bit 31 set for both slots, bit 30 set for second slot leaf
// node: occupies one slot, with biome ID in state
typedef const struct biome_search_tree_node {
    // bit 31: set if branch node, clear if leaf node
    // bit 30: set if is branch node children offsets
    // bit 0-29: biome ID (only valid for leaf nodes)
    uint32_t state;
    union {
        struct {
            uint32_t children_offset[7]; // at most 7 children, 0 is reserved and means no child
        } branch_children;
        struct {
            int16_t maxs[7];
            int16_t mins[7];
        } node_minmaxs;
    };
} biome_search_tree_node_t;

static inline bool __attribute__((pure))
__math_biome_search_tree_is_branch(const biome_search_tree_node_t * restrict const node) {
    return (node->state & (1U << 31)) != 0;
}

static inline bool __attribute__((pure))
__math_biome_search_tree_is_branch_children(const biome_search_tree_node_t * restrict const node) {
    return (node->state & (1U << 30)) != 0;
}

static inline void
__math_biome_search_tree_validate_node(const biome_search_tree_node_t * restrict const node) {
    if (!__math_biome_search_tree_is_branch(node) && __math_biome_search_tree_is_branch_children(node)) {
        // invalid state
        __builtin_trap();
    }
    if (__math_biome_search_tree_is_branch(node)) {
        if (!__math_biome_search_tree_is_branch(node + 1) ||
            !__math_biome_search_tree_is_branch_children(node + 1)) {
            // branch node must have children offsets in the next slot
            __builtin_trap();
        }
        if (!__math_biome_search_tree_is_branch(node + 1) &&
            __math_biome_search_tree_is_branch_children(node + 1)) {
            // branch node children offsets must be in a branch node
            __builtin_trap();
        }
    }
}

static inline uint64_t __attribute__((pure))
__math_biome_search_tree_distance_func(const biome_search_tree_node_t * restrict const node,
                                       const int16_t * restrict const target) {
    if (__math_biome_search_tree_is_branch_children(node)) {
        __builtin_trap();
    }

    uint64_t res = 0;

    for (uint32_t i = 0; i < 7; i++) {
        const int32_t l = (int32_t) target[i] - (int32_t) node->node_minmaxs.maxs[i];
        const int32_t m = (int32_t) node->node_minmaxs.mins[i] - (int32_t) target[i];
        const uint32_t dist = (uint32_t) (l >= 0 ? l : max(m, 0));
        res += (uint64_t) (dist * dist);
    }

    return res;
}

static inline __attribute__((pure, always_inline)) uint64_t
noise_biome_tree_distance64(const biome_search_tree_node_t *restrict node,
                            const int16_t *restrict target) {
    if (__math_biome_search_tree_is_branch_children(node))
        __builtin_trap();
    uint64_t sum = 0;
    for (uint32_t i = 0; i < 7; ++i) {
        const int64_t l = (int32_t) target[i] - (int32_t) node->node_minmaxs.maxs[i];
        const int64_t m = (int32_t) node->node_minmaxs.mins[i] - (int32_t) target[i];
        const int64_t d = l >= 0 ? l : max(m, 0LL);
        sum += (uint64_t) (d * d);
    }
    return sum;
}

typedef struct __biome_search_stack_element {
    uint32_t node;
    uint8_t iter_i;
} __biome_search_stack_element_t;

static inline uint32_t __attribute__((pure, always_inline))
math_biome_search_tree_calc(const biome_search_tree_node_t * restrict const nodes,
                            const int16_t * restrict const target,
                            UNUSED_ATTR const uint32_t nodes_c, const uint32_t tree_depth) {
    // no recursion allowed, because this needs to be eventually ported to GPU

    if (!__math_biome_search_tree_is_branch(nodes + 1)) {
        return nodes[1].state & 0x3FFFFFFF;
    }

    __biome_search_stack_element_t working[tree_depth];
    uint32_t top = 0;
    uint32_t current_optimal_node = 1;
    uint64_t current_optimal_dist = UINT64_MAX;

    working[top ++] = (__biome_search_stack_element_t) { .node = 1, .iter_i = 0 };
    __math_biome_search_tree_validate_node(nodes + 1);

    // loop_start:
    while (top) {
        uint32_t cur_node = working[top - 1].node;
        uint32_t iter_i = working[top - 1].iter_i;
        __math_biome_search_tree_validate_node(nodes + cur_node);

        uint32_t child_node;
        if (iter_i >= 7 || !(child_node = nodes[cur_node + 1].branch_children.children_offset[iter_i])) {
            // no more children, pop the stack
            top--;
            continue;
        }

        // bump iter index for the current node
        working[top - 1].iter_i++;

        __math_biome_search_tree_validate_node(nodes + child_node);

        uint64_t d = noise_biome_tree_distance64(nodes + child_node, target);

        if (d >= current_optimal_dist) {
            // this child cannot be better than the current optimal, skip it
            continue;
        }

        if (__math_biome_search_tree_is_branch(nodes + child_node)) {
            // this is a branch node, push it to the stack
            working[top ++] = (__biome_search_stack_element_t) { .node = child_node, .iter_i = 0 };
            if (top >= tree_depth) {
                // stack overflow, this should never happen
                __builtin_trap();
            }
        } else {
            current_optimal_dist = d;
            current_optimal_node = child_node;
        }
    }

    return nodes[current_optimal_node].state & 0x3FFFFFFF;
}

static inline uint32_t __attribute__((pure))
math_biome_search_tree_calc_args(const biome_search_tree_node_t * restrict const nodes,
                                 const uint32_t nodes_c, const uint32_t tree_depth,
                                 int16_t p0, int16_t p1, int16_t p2, int16_t p3,
                                 int16_t p4, int16_t p5, int16_t p6) {
    const int16_t target[7] = { p0, p1, p2, p3, p4, p5, p6 };
    return math_biome_search_tree_calc(nodes, target, nodes_c, tree_depth);
}

#undef NOISEOPT_FLOAT_LANES
#undef NOISEOPT_DOUBLE_LANES
#undef NOISEOPT_BIOME_INTERLEAVE
#pragma float_control(pop)
