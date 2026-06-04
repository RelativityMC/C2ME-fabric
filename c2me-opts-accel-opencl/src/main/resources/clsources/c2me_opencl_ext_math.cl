/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

typedef char int8_t;
typedef unsigned char uint8_t;
typedef short int16_t;
typedef unsigned short uint16_t;
typedef int int32_t;
typedef unsigned int uint32_t;
typedef long int64_t;
typedef unsigned long uint64_t;

// #define DEBUG 1

#pragma OPENCL FP_CONTRACT OFF

#ifndef NULL
#define NULL (void *) 0L
#endif

#ifndef FUNC_NOINLINE
#define FUNC_NOINLINE __attribute__((noinline))
#endif

#ifndef FUNC_NOINLINE_MIDDF
#define FUNC_NOINLINE_MIDDF __attribute__((noinline))
#endif

#ifdef AVOID_TRAP
#define __builtin_trap()
#endif

#define UINT32_MAX 0xffffffffU
#define INT32_MAX 0x7fffffff
#define UINT64_MAX 0xffffffffffffffffLU
#define INT64_MAX 0x7fffffffffffffffLU

__attribute__((aligned(64))) static constant double FLAT_SIMPLEX_GRAD[16][3] = {
        {1, 1, 0},
        {-1, 1, 0},
        {1, -1, 0},
        {-1, -1, 0},
        {1, 0, 1},
        {-1, 0, 1},
        {1, 0, -1},
        {-1, 0, -1},
        {0, 1, 1},
        {0, -1, 1},
        {0, 1, -1},
        {0, -1, -1},
        {1, 1, 0},
        {0, -1, 1},
        {-1, 1, 0},
        {0, -1, -1},
};

static constant double SQRT_3 = 1.7320508075688772;
// 0.5 * (SQRT_3 - 1.0)
static constant double SKEW_FACTOR_2D = 0.3660254037844386;
// (3.0 - SQRT_3) / 6.0
static constant double UNSKEW_FACTOR_2D = 0.21132486540518713;

// Used in intel fast compile
#ifdef BLOAT_APPARENT_FUNCTION_SIZES
static void nop() {
}
#else
#define nop()
#endif

static __attribute__((const)) void *ptr_shift(const void * const ptr, const int32_t shift) {
    return (void *) (((uint8_t *) ptr) + shift);
}

static __attribute__((const)) constant void *ptr_shift_const(constant const void * const ptr, const int32_t shift) {
    return (constant void *) (((constant uint8_t *) ptr) + shift);
}

// static __attribute__((const)) local void *ptr_shift_local(local const void * const ptr, const int32_t shift) {
//     return (local void *) (((local uint8_t *) ptr) + shift);
// }

static __attribute__((const)) global void *ptr_shift_global(global const void * const ptr, const int32_t shift) {
    return (global void *) (((global uint8_t *) ptr) + shift);
}

static __attribute__((const)) double math_floor(const double v) {
    return floor(v);
}

static __attribute__((const)) uint64_t math_rotateLeftU64(uint64_t i, uint64_t distance) {
    return (i << distance) | (i >> -distance);
}

static __attribute__((const)) int32_t math_floorDiv(const int32_t x, const int32_t y) {
    int r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x)) {
        r--;
    }
    return r;
}

static __attribute__((const)) double math_octave_maintainPrecision(const double value) {
    return value - math_floor(value / 3.3554432E7 + 0.5) * 3.3554432E7;
}

static __attribute__((pure)) double math_simplex_grad(const int32_t hash, const double x, const double y,
                                                              const double z, const double distance) {
    double d = distance - x * x - y * y - z * z;
    if (d < 0.0) {
        return 0.0;
    } else {
        double var0 = FLAT_SIMPLEX_GRAD[hash][0] * x;
        double var1 = FLAT_SIMPLEX_GRAD[hash][1] * y;
        double var2 = FLAT_SIMPLEX_GRAD[hash][2] * z;
        return d * d * d * d * (var0 + var1 + var2);
    }
}

static __attribute__((const)) double math_lerp(const double delta, const double start, const double end) {
    return start + delta * (end - start);
}

static __attribute__((const)) float math_lerpf(const float delta, const float start, const float end) {
    return start + delta * (end - start);
}

static __attribute__((const)) double math_clampedLerp(const double start, const double end, const double delta) {
    if (delta < 0.0) {
        return start;
    } else {
        return delta > 1.0 ? end : math_lerp(delta, start, end);
    }
}

static __attribute__((const)) double math_square(const double operand) {
    return operand * operand;
}

static __attribute__((const)) double math_lerp2(const double deltaX, const double deltaY, const double x0y0,
                                                       const double x1y0, const double x0y1, const double x1y1) {
    return math_lerp(deltaY, math_lerp(deltaX, x0y0, x1y0), math_lerp(deltaX, x0y1, x1y1));
}

static __attribute__((const)) double math_lerp3(
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

static __attribute__((const)) double math_getLerpProgress(const double value, const double start,
                                                                 const double end) {
    return (value - start) / (end - start);
}

static __attribute__((const)) double
math_clampedLerpFromProgress(const double lerpValue, const double lerpStart, const double lerpEnd, const double start,
                             const double end) {
    return math_clampedLerp(start, end, math_getLerpProgress(lerpValue, lerpStart, lerpEnd));
}

static __attribute__((const)) int32_t math_floorMod(const int32_t x, const int32_t y) {
    int32_t mod = x % y;
    // if the signs are different and modulo not zero, adjust result
    if ((mod ^ y) < 0 && mod != 0) {
        mod += y;
    }
    return mod;
}

static __attribute__((const)) int32_t math_biome2block(const int32_t biomeCoord) {
    return biomeCoord << 2;
}

static __attribute__((const)) int32_t math_block2biome(const int32_t blockCoord) {
    return blockCoord >> 2;
}

static __attribute__((pure)) uint32_t
__math_simplex_map_global(global const uint32_t * restrict permutations, const int32_t input) {
    return permutations[input & 0xFF];
}

static __attribute__((pure)) double math_simplex_dot(const int32_t hash, const double x, const double y,
                                                     const double z) {
    return FLAT_SIMPLEX_GRAD[hash][0] * x + FLAT_SIMPLEX_GRAD[hash][1] * y + FLAT_SIMPLEX_GRAD[hash][2] * z;
}

static __attribute__((const)) double __math_simplex_grad(const int32_t hash, const double x, const double y,
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

static double __attribute__((pure))
math_noise_simplex_sample2d_global(global const uint32_t * restrict permutations, const double x, const double y) {
    const double d = (x + y) * SKEW_FACTOR_2D;
    const double i = math_floor(x + d);
    const double j = math_floor(y + d);
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
    const int32_t t = __math_simplex_map_global(permutations, r + __math_simplex_map_global(permutations, s)) % 12;
    const int32_t u = __math_simplex_map_global(permutations, r + li + __math_simplex_map_global(permutations, s + mi)) % 12;
    const int32_t v = __math_simplex_map_global(permutations, r + 1 + __math_simplex_map_global(permutations, s + 1)) % 12;
    const double w = __math_simplex_grad(t, h, k, 0.0, 0.5);
    const double z = __math_simplex_grad(u, n, o, 0.0, 0.5);
    const double aa = __math_simplex_grad(v, p, q, 0.0, 0.5);
    return 70.0 * (w + z + aa);
}

static __attribute__((const)) double math_perlinFade(const double value) {
    return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
}

// noinline to prevent broken optimizations on intel drivers
static __attribute__((pure)) __attribute__((noinline)) double __math_perlin_grad_global(global const uint8_t * restrict permutations, const int32_t px,
                                                                     const int32_t py, const int32_t pz, const double fx,
                                                                     const double fy, const double fz) {
    const uint32_t map0 = (((uint32_t) permutations[((uint32_t) px) & 0xFF]) + ((uint32_t) py));
    const uint32_t map1 = (((uint32_t) permutations[map0 & 0xFF]) + ((uint32_t) pz));
    const uint32_t hash = permutations[map1 & 0XFF] & 0xF;
    return FLAT_SIMPLEX_GRAD[hash][0] * fx + FLAT_SIMPLEX_GRAD[hash][1] * fy + FLAT_SIMPLEX_GRAD[hash][2] * fz;
}

static __attribute__((pure)) double
math_noise_perlin_sampleScalar_global(global const uint8_t * restrict permutations,
                                      const int32_t px0, const int32_t py0, const int32_t pz0,
                                      const double fx0, const double fy0, const double fz0, const double fadeLocalY) {
    const int32_t px1 = px0 + 1;
    const int32_t py1 = py0 + 1;
    const int32_t pz1 = pz0 + 1;
    const double fx1 = fx0 - 1;
    const double fy1 = fy0 - 1;
    const double fz1 = fz0 - 1;

    double f000 = __math_perlin_grad_global(permutations, px0, py0, pz0, fx0, fy0, fz0);
    double f100 = __math_perlin_grad_global(permutations, px1, py0, pz0, fx1, fy0, fz0);
    double f010 = __math_perlin_grad_global(permutations, px0, py1, pz0, fx0, fy1, fz0);
    double f110 = __math_perlin_grad_global(permutations, px1, py1, pz0, fx1, fy1, fz0);
    double f001 = __math_perlin_grad_global(permutations, px0, py0, pz1, fx0, fy0, fz1);
    double f101 = __math_perlin_grad_global(permutations, px1, py0, pz1, fx1, fy0, fz1);
    double f011 = __math_perlin_grad_global(permutations, px0, py1, pz1, fx0, fy1, fz1);
    double f111 = __math_perlin_grad_global(permutations, px1, py1, pz1, fx1, fy1, fz1);

    const double dx = math_perlinFade(fx0);
    const double dy = math_perlinFade(fadeLocalY);
    const double dz = math_perlinFade(fz0);
    return math_lerp3(dx, dy, dz, f000, f100, f010, f110, f001, f101, f011, f111);
}

static __attribute__((pure)) double
math_noise_perlin_sample_global(global const uint8_t * restrict permutations,
                                const double originX, const double originY, const double originZ,
                                const double x, const double y, const double z,
                                const double yScale, const double yMax) {
    const double d = x + originX;
    const double e = y + originY;
    const double f = z + originZ;
    const double i = math_floor(d);
    const double j = math_floor(e);
    const double k = math_floor(f);
    const double g = d - i;
    const double h = e - j;
    const double l = f - k;
    const double o = yScale != 0 ? math_floor(((yMax >= 0.0 && yMax < h) ? yMax : h) / yScale + 1.0E-7) * yScale : 0;

    return math_noise_perlin_sampleScalar_global(permutations, (int32_t) i, (int32_t) j, (int32_t) k, g, h - o, l, h);
}


typedef const struct double_octave_sampler_data {
    const uint64_t length;
    const double amplitude;
    const int32_t need_shift;
    const int32_t lacunarity_powd;
    const int32_t persistence_powd;
    const int32_t sampler_permutations;
    const int32_t sampler_originX;
    const int32_t sampler_originY;
    const int32_t sampler_originZ;
    const int32_t amplitudes;
} double_octave_sampler_data_t;

static __attribute__((pure)) double
math_noise_perlin_double_octave_sample_impl_global(global const double_octave_sampler_data_t * restrict const data,
                                                   const double x, const double y, const double z,
                                                   const double yScale, const double yMax, const uint8_t useOrigin) {
    double d1 = 0.0;
    double d2 = 0.0;

    global const bool *const need_shift = ptr_shift_global(data, data->need_shift);
    global const double *lacunarity_powd = ptr_shift_global(data, data->lacunarity_powd);
    global const double *persistence_powd = ptr_shift_global(data, data->persistence_powd);
    global const uint8_t *sampler_permutations = ptr_shift_global(data, data->sampler_permutations);
    global const double *sampler_originX = ptr_shift_global(data, data->sampler_originX);
    global const double *sampler_originY = ptr_shift_global(data, data->sampler_originY);
    global const double *sampler_originZ = ptr_shift_global(data, data->sampler_originZ);
    global const double *amplitudes = ptr_shift_global(data, data->amplitudes);

    for (uint32_t i = 0; i < data->length; i++) {
        const double e = lacunarity_powd[i];
        const double f = persistence_powd[i];
        global const uint8_t *permutations = sampler_permutations + 256 * i;
        const double sampleX = need_shift[i] ? x * 1.0181268882175227 : x;
        const double sampleY = need_shift[i] ? y * 1.0181268882175227 : y;
        const double sampleZ = need_shift[i] ? z * 1.0181268882175227 : z;
        const double g = math_noise_perlin_sample_global(
                permutations,
                sampler_originX[i],
                sampler_originY[i],
                sampler_originZ[i],
                math_octave_maintainPrecision(sampleX * e),
                useOrigin ? -(sampler_originY[i]) : math_octave_maintainPrecision(sampleY * e),
                math_octave_maintainPrecision(sampleZ * e),
                yScale * e,
                yMax * e);
        const double d = amplitudes[i] * g * f;
        if (!need_shift[i]) {
            d1 += d;
        } else {
            d2 += d;
        }
    }

    return (d1 + d2) * data->amplitude;
}

static FUNC_NOINLINE double
math_noise_perlin_double_octave_sample_global_noinline(global const double_octave_sampler_data_t * restrict const data,
                                                       const double x, const double y, const double z) {
    return math_noise_perlin_double_octave_sample_impl_global(data, x, y, z, 0.0, 0.0, 0);
}

static double
math_noise_perlin_double_octave_sample_global(global const double_octave_sampler_data_t * restrict const data,
                                              const double x, const double y, const double z) {
    return math_noise_perlin_double_octave_sample_impl_global(data, x, y, z, 0.0, 0.0, 0);
}

typedef const struct interpolated_noise_sub_sampler {
    const uint32_t length;
    const int32_t sampler_permutations;
    const int32_t sampler_originX;
    const int32_t sampler_originY;
    const int32_t sampler_originZ;
    const int32_t sampler_mulFactor;
} interpolated_noise_sub_sampler_t;

typedef const struct interpolated_noise_sampler {
    const double scaledXzScale;
    const double scaledYScale;
    const double xzFactor;
    const double yFactor;
    const double smearScaleMultiplier;
    const double xzScale;
    const double yScale;

    const interpolated_noise_sub_sampler_t lower;
    const interpolated_noise_sub_sampler_t upper;
    const interpolated_noise_sub_sampler_t normal;
} interpolated_noise_sampler_t;

static double
math_noise_perlin_interpolated_sample_global(global const interpolated_noise_sampler_t * restrict const data,
                                             const double x, const double y, const double z) {
    const double d = x * data->scaledXzScale;
    const double e = y * data->scaledYScale;
    const double f = z * data->scaledXzScale;
    const double g = d / data->xzFactor;
    const double h = e / data->yFactor;
    const double i = f / data->xzFactor;
    const double j = data->scaledYScale * data->smearScaleMultiplier;
    const double k = j / data->yFactor;
    double l = 0.0;
    double m = 0.0;
    double n = 0.0;

    for (uint32_t offset = 0; offset < data->normal.length; offset++) {
        global const uint8_t *sampler_permutations = ptr_shift_global(data, data->normal.sampler_permutations);
        global const double *sampler_originX = ptr_shift_global(data, data->normal.sampler_originX);
        global const double *sampler_originY = ptr_shift_global(data, data->normal.sampler_originY);
        global const double *sampler_originZ = ptr_shift_global(data, data->normal.sampler_originZ);
        global const double *sampler_mulFactor = ptr_shift_global(data, data->normal.sampler_mulFactor);
        n += math_noise_perlin_sample_global(
                sampler_permutations + 256 * offset,
                sampler_originX[offset],
                sampler_originY[offset],
                sampler_originZ[offset],
                math_octave_maintainPrecision(g * sampler_mulFactor[offset]),
                math_octave_maintainPrecision(h * sampler_mulFactor[offset]),
                math_octave_maintainPrecision(i * sampler_mulFactor[offset]),
                k * sampler_mulFactor[offset],
                h * sampler_mulFactor[offset]
        ) / sampler_mulFactor[offset];
    }

    const double q = (n / 10.0 + 1.0) / 2.0;
    const uint8_t bl2 = q >= 1.0;
    const uint8_t bl3 = q <= 0.0;

    if (!bl2) {
        for (uint32_t offset = 0; offset < data->lower.length; offset++) {
            global const uint8_t *sampler_permutations = ptr_shift_global(data, data->lower.sampler_permutations);
            global const double *sampler_originX = ptr_shift_global(data, data->lower.sampler_originX);
            global const double *sampler_originY = ptr_shift_global(data, data->lower.sampler_originY);
            global const double *sampler_originZ = ptr_shift_global(data, data->lower.sampler_originZ);
            global const double *sampler_mulFactor = ptr_shift_global(data, data->lower.sampler_mulFactor);
            l += math_noise_perlin_sample_global(
                    sampler_permutations + 256 * offset,
                    sampler_originX[offset],
                    sampler_originY[offset],
                    sampler_originZ[offset],
                    math_octave_maintainPrecision(d * sampler_mulFactor[offset]),
                    math_octave_maintainPrecision(e * sampler_mulFactor[offset]),
                    math_octave_maintainPrecision(f * sampler_mulFactor[offset]),
                    j * sampler_mulFactor[offset],
                    e * sampler_mulFactor[offset]
            ) / sampler_mulFactor[offset];
        }
    }

    if (!bl3) {
        for (uint32_t offset = 0; offset < data->upper.length; offset++) {
            global const uint8_t *sampler_permutations = ptr_shift_global(data, data->upper.sampler_permutations);
            global const double *sampler_originX = ptr_shift_global(data, data->upper.sampler_originX);
            global const double *sampler_originY = ptr_shift_global(data, data->upper.sampler_originY);
            global const double *sampler_originZ = ptr_shift_global(data, data->upper.sampler_originZ);
            global const double *sampler_mulFactor = ptr_shift_global(data, data->upper.sampler_mulFactor);
            m += math_noise_perlin_sample_global(
                    sampler_permutations + 256 * offset,
                    sampler_originX[offset],
                    sampler_originY[offset],
                    sampler_originZ[offset],
                    math_octave_maintainPrecision(d * sampler_mulFactor[offset]),
                    math_octave_maintainPrecision(e * sampler_mulFactor[offset]),
                    math_octave_maintainPrecision(f * sampler_mulFactor[offset]),
                    j * sampler_mulFactor[offset],
                    e * sampler_mulFactor[offset]
            ) / sampler_mulFactor[offset];
        }
    }

    return math_clampedLerp(l / 512.0, m / 512.0, q) / 128.0;
}

static FUNC_NOINLINE double
math_noise_perlin_interpolated_sample_global_noinline(global const interpolated_noise_sampler_t * restrict const data,
                                                      const double x, const double y, const double z) {
    return math_noise_perlin_interpolated_sample_global(data, x, y, z);
}

static __attribute__((pure)) float
math_end_islands_sample_global(global const uint32_t * restrict simplex_permutations, const int32_t x, const int32_t z) {
    const int32_t i = x / 2;
    const int32_t j = z / 2;
    const int32_t k = x % 2;
    const int32_t l = z % 2;
    volatile int32_t muld = x * x + z * z; // int32_t intentionally
    if (muld & 0x80000000L) {
        return nan((uint32_t) 0);
    }
    float f = 100.0F - sqrt((float) (muld & 0x7fffffffL)) * 8.0F;
    f = clamp(f, -100.0F, 80.0F);

    int8_t ms[25 * 25], ns[25 * 25], hit[25 * 25];
    const int64_t omin = abs(i) - 12L;
    const int64_t pmin = abs(j) - 12L;
    const int64_t omax = abs(i) + 12L;
    const int64_t pmax = abs(j) + 12L;

    {
        uint32_t idx = 0;
        for (int8_t m = -12; m < 13; m++) {
            for (int8_t n = -12; n < 13; n++) {
                ms[idx] = m;
                ns[idx] = n;
                idx++;
            }
        }
        if (idx != 25 * 25) {
            #ifdef DEBUG
            printf("trap: idx != 25 * 25\n idx=%u\n", idx);
            #endif
            __builtin_trap();
            __builtin_unreachable();
            return nan((uint64_t) 0);
        }
    }

    if (omin * omin + pmin * pmin > 4096L) {
        for (uint32_t idx = 0; idx < 25 * 25; idx++) {
            const int64_t o = (int64_t) i + (int64_t) ms[idx];
            const int64_t p = (int64_t) j + (int64_t) ns[idx];
            hit[idx] = math_noise_simplex_sample2d_global(simplex_permutations, (double) o, (double) p) < -0.9F;
        }
    } else {
        for (uint32_t idx = 0; idx < 25 * 25; idx++) {
            const int64_t o = (int64_t) i + (int64_t) ms[idx];
            const int64_t p = (int64_t) j + (int64_t) ns[idx];
            hit[idx] = (o * o + p * p > 4096L) && math_noise_simplex_sample2d_global(
                    simplex_permutations, (double) o, (double) p) < -0.9F;
        }
    }

    for (uint32_t idx = 0; idx < 25 * 25; idx++) {
        if (hit[idx]) {
            const int32_t m = ms[idx];
            const int32_t n = ns[idx];
            const int64_t o = (int64_t) i + (int64_t) m;
            const int64_t p = (int64_t) j + (int64_t) n;
            const float g1 = fabs((float) o) * 3439.0F;
            const float g2 = fabs((float) p) * 147.0F;
            const float g = fmod((g1 + g2), 13.0F) + 9.0F;
            const float h = (float) (k - m * 2);
            const float q = (float) (l - n * 2);
            float r = 100.0F - sqrt(h * h + q * q) * g;
            r = clamp(r, -100.0F, 80.0F);
            f = fmax(f, r);
        }
    }

    return f;
}
 
static __attribute__((const)) uint32_t
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
    int32_t sizeY;
    int32_t sizeZ;
    int32_t samplingYLowPassCutoff;

    int32_t randomDeriver;
    int32_t posIdx_len;
    int32_t waterLevels; // aquifer_fluidlevel_t[posIdx]
    int32_t packedBlockPositions; // short[posIdx]
} aquifer_data_t;

static __attribute__((pure)) uint32_t
math_aquifer_index_global(global const aquifer_data_t *restrict const aquiferData, const int32_t x, const int32_t y,
                          const int32_t z) {
    int i = x - aquiferData->startX;
    int j = y - aquiferData->startY;
    int k = z - aquiferData->startZ;
    if (i < 0 || j < 0 || k < 0 || i >= aquiferData->sizeX || j >= aquiferData->sizeY || k >= aquiferData->sizeZ) {
        #ifdef DEBUG
        printf("trap: i < 0 || j < 0 || k < 0 || i >= aquiferData->sizeX || j >= aquiferData->sizeY || k >= aquiferData->sizeZ\n i=%d j=%d k=%d aquiferData->sizeX=%d aquiferData->sizeY=%d aquiferData->sizeZ=%d\n", 
            i, j, k, aquiferData->sizeX, aquiferData->sizeY, aquiferData->sizeZ);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return 0;
    }
    return (j * aquiferData->sizeZ + k) * aquiferData->sizeX + i;
}

static __attribute__((const)) int32_t
math_aquifer_unpackPackedX(uint32_t packed) {
    return (packed >> 8) & 0b1111;
}

static __attribute__((const)) int32_t
math_aquifer_unpackPackedY(uint32_t packed) {
    return (packed >> 4) & 0b1111;
}

static __attribute__((const)) int32_t
math_aquifer_unpackPackedZ(uint32_t packed) {
    return packed & 0b1111;
}

static __attribute__((const)) int32_t
math_aquifer_unpackPackedDist(uint64_t packed) {
    return (int32_t) (packed >> 36UL);
}

static __attribute__((const)) int32_t
math_aquifer_unpackPackedPosIdx(uint64_t packed) {
    return (int32_t) (packed & 0xffffffffUL);
}

static void
math_aquifer_refreshDistPosIdx_global(global const uint16_t *restrict const packedBlockPositions, uint64_t *restrict const res,
                                      global const aquifer_data_t *restrict const aquiferData,
                                      const int32_t x, const int32_t y, const int32_t z) {
    int32_t gx = (x - 5) >> 4;
    int32_t gy = math_floorDiv(y + 1, 12) - 1;
    int32_t gz = (z - 5) >> 4;
    uint64_t A = UINT64_MAX;
    uint64_t B = UINT64_MAX;
    uint64_t C = UINT64_MAX;
    uint64_t D = UINT64_MAX;

    uint64_t ps[12];

    uint64_t index = 12; // 12 max
    for (int32_t offY = 0; offY <= 2; ++offY) {
        int32_t gymul = gy * 12 + offY * 12;
        for (int32_t offZ = 0; offZ <= 1; ++offZ) {
            int32_t gzmul = (gz + offZ) << 4;

            uint64_t index0 = index - 1;
            uint32_t posIdx0 = math_aquifer_index_global(aquiferData, gx, gy + offY, gz + offZ);
            uint32_t position0 = packedBlockPositions[posIdx0];
            int32_t dx0 = (gx << 4) + math_aquifer_unpackPackedX(position0) - x;
            int32_t dy0 = gymul + math_aquifer_unpackPackedY(position0) - y;
            int32_t dz0 = gzmul + math_aquifer_unpackPackedZ(position0) - z;
            uint64_t dist_0 = (uint64_t) (dx0 * dx0 + dy0 * dy0 + dz0 * dz0);

            uint64_t index1 = index - 2;
            uint32_t posIdx1 = posIdx0 + 1;
            uint32_t position1 = packedBlockPositions[posIdx1];
            int32_t dx1 = ((gx + 1) << 4) + math_aquifer_unpackPackedX(position1) - x;
            int32_t dy1 = gymul + math_aquifer_unpackPackedY(position1) - y;
            int32_t dz1 = gzmul + math_aquifer_unpackPackedZ(position1) - z;
            uint64_t dist_1 = (uint64_t) (dx1 * dx1 + dy1 * dy1 + dz1 * dz1);

            ps[12 - index] = (dist_0 << 36UL) | (index0 << 32UL) | ((uint64_t) posIdx0);
            ps[13 - index] = (dist_1 << 36UL) | (index1 << 32UL) | ((uint64_t) posIdx1);

            index -= 2;
        }
    }

    A = ps[0];
    for (uint32_t i = 1; i < 12; i ++) {
        uint64_t p1 = ps[i];
        if (p1 <= C) {
            uint64_t n11 = max(A, p1);
            A = min(A, p1);

            uint64_t n12 = max(B, n11);
            B = min(B, n11);

            uint64_t n13 = max(C, n12);
            C = min(C, n12);

            D = min(D, n13);
        }
    }

    res[0] = A;
    res[1] = B;
    res[2] = C;
    res[3] = D;
}

constant const uint32_t MASK_isInterpolation = 1 << 0;
constant const uint32_t MASK_inInterpolationLoop = 1 << 1;
constant const uint32_t MASK_interpolationEnableCache2D = 1 << 2;

static __attribute__((pure)) global void *df_data_offset_global(global const void * const root, const int32_t index) {
    int32_t offset = ((global int32_t *) ptr_shift_global(root, 128))[index];
    return offset ? ptr_shift_global(root, offset) : NULL;
}

static __attribute__((const)) double math_clampedMap(const double value, const double oldStart, const double oldEnd, const double newStart, const double newEnd) {
    return math_clampedLerp(newStart, newEnd, math_getLerpProgress(value, oldStart, oldEnd));
}

static __attribute__((const)) int32_t math_roundDownToMultiple(const double a, const int32_t b) {
    return ((int32_t) math_floor(a / (double)b)) * b;
}

extern constant const int32_t genShapeCfg_minimumY;
extern constant const int32_t genShapeCfg_height;
extern constant const uint32_t genShapeCfg_horizontalSize;
extern constant const uint32_t genShapeCfg_verticalSize;

static __attribute__((const)) uint32_t genShapeCfg_verticalCellBlockCount() {
    return math_biome2block(genShapeCfg_verticalSize);
}

static __attribute__((const)) uint32_t genShapeCfg_horizontalCellBlockCount() {
    return math_biome2block(genShapeCfg_horizontalSize);
}

typedef struct worldgen_params {
    // cache size is (size + 1) to account for interpolation
    int32_t startBiomeX;
    int32_t startBiomeZ;
    int32_t sizeBiomeX;
    int32_t sizeBiomeZ;

    // cache size is (size + 1) to account for interpolation
    int32_t startCellX;
    int32_t startCellY;
    int32_t startCellZ;
    int32_t sizeCellX;
    int32_t sizeCellY;
    int32_t sizeCellZ;

    // cache size is actually size
    int32_t estimateSurfaceHeight_startBiomeX;
    int32_t estimateSurfaceHeight_startBiomeZ;
    int32_t estimateSurfaceHeight_sizeBiomeX;
    int32_t estimateSurfaceHeight_sizeBiomeZ;

    // cache size is actually size
    int32_t cache2d_startX;
    int32_t cache2d_startZ;
    int32_t cache2d_sizeX;
    int32_t cache2d_sizeZ;

    int32_t offset_estimateSurfaceHeight;
    int32_t genConfig_defaultBlock;
    int32_t genConfig_defaultFluid; // see aquifer code for blockstate defs
    int32_t offset_aquifer;
    int32_t offset_fluidLevelSampler; // aquifer_fluidlevel_t[] minimumY -> height
    int32_t offset_oreVeinRandom;
} worldgen_params_t;

typedef struct interpolation_pos {
    int32_t cellRelX;
    int32_t cellRelY;
    int32_t cellRelZ;
    int32_t cellBlockX;
    int32_t cellBlockY;
    int32_t cellBlockZ;
} interpolation_pos_t;

static interpolation_pos_t df_get_interpolation_pos(global const worldgen_params_t * restrict params, const int32_t x, const int32_t y, const int32_t z) {
    int32_t cellRelX = math_floorDiv(x, genShapeCfg_horizontalCellBlockCount()) - params->startCellX;
    int32_t cellRelY = math_floorDiv(y, genShapeCfg_verticalCellBlockCount()) - params->startCellY;
    int32_t cellRelZ = math_floorDiv(z, genShapeCfg_horizontalCellBlockCount()) - params->startCellZ;
    int32_t cellBlockX = math_floorMod(x, genShapeCfg_horizontalCellBlockCount());
    int32_t cellBlockY = math_floorMod(y, genShapeCfg_verticalCellBlockCount());
    int32_t cellBlockZ = math_floorMod(z, genShapeCfg_horizontalCellBlockCount());
    if (cellRelX < 0 || cellRelY < 0 || cellRelZ < 0 || cellRelX >= params->sizeCellX || cellRelY >= params->sizeCellY || cellRelZ >= params->sizeCellY || cellBlockX < 0 || cellBlockY < 0 || cellBlockZ < 0) {
        #ifdef DEBUG
        printf("trap: cellRelX < 0 || cellRelY < 0 || cellRelZ < 0 || cellRelX >= params->sizeCellX || cellRelY >= params->sizeCellY || cellRelZ >= params->sizeCellY || cellBlockX < 0 || cellBlockY < 0 || cellBlockZ < 0\n x=%d y=%d z=%d params->sizeCellX=%d params->sizeCellY=%d params->sizeCellZ=%d genShapeCfg_horizontalCellBlockCount()=%d genShapeCfg_verticalCellBlockCount()=%d params->startCellX=%d params->startCellY=%d params->startCellZ=%d\n", x, y, z, params->sizeCellX, params->sizeCellY, params->sizeCellZ, genShapeCfg_horizontalCellBlockCount(), genShapeCfg_verticalCellBlockCount(), params->startCellX, params->startCellY, params->startCellZ);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return (interpolation_pos_t) {};
    }
    return (interpolation_pos_t) {
        .cellRelX = cellRelX,
        .cellRelY = cellRelY,
        .cellRelZ = cellRelZ,
        .cellBlockX = cellBlockX,
        .cellBlockY = cellBlockY,
        .cellBlockZ = cellBlockZ,
    };
}

static uint32_t df_address_flatcache_buffer(global const worldgen_params_t * restrict params, const uint32_t cacheIndex, const uint32_t offsetX, const uint32_t offsetZ) {
    if (offsetX > params->sizeBiomeX || offsetZ > params->sizeBiomeZ) {
        #ifdef DEBUG
        printf("trap: offsetX > params->sizeBiomeX || offsetZ > params->sizeBiomeZ\n offsetX=%d offsetZ=%d params->sizeBiomeX=%d params->sizeBiomeZ=%d\n", offsetX, offsetZ, params->sizeBiomeX, params->sizeBiomeZ);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return 0;
    }
    return ((cacheIndex) * (params->sizeBiomeX + 1) + offsetX) * (params->sizeBiomeZ + 1) + offsetZ;
}

static uint32_t df_address_cache2d_buffer(global const worldgen_params_t * restrict params, const uint32_t cacheIndex, const uint32_t offsetX, const uint32_t offsetZ) {
    if (offsetX >= params->cache2d_sizeX || offsetZ >= params->cache2d_sizeZ) {
        #ifdef DEBUG
        printf("trap: offsetX >= params->cache2d_sizeX || offsetZ >= params->cache2d_sizeZ\n offsetX=%d offsetZ=%d params->cache2d_sizeX=%d params->cache2d_sizeZ=%d\n", offsetX, offsetZ, params->cache2d_sizeX, params->cache2d_sizeZ);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return 0;
    }
    return ((cacheIndex) * (params->cache2d_sizeX) + offsetX) * (params->cache2d_sizeZ) + offsetZ;
}

static uint32_t df_address_interpolator_buffer(global const worldgen_params_t * restrict params, const uint32_t cacheIndex, const int32_t cellX, const int32_t cellY, const int32_t cellZ) {
    if (cellX < 0 || cellY < 0 || cellZ < 0 || cellX > params->sizeCellX || cellY > params->sizeCellY || cellZ > params->sizeCellZ) {
        #ifdef DEBUG
        printf("trap: cellX < 0 || cellY < 0 || cellZ < 0 || cellX > params->sizeCellX || cellY > params->sizeCellY || cellZ > params->sizeCellZ\n cellX=%d cellY=%d cellZ=%d params->sizeCellX=%d params->sizeCellY=%d params->sizeCellZ=%d\n", cellX, cellY, cellZ, params->sizeCellX, params->sizeCellY, params->sizeCellZ);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return 0;
    }
    return ((((cacheIndex) * (params->sizeCellX + 1) + cellX) * (params->sizeCellY + 1) + cellY) * (params->sizeCellZ + 1) + cellZ);
}

typedef struct cache_result {
    bool cached;
    double res;
} cache_result_t;

static cache_result_t df_cachelike_interpolator(global const worldgen_params_t * restrict params, global const double * restrict interpolator_buffer, const uint32_t cacheIndex, const int32_t x, const int32_t y, const int32_t z, const uint32_t interpolationState) {
    if (!params || !(interpolationState & MASK_inInterpolationLoop) || !(interpolationState & MASK_isInterpolation)) {
        return (cache_result_t) { .cached = false, .res = nan((uint64_t) 0) };
    }
    // if (!params_local->isSamplingForCaches) {
    //     *res = data->result;
    //     return true;
    // }
    const interpolation_pos_t pos = df_get_interpolation_pos(params, x, y, z);
    const double res = math_lerp3(
        (double) pos.cellBlockX / (double) genShapeCfg_horizontalCellBlockCount(),
        (double) pos.cellBlockY / (double) genShapeCfg_verticalCellBlockCount(),
        (double) pos.cellBlockZ / (double) genShapeCfg_horizontalCellBlockCount(),
        // data->x0y0z0,
        // data->x1y0z0,
        // data->x0y1z0,
        // data->x1y1z0,
        // data->x0y0z1,
        // data->x1y0z1,
        // data->x0y1z1,
        // data->x1y1z1
        interpolator_buffer[df_address_interpolator_buffer(params, cacheIndex, pos.cellRelX, pos.cellRelY, pos.cellRelZ)],
        interpolator_buffer[df_address_interpolator_buffer(params, cacheIndex, pos.cellRelX + 1, pos.cellRelY, pos.cellRelZ)],
        interpolator_buffer[df_address_interpolator_buffer(params, cacheIndex, pos.cellRelX, pos.cellRelY + 1, pos.cellRelZ)],
        interpolator_buffer[df_address_interpolator_buffer(params, cacheIndex, pos.cellRelX + 1, pos.cellRelY + 1, pos.cellRelZ)],
        interpolator_buffer[df_address_interpolator_buffer(params, cacheIndex, pos.cellRelX, pos.cellRelY, pos.cellRelZ + 1)],
        interpolator_buffer[df_address_interpolator_buffer(params, cacheIndex, pos.cellRelX + 1, pos.cellRelY, pos.cellRelZ + 1)],
        interpolator_buffer[df_address_interpolator_buffer(params, cacheIndex, pos.cellRelX, pos.cellRelY + 1, pos.cellRelZ + 1)],
        interpolator_buffer[df_address_interpolator_buffer(params, cacheIndex, pos.cellRelX + 1, pos.cellRelY + 1, pos.cellRelZ + 1)]
    );
    return (cache_result_t) { .cached = true, .res = res };
}

static cache_result_t df_cachelike_flatcache(global const worldgen_params_t * restrict params, global const double * restrict data, const uint32_t cacheIndex, const int32_t x, const int32_t y, const int32_t z) {
    if (!params) {
        return (cache_result_t) { .cached = false, .res = nan((uint64_t) 0) };
    }
    const int32_t offsetX = math_block2biome(x) - params->startBiomeX;
    const int32_t offsetZ = math_block2biome(z) - params->startBiomeZ;
    if (offsetX >= 0 && offsetZ >= 0 && offsetX <= params->sizeBiomeX && offsetZ <= params->sizeBiomeZ) {
        const double res = data[df_address_flatcache_buffer(params, cacheIndex, offsetX, offsetZ)];
        return (cache_result_t) { .cached = true, .res = res };
    } else {
        return (cache_result_t) { .cached = false, .res = nan((uint64_t) 0) };
    }
}

static cache_result_t df_cachelike_cache2d(global const worldgen_params_t * restrict params, global const double * restrict data, const uint32_t cacheIndex, const int32_t x, const int32_t y, const int32_t z, const uint32_t interpolationState) {
    if (!params || !(interpolationState & MASK_inInterpolationLoop) || !(interpolationState & MASK_isInterpolation) || !(interpolationState & MASK_interpolationEnableCache2D)) {
        return (cache_result_t) { .cached = false, .res = nan((uint64_t) 0) };
    }
    const int32_t offsetX = x - params->cache2d_startX;
    const int32_t offsetZ = z - params->cache2d_startZ;
    if (offsetX >= 0 && offsetZ >= 0 && offsetX < params->cache2d_sizeX && offsetZ < params->cache2d_sizeZ) {
        const double res = data[df_address_cache2d_buffer(params, cacheIndex, offsetX, offsetZ)];
        return (cache_result_t) { .cached = true, .res = res };
    } else {
        return (cache_result_t) { .cached = false, .res = nan((uint64_t) 0) };
    }
}

static __attribute__((const)) double df_caveScaler_scaleCaves(const double value) {
    if (value < -0.75) {
        return 0.5;
    } else if (value < -0.5) {
        return 0.75;
    } else if (value < 0.5) {
        return 1.0;
    } else {
        return value < 0.75 ? 2.0 : 3.0;
    }
}

static __attribute__((const)) double df_caveScaler_scaleTunnels(const double value) {
    if (value < -0.5) {
        return 0.75;
    } else if (value < 0.0) {
        return 1.0;
    } else {
        return value < 0.5 ? 1.5 : 2.0;
    }
}

static __attribute__((pure)) int32_t df_spline_findRangeForLocation(const float * const locations, const uint32_t locations_len, const float x) {
    int32_t min = 0;
    int32_t i = locations_len;

    while (i > 0) {
        int32_t j = i / 2;
        int32_t k = min + j;
        if (x < locations[k]) {
            i = j;
        } else {
            min = k + 1;
            i -= j + 1;
        }
    }

    return min - 1;
}

static __attribute__((pure)) int32_t df_spline_findRangeForLocation_const(constant const float * const locations, const uint32_t locations_len, const float x) {
    int32_t min = 0;
    int32_t i = locations_len;

    while (i > 0) {
        int32_t j = i / 2;
        int32_t k = min + j;
        if (x < locations[k]) {
            i = j;
        } else {
            min = k + 1;
            i -= j + 1;
        }
    }

    return min - 1;
}

static __attribute__((pure)) float df_spline_sampleOutsideRange(const float point, const float * const locations, const float value, const float * const derivatives, const int i) {
    float f = derivatives[i];
    return f == 0.0F ? value : value + f * (point - locations[i]);
}

static __attribute__((pure)) float df_spline_sampleOutsideRange_const(const float point, constant const float * const locations, const float value, constant const float * const derivatives, const int i) {
    float f = derivatives[i];
    return f == 0.0F ? value : value + f * (point - locations[i]);
}

// StructureWeightSampler
static constant const int32_t SWSTA_NONE = 0;
static constant const int32_t SWSTA_BURY = 1;
static constant const int32_t SWSTA_BEARD_THIN = 2;
static constant const int32_t SWSTA_BEARD_BOX = 3;
static constant const int32_t SWSTA_ENCAPSULATE = 4;

typedef struct sws_index {
    // chunk pos
    const int32_t startX;
    const int32_t startZ;
    const uint32_t sizeX;
    const uint32_t sizeZ;
} sws_index_t;

typedef struct sws_data {
    const uint32_t pieceLength;
    const int32_t boxStartX;
    const int32_t boxStartY;
    const int32_t boxStartZ;
    const int32_t boxEndX;
    const int32_t boxEndY;
    const int32_t boxEndZ;
    const int32_t groundLevelDelta;
    const int32_t terrainAdjustment;

    const uint32_t funcLength;
    const int32_t sourceX;
    const int32_t sourceGroundY;
    const int32_t sourceZ;

    const int32_t affectedBox_startX;
    const int32_t affectedBox_startY;
    const int32_t affectedBox_startZ;
    const int32_t affectedBox_endX;
    const int32_t affectedBox_endY;
    const int32_t affectedBox_endZ;
} sws_data_t;

static __attribute__((const)) double __df_structureWeightSampler_getMagnitudeWeight(const double x, const double y, const double z) {
    double d = sqrt(x * x + y * y + z * z);
    if (d > 6.0) {
        return 0.0;
    } else {
        return 1.0 - d / 6.0;
    }
}

static __attribute__((const)) double math_fastInverseSqrt(double a) {
    union {
        double d;
        uint64_t l;
    } x;
    x.d = a;
    double d = 0.5 * x.d;
    x.l = 6910469410427058090L - (x.l >> 1);
    return x.d * (1.5 - d * x.d * x.d);
}

static __attribute__((pure)) double __df_structureWeightSampler_getStructureWeight(global const float * restrict const structureWeightSamplerTable, const double x, const double y, const double z, const double yy) {
    int32_t i = x + 12;
    int32_t j = y + 12;
    int32_t k = z + 12;
    if (i >= 0 && i < 24 && j >= 0 && j < 24 && k >= 0 && k < 24) {
        double d = (double)yy + 0.5;
        // double e = MathHelper.squaredMagnitude((double)x, d, (double)z);
        double e = ((double) x * (double) x) + (d * d) + ((double) z * (double) z);
        double f = -d * math_fastInverseSqrt(e / 2.0) / 2.0;
        return f * (double)structureWeightSamplerTable[k * 24 * 24 + i * 24 + j];
    } else {
        return 0.0;
    }
}

static __attribute__((pure)) double df_structureWeightSampler_sample(global const float * restrict const structureWeightSamplerTable, global const sws_index_t * restrict const data_index, const int32_t x, const int32_t y, const int32_t z) {
    const int32_t chunkX = x >> 4;
    const int32_t chunkZ = z >> 4;
    const uint32_t dataRelX = (uint32_t) clamp(chunkX - data_index->startX, 0, (int32_t) data_index->sizeX - 1);
    const uint32_t dataRelZ = (uint32_t) clamp(chunkZ - data_index->startZ, 0, (int32_t) data_index->sizeZ - 1);
    global const uint32_t * restrict const sws_data_offsets = ptr_shift_global(data_index, sizeof(sws_index_t));
    const uint32_t sws_current_offset = sws_data_offsets[dataRelX * data_index->sizeZ + dataRelZ];

    if (!sws_current_offset) return 0.0;

    global const sws_data_t * restrict const data = ptr_shift_global(data_index, sws_data_offsets[dataRelX * data_index->sizeZ + dataRelZ]);

    global const int32_t * restrict const boxStartX = ptr_shift_global(data, data->boxStartX);
    global const int32_t * restrict const boxStartY = ptr_shift_global(data, data->boxStartY);
    global const int32_t * restrict const boxStartZ = ptr_shift_global(data, data->boxStartZ);
    global const int32_t * restrict const boxEndX = ptr_shift_global(data, data->boxEndX);
    global const int32_t * restrict const boxEndY = ptr_shift_global(data, data->boxEndY);
    global const int32_t * restrict const boxEndZ = ptr_shift_global(data, data->boxEndZ);
    global const int32_t * restrict const groundLevelDelta = ptr_shift_global(data, data->groundLevelDelta);
    global const int32_t * restrict const terrainAdjustment = ptr_shift_global(data, data->terrainAdjustment);

    global const int32_t * restrict const sourceX = ptr_shift_global(data, data->sourceX);
    global const int32_t * restrict const sourceGroundY = ptr_shift_global(data, data->sourceGroundY);
    global const int32_t * restrict const sourceZ = ptr_shift_global(data, data->sourceZ);

    if (x < data->affectedBox_startX || x > data->affectedBox_endX ||
        y < data->affectedBox_startY || y > data->affectedBox_endY ||
        z < data->affectedBox_startZ || z > data->affectedBox_endZ) {
        return 0.0;
    }

    double d = 0.0;

    for (uint32_t i = 0; i < data->pieceLength; i ++) {
        int32_t m = max(0, max(boxStartX[i] - x, x - boxEndX[i]));
        int32_t n = max(0, max(boxStartZ[i] - z, z - boxEndZ[i]));
        int32_t o = boxStartY[i] + groundLevelDelta[i];
        int32_t p = y - o;

        switch (terrainAdjustment[i]) {
            case SWSTA_NONE:
                d += 0.0;
                break;
            case SWSTA_BURY:
                d += __df_structureWeightSampler_getMagnitudeWeight(m, (double)p / 2.0, n);
                break;
            case SWSTA_BEARD_THIN:
                d += __df_structureWeightSampler_getStructureWeight(structureWeightSamplerTable, m, p, n, p) * 0.8;
                break;
            case SWSTA_BEARD_BOX:
                d += __df_structureWeightSampler_getStructureWeight(structureWeightSamplerTable, m, max(0, max(o - y, y - boxEndY[i])), n, p) * 0.8;
                break;
            case SWSTA_ENCAPSULATE:
                d += __df_structureWeightSampler_getMagnitudeWeight((double)m / 2.0, (double)max(0, max(boxStartY[i] - y, y - boxEndY[i])) / 2.0, (double)n / 2.0) * 0.8;
                break;
            default:
                #ifdef DEBUG
                printf("trap: unexpected terrainAdjustment[i]=%d, i=%u\n", terrainAdjustment[i], i);
                #endif
                __builtin_trap();
                __builtin_unreachable();
                return nan((uint32_t) 0);
        };
    }

    for (uint32_t i = 0; i < data->funcLength; i ++) {
        int r = x - sourceX[i];
        int l = y - sourceGroundY[i];
        int m = z - sourceZ[i];
        d += __df_structureWeightSampler_getStructureWeight(structureWeightSamplerTable, r, l, m, l) * 0.4;
    }

    return d;
}

typedef struct sample_int32_ctx {
    global const void * restrict const const_data;
    global void * restrict const rw_data;
    const int32_t x, y, z;
    const uint32_t sample_flags;
} sample_int32_ctx_t;

static sample_int32_ctx_t make_sample_int32_ctx(global const void * restrict const const_data, global void * restrict const rw_data, const int32_t x, const int32_t y, const int32_t z, const uint32_t sample_flags) {
    return (sample_int32_ctx_t) {
        .const_data = const_data,
        .rw_data = rw_data,
        .x = x,
        .y = y,
        .z = z,
        .sample_flags = sample_flags
    };
}

#ifndef DEBUG
#define df_cachelike_trap_printf(desc, ctx)
#else
#define df_cachelike_trap_printf(desc, ctx) printf("trap: accessing cachelike \"%s\" beyond cache boundary\n ctx.xyz=(%d, %d, %d) ctx.sample_flags=%u\n", desc, ctx.x, ctx.y, ctx.z, ctx.sample_flags)
#endif

#define df_binding_def(name) static __attribute__((pure)) double df_binding_##name(const sample_int32_ctx_t ctx)

df_binding_def(barrier);
df_binding_def(fluid_level_floodedness);
df_binding_def(fluid_level_spread);
df_binding_def(lava);
df_binding_def(temperature);
df_binding_def(vegetation);
df_binding_def(continents);
df_binding_def(erosion);
df_binding_def(depth);
df_binding_def(ridges);
df_binding_def(preliminary_surface_level);
df_binding_def(final_density);
df_binding_def(vein_toggle);
df_binding_def(vein_ridged);
df_binding_def(vein_gap);
df_binding_def(final_final_density);

#undef df_binding_def

static __attribute__((pure)) int32_t chunkNoiseSampler_estimateSurfaceHeight0(global const void * restrict const const_data, global void * restrict const rw_data, const int32_t blockX, const int32_t blockZ) {
    return math_floor(df_binding_preliminary_surface_level(make_sample_int32_ctx(const_data, rw_data, blockX, 0, blockZ, 0)));
}

// constant const int32_t CACHE_CHUNK_RADIUS_estimateSurfaceHeight = 4;
// constant const int32_t CACHE_SIZE_estimateSurfaceHeight = (CACHE_CHUNK_RADIUS_estimateSurfaceHeight * 2 + 1) << 2;

static __attribute__((pure)) int32_t chunkNoiseSampler_estimateSurfaceHeight(global const void * restrict const const_data, global const void * restrict const rw_data, const int32_t blockX, const int32_t blockZ) {
    global const worldgen_params_t *params = rw_data;
    global const int32_t *cache = ptr_shift_global(rw_data, params->offset_estimateSurfaceHeight);
    int32_t biomeX = math_block2biome(blockX);
    int32_t biomeZ = math_block2biome(blockZ);
    int32_t relX = biomeX - params->estimateSurfaceHeight_startBiomeX;
    int32_t relZ = biomeZ - params->estimateSurfaceHeight_startBiomeZ;
    if (!cache || relX < 0 || relZ < 0 || relX >= params->estimateSurfaceHeight_sizeBiomeX || relZ >= params->estimateSurfaceHeight_sizeBiomeZ) {
        // // SLOW PATH
        // printf("SLOW PATH\n");
        // return chunkNoiseSampler_estimateSurfaceHeight0(const_data, math_biome2block(biomeX), math_biome2block(biomeZ));
        #ifdef DEBUG
        printf("trap: accessing uncached region for estimateSurfaceHeight\n hasCache=%d blockPos=(%d, %d), cacheStartBiomeX=%d cacheStartBiomeZ=%d cacheSizeBiomeX=%d cacheSizeBiomeZ=%d\n", (cache ? 1 : 0), blockX, blockZ, params->estimateSurfaceHeight_startBiomeX, params->estimateSurfaceHeight_startBiomeZ, params->estimateSurfaceHeight_sizeBiomeX, params->estimateSurfaceHeight_sizeBiomeZ);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return nan((uint64_t) 0L);
    } else {
        return cache[relX * params->estimateSurfaceHeight_sizeBiomeZ + relZ];
    }
}

#ifdef DF_COMPILE_ESTIMATE_SURFACE_HEIGHT
kernel __attribute__((reqd_work_group_size(8, 8, 1))) void chunkNoiseSampler_estimateSurfaceHeight_prefill_indep(global const void * restrict const const_data, global void * restrict const rw_data,
                                                                                                                 global int32_t * restrict const cache,
                                                                                                                 const int32_t startChunkX, const int32_t startChunkZ, const uint32_t cacheWidth) {
    if (!const_data || !cache || !rw_data) {
        #ifdef DEBUG
        printf("trap: !const_data || !cache || !rw_data\n const_data=%p cache=%p rw_data=%p\n", const_data, cache, rw_data);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return;
    }

    int32_t relX = get_global_id(0);
    int32_t relZ = get_global_id(1);
    int32_t biomeX = (startChunkX << 2) + relX;
    int32_t biomeZ = (startChunkZ << 2) + relZ;
    int32_t blockX = math_biome2block(biomeX);
    int32_t blockZ = math_biome2block(biomeZ);
    cache[relX * cacheWidth + relZ] = chunkNoiseSampler_estimateSurfaceHeight0(const_data, rw_data, blockX, blockZ);
}
#endif

static constant const uint64_t RANDOM_Checked = 0;
static constant const uint64_t RANDOM_Xoroshiro128PlusPlus = 1;
 
typedef struct random_state {
    uint64_t type; // see constants above
    uint64_t seedLo;
    uint64_t seedHi;
} random_state_t;

static __attribute__((const)) int64_t math_hashCode_int32x3(int32_t x, int32_t y, int32_t z) {
    int64_t l = (int64_t)(x * 3129871) ^ (int64_t)z * 116129781L ^ (int64_t)y;
    l = l * l * 42317861L + l * 11L;
    return l >> 16;
}

static __attribute__((const)) uint64_t math_mixStafford13(uint64_t seed) {
    seed = ((int64_t) (seed ^ seed >> 30)) * -4658895280553007687L;
    seed = ((int64_t) (seed ^ seed >> 27)) * -7723592293110705685L;
    return seed ^ seed >> 31;
}

static void random_state_set_seed(random_state_t *state, int64_t seed) {
    if (state->type == RANDOM_Checked) {
        state->seedLo = (seed ^ 25214903917L) & 281474976710655L;
    } else if (state->type == RANDOM_Xoroshiro128PlusPlus) {
        state->seedLo = seed ^ 7640891576956012809L;
        state->seedHi = ((int64_t) state->seedLo) + -7046029254386353131L;
        state->seedLo = math_mixStafford13(state->seedLo);
        state->seedHi = math_mixStafford13(state->seedHi);
    } else {
        #ifdef DEBUG
        printf("trap: random_state_set_seed: unexpected random type %lu\n", state->type);
        #endif
        __builtin_trap();
        __builtin_unreachable();
    }
}

static void random_state_split_coords(random_state_t *state, int32_t x, int32_t y, int32_t z) {
    if (state->type == RANDOM_Checked) {
        int64_t l = math_hashCode_int32x3(x, y, z);
        state->seedLo ^= l;
        random_state_set_seed(state, state->seedLo);
    } else if (state->type == RANDOM_Xoroshiro128PlusPlus) {
        int64_t l = math_hashCode_int32x3(x, y, z);
        state->seedLo ^= l;
        if ((state->seedLo | state->seedHi) == 0L) {
            state->seedLo = -7046029254386353131L;
            state->seedHi = 7640891576956012809L;
        }
    } else {
        #ifdef DEBUG
        printf("trap: random_state_split_coords: unexpected random type %lu\n", state->type);
        #endif
        __builtin_trap();
        __builtin_unreachable();
    }
}

static int32_t random_state_Checked_next(random_state_t *state, int32_t bits) {
    if (state->type != RANDOM_Checked) {
        #ifdef DEBUG
        printf("trap: random_state_Checked_next: unexpected random type %lu\n", state->type);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return 0;
    }

    int32_t m = state->seedLo * 25214903917L + 11L & 281474976710655L;
    state->seedLo = m;
    return (int32_t) (m >> (48 - bits));
}

static int64_t random_state_Xoroshiro128PlusPlus_next0(random_state_t *state) {
    if (state->type != RANDOM_Xoroshiro128PlusPlus) {
        #ifdef DEBUG
        printf("trap: random_state_Xoroshiro128PlusPlus_next0: unexpected random type %lu\n", state->type);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return 0;
    }

    int64_t l = state->seedLo;
    int64_t m = state->seedHi;
    int64_t n = math_rotateLeftU64((uint64_t) (l + m), 17) + l;
    m ^= l;
    state->seedLo = math_rotateLeftU64((uint64_t) l, 49) ^ m ^ m << 21;
    state->seedHi = math_rotateLeftU64((uint64_t) m, 28);
    return n;
}

static int64_t random_state_Xoroshiro128PlusPlus_next(random_state_t *state, int32_t bits) {
    return ((uint64_t) random_state_Xoroshiro128PlusPlus_next0(state)) >> (64 - bits);
}

static float random_state_nextFloat(random_state_t *state) {
    if (state->type == RANDOM_Checked) {
        return (float) random_state_Checked_next(state, 24) * 5.9604645E-8F;
    } else if (state->type == RANDOM_Xoroshiro128PlusPlus) {
        return (float) random_state_Xoroshiro128PlusPlus_next(state, 24) * 5.9604645E-8F;
    } else {
        #ifdef DEBUG
        printf("trap: random_state_nextFloat: unexpected random type %lu\n", state->type);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return nan((uint32_t) 0);
    }
}

static int32_t random_state_nextIntBounded(random_state_t *state, int32_t bound) {
    if (state->type == RANDOM_Checked) {
        if (bound <= 0) {
            #ifdef DEBUG
            printf("trap: random_state_nextIntBounded RANDOM_Checked: bound <= 0\n bound=%d\n", bound);
            #endif
            __builtin_trap();
            __builtin_unreachable();
            return 0;
        } else if ((bound & bound - 1) == 0) {
            return (int32_t)((int64_t)bound * (int64_t)random_state_Checked_next(state, 31) >> 31);
        } else {
            int32_t i;
            int32_t j;
            do {
                i = random_state_Checked_next(state, 31);
                j = i % bound;
            } while (i - j + (bound - 1) < 0);

            if (j >= bound) {
                #ifdef DEBUG
                printf("trap: random_state_nextIntBounded RANDOM_Checked ret >= bound\n ret=%d bound=%d\n", j, bound);
                #endif
                __builtin_trap();
                __builtin_unreachable();
                return 0;
            }
            return j;
        }
    } else if (state->type == RANDOM_Xoroshiro128PlusPlus) {
        if (bound <= 0) {
            #ifdef DEBUG
            printf("trap: random_state_nextIntBounded RANDOM_Xoroshiro128PlusPlus: bound <= 0\n bound=%d\n", bound);
            #endif
            __builtin_trap();
            __builtin_unreachable();
            return 0;
        } else {
            int64_t l = (uint32_t) random_state_Xoroshiro128PlusPlus_next0(state);
            int64_t m = l * (int64_t)bound;
            int64_t n = m & 4294967295L;
            if (n < (int64_t)bound) {
                for (int32_t i = ((uint32_t) ~bound + 1) % ((uint32_t) bound); n < (int64_t)i; n = m & 4294967295L) {
                    l = (uint32_t) random_state_Xoroshiro128PlusPlus_next0(state);
                    m = l * (int64_t)bound;
                }
            }

            int64_t o = m >> 32;
            int32_t ret = (int32_t) o;
            if (ret >= bound) {
                #ifdef DEBUG
                printf("trap: random_state_nextIntBounded RANDOM_Checked ret >= bound\n ret=%d bound=%d\n", ret, bound);
                #endif
                __builtin_trap();
                __builtin_unreachable();
                return 0;
            }
            return (int32_t)o;
        }
    } else {
        #ifdef DEBUG
        printf("trap: random_state_nextIntBounded: unexpected random type %lu\n", state->type);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return 0;
    }
}

static constant const int32_t BLOCK_NULL = 0;
static constant const int32_t BLOCK_AIR = 1;
static constant const int32_t BLOCK_DEFAULT_BLOCK = 2;
static constant const int32_t BLOCK_WATER = 3;
static constant const int32_t BLOCK_LAVA = 4;
static constant const int32_t BLOCK_COPPER_ORE = 5;
static constant const int32_t BLOCK_RAW_COPPER_BLOCK = 6;
static constant const int32_t BLOCK_GRANITE = 7;
static constant const int32_t BLOCK_DEEPSLATE_IRON_ORE = 8;
static constant const int32_t BLOCK_RAW_IRON_BLOCK = 9;
static constant const int32_t BLOCK_TUFF = 10;

typedef struct aquifer_fluidlevel {
    int32_t y;
    int32_t blockState;
} aquifer_fluidlevel_t;

// static void dbg_checkBlockState(int32_t blockState) {
//     if (blockState < 0 || blockState > 10) {
//         #ifdef DEBUG
//         printf("trap: dbg_checkBlockState: unexpected block state %d\n", blockState);
//         #endif
//         __builtin_trap();
//         __builtin_unreachable();
//     }
// }

static __attribute__((pure)) int32_t aquifer_fluidlevel_getBlockState_ptr_global(global const aquifer_fluidlevel_t *data, const int32_t y) {
    return y < data->y ? data->blockState : BLOCK_AIR;
}

static __attribute__((pure)) int32_t aquifer_fluidlevel_equals_global(global const aquifer_fluidlevel_t *data0, global const aquifer_fluidlevel_t *data1) {
    return data0->y == data1->y && data0->blockState == data1->blockState;
}
static constant const int32_t __aquifer_chunkPosOffset[13][2] = {
    {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
};

static __attribute__((pure)) const global aquifer_fluidlevel_t *fluidLevelSampler_getFluidLevel_ptr(global const void * restrict const rw_data, const int32_t y) {
    global const worldgen_params_t *params = rw_data;
    global const aquifer_fluidlevel_t *fluidLevels = ptr_shift_global(rw_data, params->offset_fluidLevelSampler);
    const int32_t relY = y - genShapeCfg_minimumY;
    return &fluidLevels[clamp(relY, 0, genShapeCfg_height - 1)];
}

static __attribute__((pure)) bool math_VanillaBiomeParameters_inDeepDarkParameters(const sample_int32_ctx_t ctx) {
    return df_binding_erosion(ctx) < -0.225F && df_binding_depth(ctx) > 0.9F;
}

static __attribute__((pure)) int32_t __aquifer_getNoiseBasedFluidLevel(global const void * restrict const const_data, int32_t blockX, int32_t blockY, int32_t blockZ, int32_t surfaceHeightEstimate) {
    int32_t i = 16;
    int32_t j = 40;
    int32_t k = blockX >> 4;
    int32_t l = math_floorDiv(blockY, 40);
    int32_t m = blockZ >> 4;
    int32_t n = l * 40 + 20;
    int32_t o = 10;
    double d = df_binding_fluid_level_spread(make_sample_int32_ctx(const_data, NULL, k, l, m, 0)) * 10.0;
    int32_t p = math_roundDownToMultiple(d, 3);
    int32_t q = n + p;
    return min(surfaceHeightEstimate, q);
}

constant const int32_t DimensionType_field_35479 = -32512; // copied from debugger

static __attribute__((pure)) int32_t __aquifer_getFluidBlockY(global const void * restrict const const_data, int32_t blockX, int32_t blockY, int32_t blockZ, global const aquifer_fluidlevel_t *defaultFluidLevel, int32_t surfaceHeightEstimate, bool bl) {
    // DensityFunction.UnblendedNoisePos unblendedNoisePos = new DensityFunction.UnblendedNoisePos(blockX, blockY, blockZ);
    const sample_int32_ctx_t unblendedNoisePos = make_sample_int32_ctx(const_data, NULL, blockX, blockY, blockZ, 0);
    double d;
    double e;
    if (math_VanillaBiomeParameters_inDeepDarkParameters(unblendedNoisePos)) {
        d = -1.0;
        e = -1.0;
    } else {
        int i = surfaceHeightEstimate + 8 - blockY;
        double f = bl ? math_clampedLerp(1.0, 0.0, ((double) i) / 64.0) : 0.0; // inline
        double g = clamp(df_binding_fluid_level_floodedness(unblendedNoisePos), -1.0, 1.0);
        d = g + 0.8 + (f - 1.0) * 1.2; // inline
        e = g + 0.3 + (f - 1.0) * 1.1; // inline
    }

    int i;
    if (e > 0.0) {
        i = defaultFluidLevel->y;
    } else if (d > 0.0) {
        i = __aquifer_getNoiseBasedFluidLevel(const_data, blockX, blockY, blockZ, surfaceHeightEstimate);
    } else {
        i = DimensionType_field_35479;
    }

    return i;
}

static __attribute__((pure)) int32_t __aquifer_getFluidBlockState(global const void * restrict const const_data, int blockX, int blockY, int blockZ, global const aquifer_fluidlevel_t *defaultFluidLevel, int fluidLevel) {
    int32_t blockState = defaultFluidLevel->blockState;
    if (fluidLevel <= -10 && fluidLevel != DimensionType_field_35479 && defaultFluidLevel->blockState != BLOCK_LAVA) {
        int i = 64;
        int j = 40;
        int k = blockX >> 6;
        int l = math_floorDiv(blockY, 40);
        int m = blockZ >> 6;
        double d = df_binding_lava(make_sample_int32_ctx(const_data, NULL, k, l, m, 0));
        if (fabs(d) > 0.3) {
            blockState = BLOCK_LAVA;
        }
    }

    return blockState;
}

#ifdef DF_COMPILE_AQUIFER_PREFILL
// launch with aquifer sizeX sizeY sizeZ
kernel void aquifer_data_prefill(global const void * restrict const const_data, global const void * restrict const rw_data) {
    if (!const_data || !rw_data) {
        #ifdef DEBUG
        printf("trap: !const_data || !rw_data\n const_data=%p rw_data=%p\n", const_data, rw_data);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return;
    }

    global const worldgen_params_t *params = rw_data;

    if (!params->offset_aquifer) {
        #ifdef DEBUG
        printf("trap: no aquifer configured\n");
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return;
    }

    global const aquifer_data_t *data = ptr_shift_global(rw_data, params->offset_aquifer);
    global const random_state_t *randomDeriver = ptr_shift_global(data, data->randomDeriver);
    
    global aquifer_fluidlevel_t *waterLevels = ptr_shift_global(data, data->waterLevels);
    global uint16_t *packedBlockPositions = ptr_shift_global(data, data->packedBlockPositions);

    const int32_t curX = data->startX + get_global_id(0);
    const int32_t curY = data->startY + get_global_id(2);
    const int32_t curZ = data->startZ + get_global_id(1);

    // fill packedBlockPositions
    random_state_t derived = *randomDeriver;
    random_state_split_coords(&derived, curX, curY, curZ);
    int32_t r0 = random_state_nextIntBounded(&derived, 10);
    int32_t r1 = random_state_nextIntBounded(&derived, 9);
    int32_t r2 = random_state_nextIntBounded(&derived, 10);
    int32_t blockX = curX * 16 + r0;
    int32_t blockY = curY * 12 + r1;
    int32_t blockZ = curZ * 16 + r2;
    int32_t index = math_aquifer_index_global(data, curX, curY, curZ);
    packedBlockPositions[index] = (uint16_t) ((r0 << 8) | (r1 << 4) | r2);

    int i = INT32_MAX;
    int j = blockY + 12;
    int k = blockY - 12;
    bool bl = false;

    // direct port
    global const aquifer_fluidlevel_t *fluidLevel = fluidLevelSampler_getFluidLevel_ptr(rw_data, blockY);
    for (uint32_t __i = 0; __i < 13; __i ++) { // 13 comes from __aquifer_chunkPosOffset
        const int32_t offX = __aquifer_chunkPosOffset[__i][0];
        const int32_t offZ = __aquifer_chunkPosOffset[__i][1];
        int32_t l = blockX + (offX << 4);
        int32_t m = blockZ + (offZ << 4);
        int32_t n = chunkNoiseSampler_estimateSurfaceHeight(const_data, rw_data, l, m);
        int32_t o = n + 8;
        bool bl2 = offX == 0 && offZ == 0;
        if (bl2 && k > o) {
            waterLevels[index] = *fluidLevel;
            return;
        }

        bool bl3 = j > o;
        if (bl3 || bl2) {
            global const aquifer_fluidlevel_t *fluidLevel2 = fluidLevelSampler_getFluidLevel_ptr(rw_data, o);
            if (aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel2, o) != BLOCK_AIR) {
                if (bl2) {
                    bl = true;
                }

                if (bl3) {
                    waterLevels[index] = *fluidLevel2;
                    return;
                }
            }
        }

        i = min(i, n);
    }

    int32_t p = __aquifer_getFluidBlockY(const_data, blockX, blockY, blockZ, fluidLevel, i, bl);
    aquifer_fluidlevel_t res;
    res.y = p;
    res.blockState = __aquifer_getFluidBlockState(const_data, blockX, blockY, blockZ, fluidLevel, p);
    waterLevels[index] = res;
}
#endif

typedef struct aquifer_result {
    int32_t blockState;
    bool needsFluidTick;
} aquifer_result_t;

static __attribute__((const)) double math_aquifer_maxDistance(int i, int a) {
    double d = 25.0;
    return 1.0 - (double)abs(a - i) / 25.0;
}

static constant const double aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD = -0x1.851eb851eb852p-1; // = maxDistance(MathHelper.square(10), MathHelper.square(12)) = -0.76

static __attribute__((const)) double __aquifer_getQ(const double i, const double d, const double j) {
    double e = i + 0.5 - d;
    double f = j / 2.0;
    double o = f - fabs(e);
    double q;
    if (e > 0.0) {
        if (o > 0.0) {
            q = o / 1.5;
        } else {
            q = o / 2.5;
        }
    } else {
        double p = 3.0 + o;
        if (p > 0.0) {
            q = p / 3.0;
        } else {
            q = p / 10.0;
        }
    }
    return q;
}

static double __aquifer_postCalculateDensityModified(const sample_int32_ctx_t ctx, const double q, double *mutableDoubleThingy) {
    double r;
    if (!(q < -2.0) && !(q > 2.0)) {
        double s = *mutableDoubleThingy;
        if (isnan(s)) {
            double t = df_binding_barrier(ctx);
            *mutableDoubleThingy = t;
            r = t;
        } else {
            r = s;
        }
    } else {
        r = 0.0;
    }

    return 2.0 * (r + q);
}

static double __aquifer_calculateDensityModified(const sample_int32_ctx_t ctx, global const aquifer_fluidlevel_t *fluidLevel, global const aquifer_fluidlevel_t *fluidLevel2, double *mutableDoubleThingy) {
    int32_t i = ctx.y;
    int32_t blockState = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel, i);
    int32_t blockState2 = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel2, i);
    if ((blockState != BLOCK_LAVA || blockState2 != BLOCK_WATER) && (blockState != BLOCK_WATER || blockState2 != BLOCK_LAVA)) {
        int32_t j = abs(fluidLevel->y - fluidLevel2->y);
        if (j == 0) {
            return 0.0;
        } else {
            double d = 0.5 * (double)(fluidLevel->y + fluidLevel2->y);
            const double q = __aquifer_getQ(i, d, j);

            return __aquifer_postCalculateDensityModified(ctx, q, mutableDoubleThingy);
        }
    } else {
        return 2.0;
    }
}

static bool __aquifer_extractedCheckFG(const sample_int32_ctx_t ctx,
                                       const double density, const double d, global const aquifer_fluidlevel_t *fluidLevel2, const double f, global const aquifer_fluidlevel_t *fluidLevel4, double *mutableDoubleThingy) {
    if (f > 0.0) {
        double g = d * f * __aquifer_calculateDensityModified(ctx, fluidLevel2, fluidLevel4, mutableDoubleThingy);
        if (density + g > 0.0) {
            // this.needsFluidTick = false;
            return true;
        }
    }
    return false;
}

static aquifer_result_t __aquifer_getFinalBlockState(const sample_int32_ctx_t ctx,
                                                     const double density, const double d, global const aquifer_fluidlevel_t *fluidLevel2, global const aquifer_fluidlevel_t *fluidLevel3, 
                                                     const int32_t blockState, const uint64_t *packedRes, double *mutableDoubleThingy) {
    global const worldgen_params_t *params = ctx.rw_data;
    global aquifer_data_t *data = ptr_shift_global(ctx.rw_data, params->offset_aquifer);
    global const aquifer_fluidlevel_t *waterLevels = ptr_shift_global(data, data->waterLevels);

    global const aquifer_fluidlevel_t *fluidLevel4 = &waterLevels[math_aquifer_unpackPackedPosIdx(packedRes[2])];
    // dbg_checkBlockState(fluidLevel4->blockState);
    int dist1 = math_aquifer_unpackPackedDist(packedRes[0]);
    int dist2 = math_aquifer_unpackPackedDist(packedRes[1]);
    int dist3 = math_aquifer_unpackPackedDist(packedRes[2]);
    int dist4 = math_aquifer_unpackPackedDist(packedRes[3]);
    double f = math_aquifer_maxDistance(dist1, dist3);

    aquifer_result_t nullResult;
    nullResult.blockState = BLOCK_NULL;
    nullResult.needsFluidTick = false;

    if (__aquifer_extractedCheckFG(ctx, density, d, fluidLevel2, f, fluidLevel4, mutableDoubleThingy)) return nullResult;

    double h = math_aquifer_maxDistance(dist2, dist3);
    if (__aquifer_extractedCheckFG(ctx, density, d, fluidLevel3, h, fluidLevel4, mutableDoubleThingy)) return nullResult;

    bool needsFluidTick = false;

    bool bl = !aquifer_fluidlevel_equals_global(fluidLevel2, fluidLevel3);
    bool bl2 = h >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD && !aquifer_fluidlevel_equals_global(fluidLevel3, fluidLevel4);
    bool bl3 = f >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD && !aquifer_fluidlevel_equals_global(fluidLevel2, fluidLevel4);
    if (!bl && !bl2 && !bl3) {
        needsFluidTick = f >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD
            && math_aquifer_maxDistance(dist1, dist4) >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD
            && !aquifer_fluidlevel_equals_global(fluidLevel2, &waterLevels[math_aquifer_unpackPackedPosIdx(packedRes[3])]);
    } else {
        needsFluidTick = true;
    }

    return (aquifer_result_t) {
        .blockState = blockState,
        .needsFluidTick = needsFluidTick,
    };
}

static aquifer_result_t __aquifer_applyPost(const sample_int32_ctx_t ctx, const double density, const int32_t j, const int32_t i, const int32_t k, const uint64_t *packedRes) {
    global const worldgen_params_t *params = ctx.rw_data;
    global aquifer_data_t *data = ptr_shift_global(ctx.rw_data, params->offset_aquifer);
    global const aquifer_fluidlevel_t *waterLevels = ptr_shift_global(data, data->waterLevels);

    global const aquifer_fluidlevel_t *fluidLevel2 = &waterLevels[math_aquifer_unpackPackedPosIdx(packedRes[0])];
    double d = math_aquifer_maxDistance(math_aquifer_unpackPackedDist(packedRes[0]), math_aquifer_unpackPackedDist(packedRes[1]));
    int32_t blockState = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel2, j);
    if (d <= 0.0) {
        bool needsFluidTick = false;
        if (d >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD) {
            global const aquifer_fluidlevel_t *fluidLevel3 = &waterLevels[math_aquifer_unpackPackedPosIdx(packedRes[1])];
            needsFluidTick = !aquifer_fluidlevel_equals_global(fluidLevel2, fluidLevel3);
        } else {
            needsFluidTick = false;
        }
        return (aquifer_result_t) {
            .blockState = blockState,
            .needsFluidTick = needsFluidTick,
        };
    } else if (blockState == BLOCK_WATER && aquifer_fluidlevel_getBlockState_ptr_global(fluidLevelSampler_getFluidLevel_ptr(ctx.rw_data, j - 1), j - 1) == BLOCK_LAVA) {
        return (aquifer_result_t) {
            .blockState = blockState,
            .needsFluidTick = true,
        };
    } else {
        double mutableDoubleThingy = nan((uint64_t) 0);
        global const aquifer_fluidlevel_t *fluidLevel3 = &waterLevels[math_aquifer_unpackPackedPosIdx(packedRes[1])];
        double e = d * __aquifer_calculateDensityModified(ctx, fluidLevel2, fluidLevel3, &mutableDoubleThingy);
        if (density + e > 0.0) {
            return (aquifer_result_t) {
                .blockState = BLOCK_NULL,
                .needsFluidTick = false,
            };
        } else {
            return __aquifer_getFinalBlockState(ctx, density, d, fluidLevel2, fluidLevel3, blockState, packedRes, &mutableDoubleThingy);
        }
    }
}

static aquifer_result_t aquifer_sample(const sample_int32_ctx_t ctx, const double density) {
    global const worldgen_params_t *params = ctx.rw_data;

    if (!params->offset_aquifer) {
        if (density > 0.0) {
            return (aquifer_result_t) {
                .blockState = BLOCK_NULL,
                .needsFluidTick = false
            };
        } else {
            global const aquifer_fluidlevel_t *fluidLevel = fluidLevelSampler_getFluidLevel_ptr(ctx.rw_data, ctx.y);
            return (aquifer_result_t) {
                .blockState = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel, ctx.y),
                .needsFluidTick = false,
            };
        }
    }

    global aquifer_data_t *aquifer_data = ptr_shift_global(ctx.rw_data, params->offset_aquifer);

    int32_t i = ctx.x;
    int32_t j = ctx.y;
    int32_t k = ctx.z;

    if (density > 0.0) {
        return (aquifer_result_t) {
            .blockState = BLOCK_NULL,
            .needsFluidTick = false
        };
    } else {
        global const aquifer_fluidlevel_t *fluidLevel = fluidLevelSampler_getFluidLevel_ptr(ctx.rw_data, j);
        if (j > aquifer_data->samplingYLowPassCutoff) {
            return (aquifer_result_t) {
                .blockState = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel, j),
                .needsFluidTick = false
            };
        }
        // if (fluidLevel.getBlockState(j).isOf(Blocks.LAVA)) {
        if (aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel, j) == BLOCK_LAVA) {
            return (aquifer_result_t) {
                .blockState = BLOCK_LAVA,
                .needsFluidTick = false
            };
        } else {
            global const uint16_t *packedBlockPositions = ptr_shift_global(aquifer_data, aquifer_data->packedBlockPositions);
            uint64_t packedRes[4];
            math_aquifer_refreshDistPosIdx_global(packedBlockPositions, packedRes, aquifer_data, i, j, k);
            return __aquifer_applyPost(ctx, density, j, i, k, packedRes);
        }
    }
}

typedef struct vein_type {
    int32_t ore;
    int32_t rawOreBlock;
    int32_t stone;
    int32_t minY;
    int32_t maxY;
} vein_type_t;

static constant const vein_type_t VEIN_COPPER = {
    .ore = BLOCK_COPPER_ORE,
    .rawOreBlock = BLOCK_RAW_COPPER_BLOCK,
    .stone = BLOCK_GRANITE,
    .minY = 0,
    .maxY = 50
};

static constant const vein_type_t VEIN_IRON = {
    .ore = BLOCK_DEEPSLATE_IRON_ORE,
    .rawOreBlock = BLOCK_RAW_IRON_BLOCK,
    .stone = BLOCK_TUFF,
    .minY = -60,
    .maxY = -8
};

static int32_t ore_vein_sample(const sample_int32_ctx_t ctx) {
    global const worldgen_params_t *params = ctx.rw_data;
    if (!params->offset_oreVeinRandom) return BLOCK_NULL;
    global const random_state_t *veinRandom = ptr_shift_global(ctx.rw_data, params->offset_oreVeinRandom);

    double d = df_binding_vein_toggle(ctx);
    constant const vein_type_t *veinType = d > 0.0 ? &VEIN_COPPER : &VEIN_IRON;
    double e = fabs(d);
    int32_t j = veinType->maxY - ctx.y;
    int32_t k = ctx.y - veinType->minY;
    if (k >= 0 && j >= 0) {
        int32_t l = min(j, k);
        double f = math_clampedMap((double)l, 0.0, 20.0, -0.2, 0.0);
        if (e + f < 0.4F) {
            return BLOCK_NULL;
        } else {
            random_state_t randomState = *veinRandom;
            random_state_split_coords(&randomState, ctx.x, ctx.y, ctx.z);
            if (random_state_nextFloat(&randomState) > 0.7F) {
                return BLOCK_NULL;
            } else if (df_binding_vein_ridged(ctx) >= 0.0) {
                return BLOCK_NULL;
            } else {
                double g = math_clampedMap(e, 0.4F, 0.6F, 0.1F, 0.3F);
                if ((double) random_state_nextFloat(&randomState) < g && df_binding_vein_gap(ctx) > -0.3F) {
                    return random_state_nextFloat(&randomState) < 0.02F ? veinType->rawOreBlock : veinType->ore;
                } else {
                    return veinType->stone;
                }
            }
        }
    } else {
        return BLOCK_NULL;
    }
}

#ifdef DF_COMPILE_NOISE_KERNEL
// res_blocks: [relY][relZ][relX], sign bit incdicate needsFluidTick
__attribute__((reqd_work_group_size(16, 16, 1)))
kernel void df_noise_kernel(global const void * restrict const const_data, global void * restrict const rw_data, global uint8_t *res_blocks,
                            const int32_t chunkX, const int32_t chunkZ) {
    if (!const_data || !rw_data || !res_blocks) {
        #ifdef DEBUG
        printf("trap: !const_data || !rw_data || !res_blocks\n const_data=%p rw_data=%p res_blocks=%p\n", const_data, rw_data, res_blocks);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return;
    }

    global const worldgen_params_t *params = rw_data;
    const int32_t sizeX = get_global_size(0);
    const int32_t sizeY = get_global_size(2);
    const int32_t sizeZ = get_global_size(1);
    const int32_t relX = get_global_id(0);
    const int32_t relY = get_global_id(2);
    const int32_t relZ = get_global_id(1);

    const int32_t blockX = (chunkX << 4) + relX;
    const int32_t blockY = genShapeCfg_minimumY + relY;
    const int32_t blockZ = (chunkZ << 4) + relZ;

    sample_int32_ctx_t ctx = make_sample_int32_ctx(const_data, rw_data, blockX, blockY, blockZ, MASK_isInterpolation | MASK_inInterpolationLoop | MASK_interpolationEnableCache2D);

    int32_t blockState = BLOCK_NULL;
    aquifer_result_t aquifer_res = aquifer_sample(ctx, df_binding_final_final_density(ctx));
    blockState = aquifer_res.blockState;
    if (blockState == BLOCK_NULL) {
        blockState = ore_vein_sample(ctx);
    }
    if (blockState == BLOCK_NULL) {
        blockState = params->genConfig_defaultBlock;
    }
    uint32_t idx = ((relY) * sizeX + relZ) * sizeZ + relX;
    res_blocks[idx] = ((uint8_t) blockState) | (aquifer_res.needsFluidTick ? (1U << 7) : 0);
}
#endif

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

static bool __attribute__((pure))
__math_biome_search_tree_is_branch(global const biome_search_tree_node_t * restrict const node) {
    return (node->state & (1U << 31)) != 0;
}

static bool __attribute__((pure))
__math_biome_search_tree_is_branch_children(global const biome_search_tree_node_t * restrict const node) {
    return (node->state & (1U << 30)) != 0;
}

static void
__math_biome_search_tree_validate_node(global const biome_search_tree_node_t * restrict const node) {
    if (!__math_biome_search_tree_is_branch(node) && __math_biome_search_tree_is_branch_children(node)) {
        // invalid state
        #ifdef DEBUG
        printf("trap: potential biome search tree corruption (__math_biome_search_tree_validate_node, 1)\n");
        #endif
        __builtin_trap();
        __builtin_unreachable();
    }
    if (__math_biome_search_tree_is_branch(node)) {
        if (!__math_biome_search_tree_is_branch(node + 1) || !__math_biome_search_tree_is_branch_children(node + 1)) {
            // branch node must have children offsets in the next slot
            #ifdef DEBUG
            printf("trap: potential biome search tree corruption (__math_biome_search_tree_validate_node, 2)\n");
            #endif
            __builtin_trap();
            __builtin_unreachable();
        }
        if (!__math_biome_search_tree_is_branch(node + 1) && __math_biome_search_tree_is_branch_children(node + 1)) {
            // branch node children offsets must be in a branch node
            #ifdef DEBUG
            printf("trap: potential biome search tree corruption (__math_biome_search_tree_validate_node, 3)\n");
            #endif
            __builtin_trap();
            __builtin_unreachable();
        }
    }
}

static uint64_t __attribute__((pure))
__math_biome_search_tree_distance_func(global const biome_search_tree_node_t * restrict const node,
                                       const int16_t * restrict const target) {
    if (__math_biome_search_tree_is_branch_children(node)) {
        #ifdef DEBUG
        printf("trap: potential biome search tree corruption (__math_biome_search_tree_distance_func, 1)\n");
        #endif
        __builtin_trap();
        __builtin_unreachable();
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

static uint32_t __attribute__((pure))
math_biome_search_tree_calc(global const biome_search_tree_node_t * restrict const nodes,
                            const int16_t * restrict const target,
                            const uint32_t nodes_c) {
    // no recursion allowed, because this needs to be eventually ported to GPU

    if (!__math_biome_search_tree_is_branch(nodes + 1)) {
        return nodes[1].state & 0x3FFFFFFF;
    }

    __biome_search_stack_element_t working[BIOME_SEARCH_TREE_MAX_DEPTH];
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
            if (top >= BIOME_SEARCH_TREE_MAX_DEPTH) {
                // stack overflow, this should never happen
                #ifdef DEBUG
                printf("trap: biome search stack overflow: top >= BIOME_SEARCH_TREE_MAX_DEPTH\n BIOME_SEARCH_TREE_MAX_DEPTH=%u\n", BIOME_SEARCH_TREE_MAX_DEPTH);
                #endif
                __builtin_trap();
                __builtin_unreachable();
            }
        } else {
            current_optimal_dist = d;
            current_optimal_node = child_node;
        }
    }

    return nodes[current_optimal_node].state & 0x3FFFFFFF;
}

extern constant const uint32_t biome_multinoise_tree_offset;
extern constant const uint32_t biome_multinoise_tree_nodes_c;

#ifdef DF_COMPILE_BIOME_MULTINOISE_KERNEL
// res_blocks: [relY][relZ][relX]
__attribute__((reqd_work_group_size(8, 8, 1)))
kernel void df_biome_multinoise_kernel(global const void * restrict const const_data, global void * restrict const rw_data,
                                       global uint32_t * restrict const res_biomes,
                                       const int32_t startBiomeX, const int32_t startBiomeZ, const int32_t startBiomeY,
                                       const uint32_t sizeX, const uint32_t sizeZ, const uint32_t sizeY) {
    if (!biome_multinoise_tree_offset) {
        #ifdef DEBUG
        printf("trap: no multinoise configured\n");
        #endif
        __builtin_trap();
        __builtin_unreachable();
    }

    if (!const_data || !res_biomes || !rw_data) {
        #ifdef DEBUG
        printf("trap: !const_data || !res_biomes || !rw_data\n const_data=%p res_biomes=%p rw_data=%p\n", const_data, res_biomes, rw_data);
        #endif
        __builtin_trap();
        __builtin_unreachable();
        return;
    }

    const uint32_t relX = get_global_id(0);
    const uint32_t relZ = get_global_id(1);
    const uint32_t relY = get_global_id(2);

    const uint32_t blockX = math_biome2block(startBiomeX + relX);
    const uint32_t blockY = math_biome2block(startBiomeY + relY);
    const uint32_t blockZ = math_biome2block(startBiomeZ + relZ);

    sample_int32_ctx_t ctx = make_sample_int32_ctx(const_data, rw_data, blockX, blockY, blockZ, 0);

    const double temperature = df_binding_temperature(ctx);
    const double vegetation = df_binding_vegetation(ctx);
    const double continents = df_binding_continents(ctx);
    const double erosion = df_binding_erosion(ctx);
    const double depth = df_binding_depth(ctx);
    const double ridges = df_binding_ridges(ctx);

    const int16_t target[7] = {
        convert_short_sat((int64_t) (((float) temperature) * 10000.0F)),
        convert_short_sat((int64_t) (((float) vegetation) * 10000.0F)),
        convert_short_sat((int64_t) (((float) continents) * 10000.0F)),
        convert_short_sat((int64_t) (((float) erosion) * 10000.0F)),
        convert_short_sat((int64_t) (((float) depth) * 10000.0F)),
        convert_short_sat((int64_t) (((float) ridges) * 10000.0F)),
        0,
    };

    global const biome_search_tree_node_t * restrict const root_node = ptr_shift_global(const_data, biome_multinoise_tree_offset);

    const uint32_t result_biome = math_biome_search_tree_calc(root_node, target, biome_multinoise_tree_nodes_c);

    uint32_t idx = ((relY) * sizeX + relZ) * sizeZ + relX;
    res_biomes[idx] = result_biome;
}
#endif
