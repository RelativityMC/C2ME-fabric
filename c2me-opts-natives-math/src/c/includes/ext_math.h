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

#include <stdbool.h>
#include <stdint.h>

#define fminf(x, y) __builtin_fminf(x, y)
#define fmaxf(x, y) __builtin_fmaxf(x, y)
#define fmodf(x, y) __builtin_fmodf(x, y)
#define fabsf(x) __builtin_fabsf(x)
#define floor(x) __builtin_floor(x)
#define sqrtf(x) __builtin_sqrtf(x)
#define labs(x) __builtin_labs(x)

static const double SQRT_3 = 1.7320508075688772;
// 0.5 * (SQRT_3 - 1.0)
static const double SKEW_FACTOR_2D = 0.3660254037844386;
// (3.0 - SQRT_3) / 6.0
static const double UNSKEW_FACTOR_2D = 0.21132486540518713;

#pragma clang attribute push (__attribute__((always_inline)), apply_to = function)

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
        double var0 = FLAT_SIMPLEX_GRAD[i | 0] * x;
        double var1 = FLAT_SIMPLEX_GRAD[i | 1] * y;
        double var2 = FLAT_SIMPLEX_GRAD[i | 2] * z;
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

static inline __attribute__((const)) int32_t
__math_simplex_map(const int32_t *const permutations, const int32_t input) {
    return permutations[input & 0xFF];
}

static inline __attribute__((const)) double math_simplex_dot(const int32_t hash, const double x, const double y,
                                                             const double z) {
    const int32_t loc = hash << 2;
    return FLAT_SIMPLEX_GRAD[loc | 0] * x + FLAT_SIMPLEX_GRAD[loc | 1] * y + FLAT_SIMPLEX_GRAD[loc | 2] * z;
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
math_noise_simplex_sample2d(const int32_t *const permutations, const double x, const double y) {
    const double aaaaa = SKEW_FACTOR_2D;
    const double d = (x + y) * aaaaa;
    const int32_t i = floor(x + d);
    const int32_t j = floor(y + d);
    const double e = ((double) (i + j)) * UNSKEW_FACTOR_2D;
    const double f = i - e;
    const double g = j - e;
    const double h = x - f;
    const double k = y - g;
    int32_t l;
    int32_t m;
    if (h > k) {
        l = 1;
        m = 0;
    } else {
        l = 0;
        m = 1;
    }

    const double n = h - l + UNSKEW_FACTOR_2D;
    const double o = k - m + UNSKEW_FACTOR_2D;
    const double p = h - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
    const double q = k - 1.0 + 2.0 * UNSKEW_FACTOR_2D;
    const int32_t r = i & 0xFF;
    const int32_t s = j & 0xFF;
    const int32_t t = __math_simplex_map(permutations, r + __math_simplex_map(permutations, s)) % 12;
    const int32_t u = __math_simplex_map(permutations, r + l + __math_simplex_map(permutations, s + m)) % 12;
    const int32_t v = __math_simplex_map(permutations, r + 1 + __math_simplex_map(permutations, s + 1)) % 12;
    const double w = __math_simplex_grad(t, h, k, 0.0, 0.5);
    const double z = __math_simplex_grad(u, n, o, 0.0, 0.5);
    const double aa = __math_simplex_grad(v, p, q, 0.0, 0.5);
    return 70.0 * (w + z + aa);
}

static inline __attribute__((const)) double math_perlinFade(const double value) {
    return value * value * value * (value * (value * 6.0 - 15.0) + 10.0);
}

static inline __attribute__((const)) double __math_perlin_grad(const uint32_t hash, const double x, const double y,
                                                               const double z) {
    const int32_t loc = (hash & 15) << 2;
    return FLAT_SIMPLEX_GRAD[loc | 0] * x + FLAT_SIMPLEX_GRAD[loc | 1] * y + FLAT_SIMPLEX_GRAD[loc | 2] * z;
}

static inline __attribute__((const)) int32_t __math_perlin_map(const uint8_t *const permutations, const int32_t input) {
    return permutations[input & 0xFF] & 0xFF;
}

static inline __attribute__((const)) double
math_noise_perlin_sampleScalar(const uint8_t *const permutations,
                               const int32_t sectionX, const int32_t sectionY, const int32_t sectionZ,
                               const double localX, const double localY, const double localZ, const double fadeLocalY) {
    const int32_t i = __math_perlin_map(permutations, sectionX);
    const int32_t j = __math_perlin_map(permutations, sectionX + 1);
    const int32_t k = __math_perlin_map(permutations, i + sectionY);
    const int32_t l = __math_perlin_map(permutations, i + sectionY + 1);
    const int32_t m = __math_perlin_map(permutations, j + sectionY);
    const int32_t n = __math_perlin_map(permutations, j + sectionY + 1);
    const double d = __math_perlin_grad(__math_perlin_map(permutations, k + sectionZ), localX, localY, localZ);
    const double e = __math_perlin_grad(__math_perlin_map(permutations, m + sectionZ), localX - 1.0, localY, localZ);
    const double f = __math_perlin_grad(__math_perlin_map(permutations, l + sectionZ), localX, localY - 1.0, localZ);
    const double g = __math_perlin_grad(__math_perlin_map(permutations, n + sectionZ), localX - 1.0, localY - 1.0,
                                        localZ);
    const double h = __math_perlin_grad(__math_perlin_map(permutations, k + sectionZ + 1), localX, localY,
                                        localZ - 1.0);
    const double o = __math_perlin_grad(__math_perlin_map(permutations, m + sectionZ + 1), localX - 1.0, localY,
                                        localZ - 1.0);
    const double p = __math_perlin_grad(__math_perlin_map(permutations, l + sectionZ + 1), localX, localY - 1.0,
                                        localZ - 1.0);
    const double q = __math_perlin_grad(__math_perlin_map(permutations, n + sectionZ + 1), localX - 1.0, localY - 1.0,
                                        localZ - 1.0);
    const double r = math_perlinFade(localX);
    const double s = math_perlinFade(fadeLocalY);
    const double t = math_perlinFade(localZ);
    return math_lerp3(r, s, t, d, e, f, g, h, o, p, q);
}


static inline __attribute__((const)) double
math_noise_perlin_sample(const uint8_t *const permutations,
                         const double originX, const double originY, const double originZ,
                         const double x, const double y, const double z,
                         const double yScale, const double yMax) {
    const double d = x + originX;
    const double e = y + originY;
    const double f = z + originZ;
    const double i = floor(d);
    const double j = floor(e);
    const double k = floor(f);
    const double g = d - i;
    const double h = e - j;
    const double l = f - k;
    const double o = yScale != 0 ? floor(((yMax >= 0.0 && yMax < h) ? yMax : h) / yScale + 1.0E-7) * yScale : 0;

    return math_noise_perlin_sampleScalar(permutations, (int64_t) i, (int64_t) j, (int64_t) k, g, h - o, l, h);
}


typedef const struct double_octave_sampler_data {
    const uint64_t length;
    const bool *const need_shift __attribute__((align_value(64)));
    const double *const lacunarity_powd __attribute__((align_value(64)));
    const double *const persistence_powd __attribute__((align_value(64)));
    const uint8_t *const sampler_permutations __attribute__((align_value(64)));
    const double *const sampler_originX __attribute__((align_value(64)));
    const double *const sampler_originY __attribute__((align_value(64)));
    const double *const sampler_originZ __attribute__((align_value(64)));
    const double *const amplitudes __attribute__((align_value(64)));
} double_octave_sampler_data_t;

static inline __attribute__((const)) double
math_noise_perlin_double_octave_sample_impl(const double_octave_sampler_data_t *const data,
                                            const double x, const double y, const double z,
                                            const double yScale, const double yMax, const uint8_t useOrigin) {
    double d = 0.0;

#pragma clang loop vectorize(enable) interleave(enable) interleave_count(2)
    for (uint32_t i = 0; i < data->length; i++) {
        const double e = data->lacunarity_powd[i];
        const double f = data->persistence_powd[i];
        const uint8_t *permutations = data->sampler_permutations + 256 * i;
        const double sampleX = data->need_shift[i] ? x * 1.0181268882175227 : x;
        const double sampleY = data->need_shift[i] ? y * 1.0181268882175227 : y;
        const double sampleZ = data->need_shift[i] ? z * 1.0181268882175227 : z;
        const double g = math_noise_perlin_sample(
            permutations,
            data->sampler_originX[i],
            data->sampler_originY[i],
            data->sampler_originZ[i],
            math_octave_maintainPrecision(sampleX * e),
            useOrigin ? -(data->sampler_originY[i]) : math_octave_maintainPrecision(sampleY * e),
            math_octave_maintainPrecision(sampleZ * e),
            yScale * e,
            yMax * e);
        d += data->amplitudes[i] * g * f;
    }

    return d;
}

static inline __attribute__((const)) double
math_noise_perlin_double_octave_sample(const double_octave_sampler_data_t *const data,
                                       const double x, const double y, const double z) {
    return math_noise_perlin_double_octave_sample_impl(data, x, y, z, 0.0, 0.0, 0);
}

typedef const struct interpolated_noise_sampler {
    const double scaledXzScale;
    const double scaledYScale;
    const double xzFactor;
    const double yFactor;
    const double smearScaleMultiplier;
    const double xzScale;
    const double yScale;

    const uint8_t *const sampler_permutations __attribute__((align_value(64)));
    const double *const sampler_originX __attribute__((align_value(64)));
    const double *const sampler_originY __attribute__((align_value(64)));
    const double *const sampler_originZ __attribute__((align_value(64)));
    const double *const sampler_mulFactor __attribute__((align_value(64)));

    const uint32_t upperNoiseOffset;
    const uint32_t normalNoiseOffset;
    const uint32_t endOffset;
} interpolated_noise_sampler_t;


static inline __attribute__((const)) double
math_noise_perlin_interpolated_sample(const interpolated_noise_sampler_t *const data,
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

#pragma clang loop vectorize(enable) interleave(enable)
    for (uint32_t offset = data->normalNoiseOffset; offset < data->endOffset; offset++) {
        n += math_noise_perlin_sample(
            data->sampler_permutations + 256 * offset,
            data->sampler_originX[offset],
            data->sampler_originY[offset],
            data->sampler_originZ[offset],
            math_octave_maintainPrecision(g * data->sampler_mulFactor[offset]),
            math_octave_maintainPrecision(h * data->sampler_mulFactor[offset]),
            math_octave_maintainPrecision(i * data->sampler_mulFactor[offset]),
            k * data->sampler_mulFactor[offset],
            h * data->sampler_mulFactor[offset]
        ) / data->sampler_mulFactor[offset];
    }

    const double q = (n / 10.0 + 1.0) / 2.0;
    const uint8_t bl2 = q >= 1.0;
    const uint8_t bl3 = q <= 0.0;

    if (!bl2) {
#pragma clang loop vectorize(enable) interleave(enable)
        for (uint32_t offset = 0; offset < data->upperNoiseOffset; offset++) {
            l += math_noise_perlin_sample(
                data->sampler_permutations + 256 * offset,
                data->sampler_originX[offset],
                data->sampler_originY[offset],
                data->sampler_originZ[offset],
                math_octave_maintainPrecision(d * data->sampler_mulFactor[offset]),
                math_octave_maintainPrecision(e * data->sampler_mulFactor[offset]),
                math_octave_maintainPrecision(f * data->sampler_mulFactor[offset]),
                j * data->sampler_mulFactor[offset],
                e * data->sampler_mulFactor[offset]
            ) / data->sampler_mulFactor[offset];
        }
    }

    if (!bl3) {
#pragma clang loop vectorize(enable) interleave(enable)
        for (uint32_t offset = data->upperNoiseOffset; offset < data->normalNoiseOffset; offset++) {
            m += math_noise_perlin_sample(
                data->sampler_permutations + 256 * offset,
                data->sampler_originX[offset],
                data->sampler_originY[offset],
                data->sampler_originZ[offset],
                math_octave_maintainPrecision(d * data->sampler_mulFactor[offset]),
                math_octave_maintainPrecision(e * data->sampler_mulFactor[offset]),
                math_octave_maintainPrecision(f * data->sampler_mulFactor[offset]),
                j * data->sampler_mulFactor[offset],
                e * data->sampler_mulFactor[offset]
            ) / data->sampler_mulFactor[offset];
        }
    }

    return math_clampedLerp(l / 512.0, m / 512.0, q) / 128.0;
}

static inline __attribute__((const)) double
math_noise_perlin_interpolated_sample_specializedBase3dNoiseFunction(const interpolated_noise_sampler_t *const data,
                                                                     const double x, const double y, const double z) {
//    assert(data->upperNoiseOffset == 16);
//    assert(data->normalNoiseOffset == 32);
//    assert(data->endOffset == 40);

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

#pragma clang loop vectorize(enable) interleave(enable)
    for (uint32_t offset = 32; offset < 40; offset++) {
        n += math_noise_perlin_sample(
            data->sampler_permutations + 256 * offset,
            data->sampler_originX[offset],
            data->sampler_originY[offset],
            data->sampler_originZ[offset],
            math_octave_maintainPrecision(g * data->sampler_mulFactor[offset]),
            math_octave_maintainPrecision(h * data->sampler_mulFactor[offset]),
            math_octave_maintainPrecision(i * data->sampler_mulFactor[offset]),
            k * data->sampler_mulFactor[offset],
            h * data->sampler_mulFactor[offset]
        ) / data->sampler_mulFactor[offset];
    }

    const double q = (n / 10.0 + 1.0) / 2.0;
    const uint8_t bl2 = q >= 1.0;
    const uint8_t bl3 = q <= 0.0;

    if (!bl2) {
#pragma clang loop vectorize(enable) interleave(enable)
        for (uint32_t offset = 0; offset < 16; offset++) {
            l += math_noise_perlin_sample(
                data->sampler_permutations + 256 * offset,
                data->sampler_originX[offset],
                data->sampler_originY[offset],
                data->sampler_originZ[offset],
                math_octave_maintainPrecision(d * data->sampler_mulFactor[offset]),
                math_octave_maintainPrecision(e * data->sampler_mulFactor[offset]),
                math_octave_maintainPrecision(f * data->sampler_mulFactor[offset]),
                j * data->sampler_mulFactor[offset],
                e * data->sampler_mulFactor[offset]
            ) / data->sampler_mulFactor[offset];
        }
    }

    if (!bl3) {
#pragma clang loop vectorize(enable) interleave(enable)
        for (uint32_t offset = 16; offset < 32; offset++) {
            m += math_noise_perlin_sample(
                data->sampler_permutations + 256 * offset,
                data->sampler_originX[offset],
                data->sampler_originY[offset],
                data->sampler_originZ[offset],
                math_octave_maintainPrecision(d * data->sampler_mulFactor[offset]),
                math_octave_maintainPrecision(e * data->sampler_mulFactor[offset]),
                math_octave_maintainPrecision(f * data->sampler_mulFactor[offset]),
                j * data->sampler_mulFactor[offset],
                e * data->sampler_mulFactor[offset]
            ) / data->sampler_mulFactor[offset];
        }
    }

    return math_clampedLerp(l / 512.0, m / 512.0, q) / 128.0;
}

static inline __attribute__((const)) float
math_end_islands_sample(const int32_t *const simplex_permutations, const int32_t x, const int32_t z) {
    const int32_t i = x / 2;
    const int32_t j = z / 2;
    const int32_t k = x % 2;
    const int32_t l = z % 2;
    const int32_t muld = x * x + z * z;
    if (muld < 0) {
        return __builtin_nanf("");
    }
    float f = 100.0F - sqrtf((float) muld) * 8.0F;
    f = clampf(f, -100.0F, 80.0F);

    uint32_t hit_count = 0;
    int8_t ms[25 * 25], ns[25 * 25];
    const int64_t omin = labs(i) - 12LL;
    const int64_t pmin = labs(j) - 12LL;

    if (omin * omin + pmin * pmin > 4096) {
#pragma clang loop vectorize(enable) interleave(enable)
        for (int8_t m = -12; m < 13; m++) {
            for (int8_t n = -12; n < 13; n++) {
                const int64_t o = (int64_t) i + m;
                const int64_t p = (int64_t) j + n;
                if (math_noise_simplex_sample2d(simplex_permutations, (double) o, (double) p) < -0.9F) {
                    const uint32_t slot = hit_count++;
                    ms[slot] = m;
                    ns[slot] = n;
                }
            }
        }
    } else {
#pragma clang loop vectorize(enable) interleave(enable)
        for (int8_t m = -12; m < 13; m++) {
            for (int8_t n = -12; n < 13; n++) {
                const int64_t o = (int64_t) i + m;
                const int64_t p = (int64_t) j + n;
                if (o * o + p * p > 4096LL) {
                    if (math_noise_simplex_sample2d(simplex_permutations, (double) o, (double) p) < -0.9F) {
                        const uint32_t slot = hit_count++;
                        ms[slot] = m;
                        ns[slot] = n;
                    }
                }
            }
        }
    }

#pragma clang loop vectorize(enable) interleave(enable)
    for (uint32_t slot = 0; slot < hit_count; slot++) {
        const int32_t m = ms[slot];
        const int32_t n = ns[slot];
        const int64_t o = (int64_t) i + (int64_t) m;
        const int64_t p = (int64_t) j + (int64_t) n;
        const float g = fmodf((fabsf((float) o) * 3439.0F + fabsf((float) p) * 147.0F), 13.0F) + 9.0F;
        const float h = (float) (k - m * 2);
        const float q = (float) (l - n * 2);
        float r = 100.0F - sqrtf(h * h + q * q) * g;
        r = clampf(r, -100.0F, 80.0F);
        f = fmaxf(f, r);
    }

    return f;
}

#pragma clang attribute pop

