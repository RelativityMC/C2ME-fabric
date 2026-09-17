#pragma once

#include <stdbool.h>
#include <stdint.h>
#include <stddef.h>
#include <float.h>

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

#pragma clang attribute push (__attribute__((always_inline)), apply_to = function)

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
    return __builtin_labs(x);
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

static inline __attribute__((const)) uint32_t
__math_simplex_map(const uint32_t *restrict const permutations, const uint32_t input) {
    uint32_t point = input & 0xFF;
    return (permutations[point >> 2] >> ((input & 3) << 3)) & 0xFF;
}

static inline __attribute__((const)) double math_simplex_dot(const int32_t hash, const double x, const double y,
                                                             const double z) {
    const int32_t loc = hash << 2;
    return FLAT_SIMPLEX_GRAD_F64[loc + 0] * x + FLAT_SIMPLEX_GRAD_F64[loc + 1] * y + FLAT_SIMPLEX_GRAD_F64[loc + 2] * z;
}

static inline __attribute__((const)) double __math_simplex_grad(const int32_t hash, const double x, const double y,
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

static inline double __attribute__((const))
math_noise_simplex_sample2d(const uint32_t *restrict const permutations, const double x, const double y) {
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
    const int32_t r = (int32_t) i & 0xFF;
    const int32_t s = (int32_t) j & 0xFF;
    const int32_t t = __math_simplex_map(permutations, r + __math_simplex_map(permutations, s)) % 12;
    const int32_t u = __math_simplex_map(permutations, r + li + __math_simplex_map(permutations, s + mi)) % 12;
    const int32_t v = __math_simplex_map(permutations, r + 1 + __math_simplex_map(permutations, s + 1)) % 12;
    const double w = __math_simplex_grad(t, h, k, 0.0, 0.5);
    const double z = __math_simplex_grad(u, n, o, 0.0, 0.5);
    const double aa = __math_simplex_grad(v, p, q, 0.0, 0.5);
    return 70.0 * (w + z + aa);
}

static inline __attribute__((const)) float math_perlinFade(const float value) {
    return value * value * value * (value * (value * 6.0f - 15.0f) + 10.0f);
}

static inline __attribute__((pure)) uint32_t __math_perlin_perm_index(const uint32_t *restrict const permutations,
                                                                      uint32_t index) {
    uint32_t point = index & 0xFF;
    return (permutations[point >> 2] >> ((index & 3) << 3)) & 0xFF;
}

static inline __attribute__((const)) float __math_perlin_grad(const uint32_t *restrict const permutations,
                                                              const int32_t px, const int32_t py, const int32_t pz,
                                                              const float fx, const float fy, const float fz) {
    const float f[3] = {fx, fy, fz};
    const int32_t p[3] = {px, py, pz};
    const uint32_t q[3] = {p[0] & 0xFF, p[1] & 0xFF, p[2] & 0xFF};
    const uint32_t hash = __math_perlin_perm_index(
                              permutations,
                              (__math_perlin_perm_index(
                                   permutations,
                                   (__math_perlin_perm_index(permutations, q[0]) + q[1]) & 0xFF
                               ) + q[2]) & 0xFF
                          ) & 0xF;
    const float *const grad = FLAT_SIMPLEX_GRAD_F32 + (hash << 2);
    return grad[0] * f[0] + grad[1] * f[1] + grad[2] * f[2];
}

static inline __attribute__((const)) float
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

static inline __attribute__((const)) float
math_noise_perlin_sample_legacy(const uint32_t *restrict const permutations,
                                const double originX, const double originY, const double originZ,
                                const double x, const double y, const double z,
                                const double yScale) {
    const double d = math_octave_maintainPrecision(x) + originX;
    const double e = math_octave_maintainPrecision(y) + originY;
    const double f = math_octave_maintainPrecision(z) + originZ;
    const double i = floor(d);
    const double j = floor(e);
    const double k = floor(f);
    const double g = d - i;
    const double h = e - j;
    const double l = f - k;
    const double o = floor(((y >= 0.0 && y < h) ? y : h) / yScale + 1.0E-7) * yScale;

    return math_noise_perlin_sample0(permutations, (int32_t) i, (int32_t) j, (int32_t) k, (float) g, (float) (h - o), (float) l, (float) h);
}

static inline __attribute__((const)) float
math_noise_perlin_sample_base(const uint32_t *restrict const permutations,
                              const double originX, const double originY, const double originZ,
                              const double x, const double y, const double z) {
    const double d = math_octave_maintainPrecision(x) + originX;
    const double e = math_octave_maintainPrecision(y) + originY;
    const double f = math_octave_maintainPrecision(z) + originZ;
    const double i = floor(d);
    const double j = floor(e);
    const double k = floor(f);
    const float g = (float) (d - i);
    const float h = (float) (e - j);
    const float l = (float) (f - k);

    return math_noise_perlin_sample0(permutations, (int32_t) i, (int32_t) j, (int32_t) k, g, h, l, h);
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

static inline __attribute__((const)) pos_i32_t
math_sampling_region_index(const sampling_region_t region, const uint32_t index) {
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
math_coord_iter_next16(coord_iter_t *restrict it,
                       int32_t *restrict xs, int32_t *restrict ys, int32_t *restrict zs,
                       uint32_t remaining) {
    const uint32_t n = remaining < 16u ? remaining : 16u;
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
    for (uint32_t j = 0; j < n; j++) {
        xs[j] = it->minX + (int32_t) cx * (int32_t) it->stepX;
        ys[j] = it->minY + (int32_t) cy * (int32_t) it->stepY;
        zs[j] = it->minZ + (int32_t) cz * (int32_t) it->stepZ;
        if (++cy == sizeY) {
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

static inline void
math_noise_perlin_sample_legacy_area0(const uint32_t *restrict const permutations,
                                      const double originX, const double originY, const double originZ,
                                      const double yScale,
                                      float *restrict const output, const sampling_region_t region,
                                      const double *restrict const shiftX, const double *restrict const shiftY,
                                      const double *restrict const shiftZ,
                                      const double scaleXz, const double scaleY, const float outputScale) {
    const uint32_t size = region.sizeX * region.sizeY * region.sizeZ;

    coord_iter_t it = math_coord_iter_begin(region);
    uint32_t i = 0;
    int32_t xs[16], ys[16], zs[16];

    if (shiftX && shiftY && shiftZ) {
        while (i < size) {
            const uint32_t n = math_coord_iter_next16(&it, xs, ys, zs, size - i);
            __builtin_assume(n >= 1 && n <= 16);

#pragma clang loop vectorize(enable)
            for (uint32_t j = 0; j < n; j++) {
                const double x = (double) xs[j] * scaleXz + shiftX[i + j];
                const double y = (double) ys[j] * scaleY + shiftY[i + j];
                const double z = (double) zs[j] * scaleXz + shiftZ[i + j];
                output[i + j] += math_noise_perlin_sample_legacy(permutations, originX, originY, originZ, x, y, z, yScale) *
                        outputScale;
            }

            i += n;
        }
    } else if (!shiftX && !shiftY && !shiftZ) {
        while (i < size) {
            const uint32_t n = math_coord_iter_next16(&it, xs, ys, zs, size - i);
            __builtin_assume(n >= 1 && n <= 16);

#pragma clang loop vectorize(enable)
            for (uint32_t j = 0; j < n; j++) {
                const double x = (double) xs[j] * scaleXz;
                const double y = (double) ys[j] * scaleY;
                const double z = (double) zs[j] * scaleXz;
                output[i + j] += math_noise_perlin_sample_legacy(permutations, originX, originY, originZ, x, y, z, yScale) *
                        outputScale;
            }

            i += n;
        }
    } else {
        __builtin_trap();
        __builtin_unreachable();
    }
}

static inline void
math_noise_perlin_sample_legacy_area(const uint32_t *restrict const permutations,
                                     const double originX, const double originY, const double originZ,
                                     const double yScale, float *restrict const output,
                                     const int32_t sizeX, const int32_t sizeY, const int32_t sizeZ,
                                     const int32_t minBlockX, const int32_t minBlockY, const int32_t minBlockZ,
                                     const int32_t stepBlockX, const int32_t stepBlockY, const int32_t stepBlockZ,
                                     const double *restrict const shiftX, const double *restrict const shiftY,
                                     const double *restrict const shiftZ,
                                     const double scaleXz, const double scaleY, const float outputScale) {
    sampling_region_t region = {
        .sizeX = sizeX,
        .sizeY = sizeY,
        .sizeZ = sizeZ,
        .minBlockX = minBlockX,
        .minBlockY = minBlockY,
        .minBlockZ = minBlockZ,
        .stepBlockX = stepBlockX,
        .stepBlockY = stepBlockY,
        .stepBlockZ = stepBlockZ,
    };
    math_noise_perlin_sample_legacy_area0(permutations, originX, originY, originZ, yScale, output, region, shiftX,
                                          shiftY, shiftZ, scaleXz, scaleY, outputScale);;
}

static inline void
math_noise_perlin_sample_base_area0(const uint32_t *restrict const permutations,
                                    const double originX, const double originY, const double originZ,
                                    float *restrict const output, const sampling_region_t region,
                                    const double *restrict const shiftX, const double *restrict const shiftY,
                                    const double *restrict const shiftZ,
                                    const double scaleXz, const double scaleY, const float outputScale) {
    const uint32_t size = region.sizeX * region.sizeY * region.sizeZ;

    coord_iter_t it = math_coord_iter_begin(region);
    uint32_t i = 0;
    int32_t xs[16], ys[16], zs[16];

    if (shiftX && shiftY && shiftZ) {
        while (i < size) {
            const uint32_t n = math_coord_iter_next16(&it, xs, ys, zs, size - i);
            __builtin_assume(n >= 1 && n <= 16);

#pragma clang loop vectorize(enable)
            for (uint32_t j = 0; j < n; j++) {
                const double x = (double) xs[j] * scaleXz + shiftX[i + j];
                const double y = (double) ys[j] * scaleY + shiftY[i + j];
                const double z = (double) zs[j] * scaleXz + shiftZ[i + j];
                output[i + j] += math_noise_perlin_sample_base(permutations, originX, originY, originZ, x, y, z) *
                        outputScale;
            }

            i += n;
        }
    } else if (!shiftX && !shiftY && !shiftZ) {
        while (i < size) {
            const uint32_t n = math_coord_iter_next16(&it, xs, ys, zs, size - i);
            __builtin_assume(n >= 1 && n <= 16);

#pragma clang loop vectorize(enable)
            for (uint32_t j = 0; j < n; j++) {
                const double x = (double) xs[j] * scaleXz;
                const double y = (double) ys[j] * scaleY;
                const double z = (double) zs[j] * scaleXz;
                output[i + j] += math_noise_perlin_sample_base(permutations, originX, originY, originZ, x, y, z) *
                        outputScale;
            }

            i += n;
        }
    } else {
        __builtin_trap();
        __builtin_unreachable();
    }
}

static inline void
math_noise_perlin_sample_base_area(const uint32_t *restrict const permutations,
                                   const double originX, const double originY, const double originZ,
                                   float *restrict const output,
                                   const int32_t sizeX, const int32_t sizeY, const int32_t sizeZ,
                                   const int32_t minBlockX, const int32_t minBlockY, const int32_t minBlockZ,
                                   const int32_t stepBlockX, const int32_t stepBlockY, const int32_t stepBlockZ,
                                   const double *restrict const shiftX, const double *restrict const shiftY,
                                   const double *restrict const shiftZ,
                                   const double scaleXz, const double scaleY, const float outputScale) {
    sampling_region_t region = {
        .sizeX = sizeX,
        .sizeY = sizeY,
        .sizeZ = sizeZ,
        .minBlockX = minBlockX,
        .minBlockY = minBlockY,
        .minBlockZ = minBlockZ,
        .stepBlockX = stepBlockX,
        .stepBlockY = stepBlockY,
        .stepBlockZ = stepBlockZ,
    };
    math_noise_perlin_sample_base_area0(permutations, originX, originY, originZ, output, region, shiftX,
                                        shiftY, shiftZ, scaleXz, scaleY, outputScale);;
}

static inline __attribute__((const)) float
math_end_islands_sample(const uint32_t *restrict const simplex_permutations, const int32_t x, const int32_t z) {
    const int32_t i = x / 2;
    const int32_t j = z / 2;
    const int32_t k = x % 2;
    const int32_t l = z % 2;
    float f = -100.0F;

    int8_t ms[25 * 25], ns[25 * 25], hit[25 * 25];
    const int64_t omin = labs(i) - 12LL;
    const int64_t pmin = labs(j) - 12LL;
    const int64_t omax = labs(i) + 12LL;
    const int64_t pmax = labs(j) + 12LL;

    {
        uint32_t idx = 0;
#pragma clang loop vectorize(enable)
        for (int8_t m = -12; m < 13; m++) {
            for (int8_t n = -12; n < 13; n++) {
                ms[idx] = m;
                ns[idx] = n;
                idx++;
            }
        }
        if (idx != 25 * 25) {
            __builtin_trap();
        }
    }

    if (omin * omin + pmin * pmin > 4096LL) {
#pragma clang loop vectorize(enable) interleave_count(2)
        for (uint32_t idx = 0; idx < 25 * 25; idx++) {
            const int64_t o = (int64_t) i + (int64_t) ms[idx];
            const int64_t p = (int64_t) j + (int64_t) ns[idx];
            hit[idx] = math_noise_simplex_sample2d(simplex_permutations, (double) o, (double) p) < -0.9F;
        }
    } else {
        for (uint32_t idx = 0; idx < 25 * 25; idx++) {
            const int64_t o = (int64_t) i + (int64_t) ms[idx];
            const int64_t p = (int64_t) j + (int64_t) ns[idx];
            hit[idx] = (o * o + p * p > 4096LL) && math_noise_simplex_sample2d(
                    simplex_permutations, (double) o, (double) p) < -0.9F;
        }
    }

    for (uint32_t idx = 0; idx < 25 * 25; idx++) {
        if (hit[idx]) {
            const int32_t m = ms[idx];
            const int32_t n = ns[idx];
            const int64_t o = (int64_t) i + (int64_t) m;
            const int64_t p = (int64_t) j + (int64_t) n;
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

#pragma clang loop vectorize_width(4) interleave_count(2)
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

    uint32_t index = 12; // 12 max
    for (int32_t offY = 0; offY <= 2; ++offY) {
        int32_t gymul = gy * 12 + offY * 12;
        for (int32_t offZ = 0; offZ <= 1; ++offZ) {
            int32_t gzmul = (gz + offZ) << 4;

            uint32_t index0 = index - 1;
            uint32_t posIdx0 = math_aquifer_index(aquiferData, gx, gy + offY, gz + offZ);
            uint32_t position0 = packedBlockPositions[posIdx0];
            int32_t dx0 = (gx << 4) + math_aquifer_unpackPackedX(position0) - x;
            int32_t dy0 = gymul + math_aquifer_unpackPackedY(position0) - y;
            int32_t dz0 = gzmul + math_aquifer_unpackPackedZ(position0) - z;
            uint32_t dist_0 = dx0 * dx0 + dy0 * dy0 + dz0 * dz0;

            uint32_t index1 = index - 2;
            uint32_t posIdx1 = posIdx0 + 1;
            uint32_t position1 = packedBlockPositions[posIdx1];
            int32_t dx1 = ((gx + 1) << 4) + math_aquifer_unpackPackedX(position1) - x;
            int32_t dy1 = gymul + math_aquifer_unpackPackedY(position1) - y;
            int32_t dz1 = gzmul + math_aquifer_unpackPackedZ(position1) - z;
            uint32_t dist_1 = dx1 * dx1 + dy1 * dy1 + dz1 * dz1;

            ps[12 - index] = (dist_0 << 20) | (index0 << 16) | posIdx0;
            ps[13 - index] = (dist_1 << 20) | (index1 << 16) | posIdx1;

            index -= 2;
        }
    }

    A = ps[0];

    for (uint32_t i = 1; i < 12; i ++) {
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

// branch node: occupies two slots, first with node_minmacs, second with branch_children
// bit 31 set for both slots, bit 30 set for second slot
// leaf node: occupies one slot, with biome ID in state

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
        if (!__math_biome_search_tree_is_branch(node + 1) || !__math_biome_search_tree_is_branch_children(node + 1)) {
            // branch node must have children offsets in the next slot
            __builtin_trap();
        }
        if (!__math_biome_search_tree_is_branch(node + 1) && __math_biome_search_tree_is_branch_children(node + 1)) {
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

    for (uint32_t i = 0; i < 7; i ++) {
        int64_t l = (int32_t) target[i] - (int32_t) node->node_minmaxs.maxs[i];
        int64_t m = (int32_t) node->node_minmaxs.mins[i] - (int32_t) target[i];
        int64_t dist = l >= 0L ? l : max(m, 0L);
        res += dist * dist;
    }

    return res;
}

typedef struct __biome_search_stack_element {
    uint32_t node;
    uint8_t iter_i;
} __biome_search_stack_element_t;

static inline uint32_t __attribute__((pure))
math_biome_search_tree_calc(const biome_search_tree_node_t * restrict const nodes,
                            const int16_t * restrict const target,
                            const uint32_t nodes_c, const uint32_t tree_depth) {
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

    loop_start:
    while (top) {
        uint32_t cur_node = working[top - 1].node;
        uint32_t iter_i = working[top - 1].iter_i;
        __math_biome_search_tree_validate_node(nodes + cur_node);

        uint32_t child_node;
        if (iter_i >= 7 || !(child_node = nodes[cur_node + 1].branch_children.children_offset[iter_i])) {
            // no more children, pop the stack
            top --;
            continue;
        }

        // bump iter index for the current node
        working[top - 1].iter_i ++;

        __math_biome_search_tree_validate_node(nodes + child_node);

        uint64_t d = __math_biome_search_tree_distance_func(nodes + child_node, target);

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

#pragma clang attribute pop
