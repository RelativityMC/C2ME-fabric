#version 460

// Port of c2me_opencl_ext_math.cl.
//
// GLSL used for readability, compiled to SPIR-V at build time, once per SpirVGen.ProgramType (see the compilePreludeSpirV tasks).
//
// TODO use std430 instead of scalar block layout

#extension GL_EXT_buffer_reference2 : require
#extension GL_EXT_scalar_block_layout : require
#extension GL_EXT_shader_explicit_arithmetic_types_int64 : require
#extension GL_EXT_shader_explicit_arithmetic_types_int32 : require
#extension GL_EXT_shader_explicit_arithmetic_types_int16 : require
#extension GL_EXT_shader_explicit_arithmetic_types_int8 : require

#if defined(DF_COMPILE_FLAT_CACHE_PREFILL) || defined(DF_COMPILE_NOISE_KERNEL)
layout(local_size_x = 16, local_size_y = 16, local_size_z = 1) in;
#else
layout(local_size_x = 8, local_size_y = 8, local_size_z = 1) in;
#endif

layout(constant_id = 0) const int  genShapeCfg_minimumY          = 0;
layout(constant_id = 1) const int  genShapeCfg_height            = 384;
layout(constant_id = 2) const uint genShapeCfg_horizontalSize    = 1u;
layout(constant_id = 3) const uint genShapeCfg_verticalSize      = 2u;
layout(constant_id = 4) const uint biome_multinoise_tree_offset  = 0u;
layout(constant_id = 5) const uint biome_multinoise_tree_nodes_c = 0u;
layout(constant_id = 6) const uint BIOME_SEARCH_TREE_MAX_DEPTH   = 1u;

const uint MASK_enableFlatCache = 1u << 0;
const uint MASK_enableAllCaches = (1u << 1) | MASK_enableFlatCache;

// addressing
layout(buffer_reference, scalar, buffer_reference_align = 1) buffer U8Ref   { uint8_t  v[]; };
layout(buffer_reference, scalar, buffer_reference_align = 2) buffer U16Ref  { uint16_t v[]; };
layout(buffer_reference, scalar, buffer_reference_align = 2) buffer I16Ref  { int16_t  v[]; };
layout(buffer_reference, scalar, buffer_reference_align = 4) buffer U32Ref  { uint     v[]; };
layout(buffer_reference, scalar, buffer_reference_align = 4) buffer I32Ref  { int      v[]; };
layout(buffer_reference, scalar, buffer_reference_align = 4) buffer F32Ref  { float    v[]; };
layout(buffer_reference, scalar, buffer_reference_align = 8) buffer F64Ref  { double   v[]; };

struct sample_int32_ctx_t {
    uint64_t const_data;
    uint64_t rw_data;
    int x;
    int y;
    int z;
    uint sample_flags;
};

struct cache_result_t {
    bool cached;
    double res;
};

layout(push_constant, scalar) uniform PushConstants {
    uint64_t const_data;
    uint64_t rw_data;
    uint64_t out_data;
    int arg0;
    int arg1;
    int arg2;
} pc;

uint64_t df_data_offset_global(uint64_t root, int index) {
    int offset = I32Ref(root + 128ul).v[index];
    return offset != 0 ? root + uint64_t(offset) : 0ul;
}

const double SQRT_3            = 1.7320508075688772lf;
const double SKEW_FACTOR_2D    = 0.3660254037844386lf;
const double UNSKEW_FACTOR_2D  = 0.21132486540518713lf;

const double FLAT_SIMPLEX_GRAD[48] = double[48](
     1.0lf,  1.0lf,  0.0lf,   -1.0lf,  1.0lf,  0.0lf,    1.0lf, -1.0lf,  0.0lf,   -1.0lf, -1.0lf,  0.0lf,
     1.0lf,  0.0lf,  1.0lf,   -1.0lf,  0.0lf,  1.0lf,    1.0lf,  0.0lf, -1.0lf,   -1.0lf,  0.0lf, -1.0lf,
     0.0lf,  1.0lf,  1.0lf,    0.0lf, -1.0lf,  1.0lf,    0.0lf,  1.0lf, -1.0lf,    0.0lf, -1.0lf, -1.0lf,
     1.0lf,  1.0lf,  0.0lf,    0.0lf, -1.0lf,  1.0lf,   -1.0lf,  1.0lf,  0.0lf,    0.0lf, -1.0lf, -1.0lf
);

double math_floor(const double v) {
    return floor(v);
}

uint64_t math_rotateLeftU64(uint64_t i, uint64_t distance) {
    return (i << (distance & 63ul)) | (i >> ((64ul - distance) & 63ul));
}

// GLSL leaves the sign of % undefined for negative operands, so the remainder is computed
// explicitly rather than with the operator.
int math_floorDiv(const int x, const int y) {
    int q = x / y;
    int r = x - q * y;
    if (r != 0 && ((r < 0) != (y < 0))) {
        q--;
    }
    return q;
}

int math_floorMod(const int x, const int y) {
    return x - math_floorDiv(x, y) * y;
}

double math_octave_maintainPrecision(const double value) {
    return value - math_floor(value / 3.3554432E7lf + 0.5lf) * 3.3554432E7lf;
}

double math_lerp(const double delta, const double start, const double end) {
    return start + delta * (end - start);
}

float math_lerpf(const float delta, const float start, const float end) {
    return start + delta * (end - start);
}

double math_clampedLerp(const double start, const double end, const double delta) {
    if (delta < 0.0lf) {
        return start;
    } else {
        return delta > 1.0lf ? end : math_lerp(delta, start, end);
    }
}

double math_square(const double operand) {
    return operand * operand;
}

double math_lerp2(const double deltaX, const double deltaY, const double x0y0,
                  const double x1y0, const double x0y1, const double x1y1) {
    return math_lerp(deltaY, math_lerp(deltaX, x0y0, x1y0), math_lerp(deltaX, x0y1, x1y1));
}

double math_lerp3(const double deltaX, const double deltaY, const double deltaZ,
                  const double x0y0z0, const double x1y0z0, const double x0y1z0, const double x1y1z0,
                  const double x0y0z1, const double x1y0z1, const double x0y1z1, const double x1y1z1) {
    return math_lerp(deltaZ, math_lerp2(deltaX, deltaY, x0y0z0, x1y0z0, x0y1z0, x1y1z0),
                             math_lerp2(deltaX, deltaY, x0y0z1, x1y0z1, x0y1z1, x1y1z1));
}

double math_getLerpProgress(const double value, const double start, const double end) {
    return (value - start) / (end - start);
}

double math_clampedLerpFromProgress(const double lerpValue, const double lerpStart, const double lerpEnd,
                                    const double start, const double end) {
    return math_clampedLerp(start, end, math_getLerpProgress(lerpValue, lerpStart, lerpEnd));
}

double math_clampedMap(const double value, const double oldStart, const double oldEnd,
                       const double newStart, const double newEnd) {
    return math_clampedLerp(newStart, newEnd, math_getLerpProgress(value, oldStart, oldEnd));
}

int math_biome2block(const int biomeCoord) {
    return biomeCoord << 2;
}

int math_block2biome(const int blockCoord) {
    return blockCoord >> 2;
}

uint c2me_math_simplex_map_global(uint64_t permutations, const int value) {
    return U32Ref(permutations).v[value & 0xFF];
}

double math_simplex_dot(const int hash, const double x, const double y, const double z) {
    return FLAT_SIMPLEX_GRAD[hash * 3 + 0] * x
         + FLAT_SIMPLEX_GRAD[hash * 3 + 1] * y
         + FLAT_SIMPLEX_GRAD[hash * 3 + 2] * z;
}

double c2me_math_simplex_grad(const int hash, const double x, const double y, const double z,
                           const double distance) {
    double d = distance - x * x - y * y - z * z;
    double e;
    if (d < 0.0lf) {
        e = 0.0lf;
    } else {
        d *= d;
        e = d * d * math_simplex_dot(hash, x, y, z);
    }
    return e;
}

double math_simplex_grad(const int hash, const double x, const double y, const double z,
                         const double distance) {
    double d = distance - x * x - y * y - z * z;
    if (d < 0.0lf) {
        return 0.0lf;
    } else {
        double var0 = FLAT_SIMPLEX_GRAD[hash * 3 + 0] * x;
        double var1 = FLAT_SIMPLEX_GRAD[hash * 3 + 1] * y;
        double var2 = FLAT_SIMPLEX_GRAD[hash * 3 + 2] * z;
        return d * d * d * d * (var0 + var1 + var2);
    }
}

double math_perlinFade(const double value) {
    return value * value * value * (value * (value * 6.0lf - 15.0lf) + 10.0lf);
}

double c2me_math_perlin_grad_global(uint64_t permutations, const int px, const int py, const int pz,
                                    const double fx, const double fy, const double fz) {
    U8Ref perm = U8Ref(permutations);
    const uint map0 = uint(perm.v[uint(px) & 0xFFu]) + uint(py);
    const uint map1 = uint(perm.v[map0 & 0xFFu]) + uint(pz);
    const uint hash = uint(perm.v[map1 & 0xFFu]) & 0xFu;
    return FLAT_SIMPLEX_GRAD[hash * 3u + 0u] * fx
         + FLAT_SIMPLEX_GRAD[hash * 3u + 1u] * fy
         + FLAT_SIMPLEX_GRAD[hash * 3u + 2u] * fz;
}

double math_noise_perlin_sampleScalar_global(uint64_t permutations,
                                             const int px0, const int py0, const int pz0,
                                             const double fx0, const double fy0, const double fz0,
                                             const double fadeLocalY) {
    const int px1 = px0 + 1;
    const int py1 = py0 + 1;
    const int pz1 = pz0 + 1;
    const double fx1 = fx0 - 1.0lf;
    const double fy1 = fy0 - 1.0lf;
    const double fz1 = fz0 - 1.0lf;

    double f000 = c2me_math_perlin_grad_global(permutations, px0, py0, pz0, fx0, fy0, fz0);
    double f100 = c2me_math_perlin_grad_global(permutations, px1, py0, pz0, fx1, fy0, fz0);
    double f010 = c2me_math_perlin_grad_global(permutations, px0, py1, pz0, fx0, fy1, fz0);
    double f110 = c2me_math_perlin_grad_global(permutations, px1, py1, pz0, fx1, fy1, fz0);
    double f001 = c2me_math_perlin_grad_global(permutations, px0, py0, pz1, fx0, fy0, fz1);
    double f101 = c2me_math_perlin_grad_global(permutations, px1, py0, pz1, fx1, fy0, fz1);
    double f011 = c2me_math_perlin_grad_global(permutations, px0, py1, pz1, fx0, fy1, fz1);
    double f111 = c2me_math_perlin_grad_global(permutations, px1, py1, pz1, fx1, fy1, fz1);

    const double dx = math_perlinFade(fx0);
    const double dy = math_perlinFade(fadeLocalY);
    const double dz = math_perlinFade(fz0);
    return math_lerp3(dx, dy, dz, f000, f100, f010, f110, f001, f101, f011, f111);
}

double math_noise_perlin_sample_global(uint64_t permutations,
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
    const double o = yScale != 0.0lf
            ? math_floor(((yMax >= 0.0lf && yMax < h) ? yMax : h) / yScale + 1.0E-7lf) * yScale
            : 0.0lf;

    return math_noise_perlin_sampleScalar_global(permutations, int(i), int(j), int(k), g, h - o, l, h);
}

double math_noise_simplex_sample2d_global(uint64_t permutations, const double x, const double y) {
    const double d = (x + y) * SKEW_FACTOR_2D;
    const double i = math_floor(x + d);
    const double j = math_floor(y + d);
    const double e = (i + j) * UNSKEW_FACTOR_2D;
    const double f = i - e;
    const double g = j - e;
    const double h = x - f;
    const double k = y - g;
    double l;
    int li;
    double m;
    int mi;
    if (h > k) {
        l = 1.0lf; li = 1; m = 0.0lf; mi = 0;
    } else {
        l = 0.0lf; li = 1; m = 1.0lf; mi = 1;
    }

    const double n = h - l + UNSKEW_FACTOR_2D;
    const double o = k - m + UNSKEW_FACTOR_2D;
    const double p = h - 1.0lf + 2.0lf * UNSKEW_FACTOR_2D;
    const double q = k - 1.0lf + 2.0lf * UNSKEW_FACTOR_2D;
    const int r = int(i) & 0xFF;
    const int s = int(j) & 0xFF;
    const int t = int(c2me_math_simplex_map_global(permutations, r + int(c2me_math_simplex_map_global(permutations, s))) % 12u);
    const int u = int(c2me_math_simplex_map_global(permutations, r + li + int(c2me_math_simplex_map_global(permutations, s + mi))) % 12u);
    const int v = int(c2me_math_simplex_map_global(permutations, r + 1 + int(c2me_math_simplex_map_global(permutations, s + 1))) % 12u);
    const double w = c2me_math_simplex_grad(t, h, k, 0.0lf, 0.5lf);
    const double z = c2me_math_simplex_grad(u, n, o, 0.0lf, 0.5lf);
    const double aa = c2me_math_simplex_grad(v, p, q, 0.0lf, 0.5lf);
    return 70.0lf * (w + z + aa);
}

layout(buffer_reference, scalar, buffer_reference_align = 8) buffer DoubleOctaveSamplerDataRef {
    uint64_t octaveCount;
    double amplitude;
    int need_shift;
    int lacunarity_powd;
    int persistence_powd;
    int sampler_permutations;
    int sampler_originX;
    int sampler_originY;
    int sampler_originZ;
    int amplitudes;
};

double math_noise_perlin_double_octave_sample_impl_global(uint64_t dataAddr,
                                                          const double x, const double y, const double z,
                                                          const double yScale, const double yMax,
                                                          const uint useOrigin) {
    DoubleOctaveSamplerDataRef data = DoubleOctaveSamplerDataRef(dataAddr);

    double d1 = 0.0lf;
    double d2 = 0.0lf;

    // C bool is one byte, so need_shift is a byte array.
    U8Ref  need_shift       = U8Ref(dataAddr + uint64_t(data.need_shift));
    F64Ref lacunarity_powd  = F64Ref(dataAddr + uint64_t(data.lacunarity_powd));
    F64Ref persistence_powd = F64Ref(dataAddr + uint64_t(data.persistence_powd));
    uint64_t permutations   = dataAddr + uint64_t(data.sampler_permutations);
    F64Ref sampler_originX  = F64Ref(dataAddr + uint64_t(data.sampler_originX));
    F64Ref sampler_originY  = F64Ref(dataAddr + uint64_t(data.sampler_originY));
    F64Ref sampler_originZ  = F64Ref(dataAddr + uint64_t(data.sampler_originZ));
    F64Ref amplitudes       = F64Ref(dataAddr + uint64_t(data.amplitudes));

    for (uint i = 0u; i < uint(data.octaveCount); i++) {
        const double e = lacunarity_powd.v[i];
        const double f = persistence_powd.v[i];
        const bool shift = need_shift.v[i] != uint8_t(0);
        const double sampleX = shift ? x * 1.0181268882175227lf : x;
        const double sampleY = shift ? y * 1.0181268882175227lf : y;
        const double sampleZ = shift ? z * 1.0181268882175227lf : z;
        const double g = math_noise_perlin_sample_global(
                permutations + uint64_t(256u * i),
                sampler_originX.v[i],
                sampler_originY.v[i],
                sampler_originZ.v[i],
                math_octave_maintainPrecision(sampleX * e),
                useOrigin != 0u ? -(sampler_originY.v[i]) : math_octave_maintainPrecision(sampleY * e),
                math_octave_maintainPrecision(sampleZ * e),
                yScale * e,
                yMax * e);
        const double d = amplitudes.v[i] * g * f;
        if (!shift) {
            d1 += d;
        } else {
            d2 += d;
        }
    }

    return (d1 + d2) * data.amplitude;
}

double math_noise_perlin_double_octave_sample_global_noinline(uint64_t data,
                                                              const double x, const double y, const double z) {
    return math_noise_perlin_double_octave_sample_impl_global(data, x, y, z, 0.0lf, 0.0lf, 0u);
}

double math_noise_perlin_double_octave_sample_global(uint64_t data,
                                                     const double x, const double y, const double z) {
    return math_noise_perlin_double_octave_sample_impl_global(data, x, y, z, 0.0lf, 0.0lf, 0u);
}

struct interpolated_noise_sub_sampler_t {
    uint count;
    int sampler_permutations;
    int sampler_originX;
    int sampler_originY;
    int sampler_originZ;
    int sampler_mulFactor;
};

layout(buffer_reference, scalar, buffer_reference_align = 8) buffer InterpolatedNoiseSamplerRef {
    double scaledXzScale;
    double scaledYScale;
    double xzFactor;
    double yFactor;
    double smearScaleMultiplier;
    double xzScale;
    double yScale;
    interpolated_noise_sub_sampler_t lower;
    interpolated_noise_sub_sampler_t upper;
    interpolated_noise_sub_sampler_t normal;
};

double c2me_interpolated_octaves(uint64_t dataAddr, interpolated_noise_sub_sampler_t sub,
                                 const double sx, const double sy, const double sz,
                                 const double smear, const double yMaxBase) {
    uint64_t permutations  = dataAddr + uint64_t(sub.sampler_permutations);
    F64Ref sampler_originX = F64Ref(dataAddr + uint64_t(sub.sampler_originX));
    F64Ref sampler_originY = F64Ref(dataAddr + uint64_t(sub.sampler_originY));
    F64Ref sampler_originZ = F64Ref(dataAddr + uint64_t(sub.sampler_originZ));
    F64Ref sampler_mul     = F64Ref(dataAddr + uint64_t(sub.sampler_mulFactor));

    double acc = 0.0lf;
    for (uint offset = 0u; offset < sub.count; offset++) {
        const double mul = sampler_mul.v[offset];
        acc += math_noise_perlin_sample_global(
                permutations + uint64_t(256u * offset),
                sampler_originX.v[offset],
                sampler_originY.v[offset],
                sampler_originZ.v[offset],
                math_octave_maintainPrecision(sx * mul),
                math_octave_maintainPrecision(sy * mul),
                math_octave_maintainPrecision(sz * mul),
                smear * mul,
                yMaxBase * mul) / mul;
    }
    return acc;
}

double math_noise_perlin_interpolated_sample_global(uint64_t dataAddr,
                                                    const double x, const double y, const double z) {
    InterpolatedNoiseSamplerRef data = InterpolatedNoiseSamplerRef(dataAddr);

    const double d = x * data.scaledXzScale;
    const double e = y * data.scaledYScale;
    const double f = z * data.scaledXzScale;
    const double g = d / data.xzFactor;
    const double h = e / data.yFactor;
    const double i = f / data.xzFactor;
    const double j = data.scaledYScale * data.smearScaleMultiplier;
    const double k = j / data.yFactor;

    double n = c2me_interpolated_octaves(dataAddr, data.normal, g, h, i, k, h);

    const double q = (n / 10.0lf + 1.0lf) / 2.0lf;
    const bool bl2 = q >= 1.0lf;
    const bool bl3 = q <= 0.0lf;

    double l = 0.0lf;
    double m = 0.0lf;
    if (!bl2) l = c2me_interpolated_octaves(dataAddr, data.lower, d, e, f, j, e);
    if (!bl3) m = c2me_interpolated_octaves(dataAddr, data.upper, d, e, f, j, e);

    return math_clampedLerp(l / 512.0lf, m / 512.0lf, q) / 128.0lf;
}

double math_noise_perlin_interpolated_sample_global_noinline(uint64_t data,
                                                             const double x, const double y, const double z) {
    return math_noise_perlin_interpolated_sample_global(data, x, y, z);
}

float c2me_nanf() {
    return uintBitsToFloat(0x7FC00000u);
}

float math_end_islands_sample_global(uint64_t simplex_permutations, const int x, const int z) {
    // GLSL leaves the sign of / and % undefined for negatives; spell the truncating division
    // out rather than relying on either.
    const int i = x / 2;
    const int j = z / 2;
    const int k = x - i * 2;
    const int l = z - j * 2;

    // The C computes this in a volatile int32 specifically to observe the overflow. Signed
    // overflow is undefined in GLSL too, so do it unsigned -- which wraps by definition -- and
    // test the sign bit by hand.
    const uint muld = uint(x) * uint(x) + uint(z) * uint(z);
    if ((muld & 0x80000000u) != 0u) {
        return c2me_nanf();
    }

    float f = 100.0 - sqrt(float(muld & 0x7FFFFFFFu)) * 8.0;
    f = clamp(f, -100.0, 80.0);

    const int64_t omin = int64_t(abs(i)) - 12L;
    const int64_t pmin = int64_t(abs(j)) - 12L;

    // The C materialised ms/ns/hit as three 625-entry private arrays. They are pure functions of
    // the loop index and have no cross-iteration dependency, so they are folded into one pass --
    // identical results, without 1.8 KiB of per-invocation private storage.
    const bool farField = omin * omin + pmin * pmin > 4096L;

    for (uint idx = 0u; idx < 25u * 25u; idx++) {
        const int m = int(idx / 25u) - 12;
        const int n = int(idx % 25u) - 12;
        const int64_t o = int64_t(i) + int64_t(m);
        const int64_t p = int64_t(j) + int64_t(n);

        bool hit = math_noise_simplex_sample2d_global(simplex_permutations, double(o), double(p)) < -0.9lf;
        if (!farField) {
            hit = hit && (o * o + p * p > 4096L);
        }
        if (!hit) continue;

        const float g1 = abs(float(o)) * 3439.0;
        const float g2 = abs(float(p)) * 147.0;
        // Both operands are non-negative here, so GLSL's floored mod and C's truncating fmod
        // agree; they would not for a negative dividend.
        const float g = mod(g1 + g2, 13.0) + 9.0;
        const float h = float(k - m * 2);
        const float q = float(l - n * 2);
        float r = 100.0 - sqrt(h * h + q * q) * g;
        r = clamp(r, -100.0, 80.0);
        f = max(f, r);
    }

    return f;
}

int math_roundDownToMultiple(const double a, const int b) {
    return int(math_floor(a / double(b))) * b;
}

uint genShapeCfg_verticalCellBlockCount() {
    return uint(math_biome2block(int(genShapeCfg_verticalSize)));
}

uint genShapeCfg_horizontalCellBlockCount() {
    return uint(math_biome2block(int(genShapeCfg_horizontalSize)));
}

uint math_biome_access_sample(const int64_t theSeed, const int x, const int y, const int z) {
    const int var0 = x - 2;
    const int var1 = y - 2;
    const int var2 = z - 2;
    const int var3 = var0 >> 2;
    const int var4 = var1 >> 2;
    const int var5 = var2 >> 2;
    const double var6 = double(var0 & 3) / 4.0lf;
    const double var7 = double(var1 & 3) / 4.0lf;
    const double var8 = double(var2 & 3) / 4.0lf;

    const uint64_t seed = uint64_t(theSeed);
    const uint64_t MUL = 6364136223846793005ul;
    const uint64_t ADD = 1442695040888963407ul;

    uint var9 = 0u;
    double var10 = 1.7976931348623157E308lf;

    for (uint var11 = 0u; var11 < 8u; ++var11) {
        uint var12 = var11 & 4u;
        uint var13 = var11 & 2u;
        uint var14 = var11 & 1u;
        uint64_t var15 = uint64_t(int64_t(var12 != 0u ? var3 + 1 : var3));
        uint64_t var16 = uint64_t(int64_t(var13 != 0u ? var4 + 1 : var4));
        uint64_t var17 = uint64_t(int64_t(var14 != 0u ? var5 + 1 : var5));
        double var18 = var12 != 0u ? var6 - 1.0lf : var6;
        double var19 = var13 != 0u ? var7 - 1.0lf : var7;
        double var20 = var14 != 0u ? var8 - 1.0lf : var8;

        uint64_t var21 = seed * (seed * MUL + ADD) + var15;
        var21 = var21 * (var21 * MUL + ADD) + var16;
        var21 = var21 * (var21 * MUL + ADD) + var17;
        var21 = var21 * (var21 * MUL + ADD) + var15;
        var21 = var21 * (var21 * MUL + ADD) + var16;
        var21 = var21 * (var21 * MUL + ADD) + var17;
        double var22 = double((var21 >> 24ul) & 1023ul) / 1024.0lf;
        double var23 = (var22 - 0.5lf) * 0.9lf;
        var21 = var21 * (var21 * MUL + ADD) + seed;
        double var24 = double((var21 >> 24ul) & 1023ul) / 1024.0lf;
        double var25 = (var24 - 0.5lf) * 0.9lf;
        var21 = var21 * (var21 * MUL + ADD) + seed;
        double var26 = double((var21 >> 24ul) & 1023ul) / 1024.0lf;
        double var27 = (var26 - 0.5lf) * 0.9lf;

        double var28 = math_square(var20 + var27) + math_square(var19 + var25) + math_square(var18 + var23);
        if (var10 > var28) {
            var9 = var11;
            var10 = var28;
        }
    }

    return var9;
}

layout(buffer_reference, scalar, buffer_reference_align = 4) buffer AquiferDataRef {
    int startX;
    int startY;
    int startZ;
    int sizeX;
    int sizeY;
    int sizeZ;
    int samplingYLowPassCutoff;
    int randomDeriver;
    int posIdx_len;
    int waterLevels;          // aquifer_fluidlevel_t[posIdx]
    int packedBlockPositions; // uint16[posIdx]
};

uint math_aquifer_index_global(uint64_t aquiferAddr, const int x, const int y, const int z) {
    AquiferDataRef aquiferData = AquiferDataRef(aquiferAddr);
    int i = x - aquiferData.startX;
    int j = y - aquiferData.startY;
    int k = z - aquiferData.startZ;
    if (i < 0 || j < 0 || k < 0 || i >= aquiferData.sizeX || j >= aquiferData.sizeY || k >= aquiferData.sizeZ) {
        return 0u;
    }
    return uint((j * aquiferData.sizeZ + k) * aquiferData.sizeX + i);
}

int math_aquifer_unpackPackedX(uint packed)    { return int((packed >> 8u) & 0xFu); }
int math_aquifer_unpackPackedY(uint packed)    { return int((packed >> 4u) & 0xFu); }
int math_aquifer_unpackPackedZ(uint packed)    { return int(packed & 0xFu); }
int math_aquifer_unpackPackedDist(uint64_t packed)   { return int(packed >> 36ul); }
int math_aquifer_unpackPackedPosIdx(uint64_t packed) { return int(packed & 0xFFFFFFFFul); }

void math_aquifer_refreshDistPosIdx_global(uint64_t packedBlockPositionsAddr, out uint64_t res[4],
                                           uint64_t aquiferAddr, const int x, const int y, const int z) {
    U16Ref packedBlockPositions = U16Ref(packedBlockPositionsAddr);

    int gx = (x - 5) >> 4;
    int gy = math_floorDiv(y + 1, 12) - 1;
    int gz = (z - 5) >> 4;

    const uint64_t U64_MAX = 0xFFFFFFFFFFFFFFFFul;
    uint64_t A = U64_MAX;
    uint64_t B = U64_MAX;
    uint64_t C = U64_MAX;
    uint64_t D = U64_MAX;

    uint64_t ps[12];

    uint index = 12u; // subscript must be 32-bit; widened where it is packed
    for (int offY = 0; offY <= 2; ++offY) {
        int gymul = gy * 12 + offY * 12;
        for (int offZ = 0; offZ <= 1; ++offZ) {
            int gzmul = (gz + offZ) << 4;

            uint64_t index0 = uint64_t(index - 1u);
            uint posIdx0 = math_aquifer_index_global(aquiferAddr, gx, gy + offY, gz + offZ);
            uint position0 = uint(packedBlockPositions.v[posIdx0]);
            int dx0 = (gx << 4) + math_aquifer_unpackPackedX(position0) - x;
            int dy0 = gymul + math_aquifer_unpackPackedY(position0) - y;
            int dz0 = gzmul + math_aquifer_unpackPackedZ(position0) - z;
            uint64_t dist_0 = uint64_t(dx0 * dx0 + dy0 * dy0 + dz0 * dz0);

            uint64_t index1 = uint64_t(index - 2u);
            uint posIdx1 = posIdx0 + 1u;
            uint position1 = uint(packedBlockPositions.v[posIdx1]);
            int dx1 = ((gx + 1) << 4) + math_aquifer_unpackPackedX(position1) - x;
            int dy1 = gymul + math_aquifer_unpackPackedY(position1) - y;
            int dz1 = gzmul + math_aquifer_unpackPackedZ(position1) - z;
            uint64_t dist_1 = uint64_t(dx1 * dx1 + dy1 * dy1 + dz1 * dz1);

            ps[12u - index] = (dist_0 << 36ul) | (index0 << 32ul) | uint64_t(posIdx0);
            ps[13u - index] = (dist_1 << 36ul) | (index1 << 32ul) | uint64_t(posIdx1);

            index -= 2u;
        }
    }

    A = ps[0];
    for (uint i = 1u; i < 12u; i++) {
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

double c2me_nan() {
    return packDouble2x32(uvec2(0u, 0x7FF80000u));
}

layout(buffer_reference, scalar, buffer_reference_align = 4) buffer WorldgenParamsRef {
    // cache size is (size + 1) to account for interpolation
    int startBiomeX;
    int startBiomeZ;
    int sizeBiomeX;
    int sizeBiomeZ;

    // cache size is (size + 1) to account for interpolation
    int startCellX;
    int startCellY;
    int startCellZ;
    int sizeCellX;
    int sizeCellY;
    int sizeCellZ;

    // cache size is actually size
    int estimateSurfaceHeight_startBiomeX;
    int estimateSurfaceHeight_startBiomeZ;
    int estimateSurfaceHeight_sizeBiomeX;
    int estimateSurfaceHeight_sizeBiomeZ;

    // cache size is actually size
    int cache2d_startX;
    int cache2d_startZ;
    int cache2d_sizeX;
    int cache2d_sizeZ;

    int offset_estimateSurfaceHeight;
    int genConfig_defaultBlock;
    int genConfig_defaultFluid;
    int offset_aquifer;
    int offset_fluidLevelSampler;
    int offset_oreVeinRandom;
};

struct interpolation_pos_t {
    int cellRelX;
    int cellRelY;
    int cellRelZ;
    int cellBlockX;
    int cellBlockY;
    int cellBlockZ;
};

interpolation_pos_t df_get_interpolation_pos(uint64_t paramsAddr, const int x, const int y, const int z) {
    WorldgenParamsRef params = WorldgenParamsRef(paramsAddr);
    int cellRelX = math_floorDiv(x, int(genShapeCfg_horizontalCellBlockCount())) - params.startCellX;
    int cellRelY = math_floorDiv(y, int(genShapeCfg_verticalCellBlockCount())) - params.startCellY;
    int cellRelZ = math_floorDiv(z, int(genShapeCfg_horizontalCellBlockCount())) - params.startCellZ;
    int cellBlockX = math_floorMod(x, int(genShapeCfg_horizontalCellBlockCount()));
    int cellBlockY = math_floorMod(y, int(genShapeCfg_verticalCellBlockCount()));
    int cellBlockZ = math_floorMod(z, int(genShapeCfg_horizontalCellBlockCount()));

    if (cellRelX < 0 || cellRelY < 0 || cellRelZ < 0
            || cellRelX >= params.sizeCellX || cellRelY >= params.sizeCellY || cellRelZ >= params.sizeCellY
            || cellBlockX < 0 || cellBlockY < 0 || cellBlockZ < 0) {
        return interpolation_pos_t(0, 0, 0, 0, 0, 0);
    }
    return interpolation_pos_t(cellRelX, cellRelY, cellRelZ, cellBlockX, cellBlockY, cellBlockZ);
}

uint df_address_flatcache_buffer(uint64_t paramsAddr, const uint cacheIndex, const uint offsetX, const uint offsetZ) {
    WorldgenParamsRef params = WorldgenParamsRef(paramsAddr);
    if (offsetX > uint(params.sizeBiomeX) || offsetZ > uint(params.sizeBiomeZ)) {
        return 0u;
    }
    return ((cacheIndex) * uint(params.sizeBiomeX + 1) + offsetX) * uint(params.sizeBiomeZ + 1) + offsetZ;
}

uint df_address_cache2d_buffer(uint64_t paramsAddr, const uint cacheIndex, const uint offsetX, const uint offsetZ) {
    WorldgenParamsRef params = WorldgenParamsRef(paramsAddr);
    if (offsetX >= uint(params.cache2d_sizeX) || offsetZ >= uint(params.cache2d_sizeZ)) {
        return 0u;
    }
    return ((cacheIndex) * uint(params.cache2d_sizeX) + offsetX) * uint(params.cache2d_sizeZ) + offsetZ;
}

uint df_address_interpolator_buffer(uint64_t paramsAddr, const uint cacheIndex,
                                    const int cellX, const int cellY, const int cellZ) {
    WorldgenParamsRef params = WorldgenParamsRef(paramsAddr);
    if (cellX < 0 || cellY < 0 || cellZ < 0
            || cellX > params.sizeCellX || cellY > params.sizeCellY || cellZ > params.sizeCellZ) {
        return 0u;
    }
    return uint(((int(cacheIndex) * (params.sizeCellX + 1) + cellX) * (params.sizeCellY + 1) + cellY)
            * (params.sizeCellZ + 1) + cellZ);
}

cache_result_t df_cachelike_interpolator(uint64_t paramsAddr, uint64_t interpolatorAddr, const uint cacheIndex,
                                         const int x, const int y, const int z, const uint interpolationState) {
    if (paramsAddr == 0ul || (interpolationState & MASK_enableAllCaches) != MASK_enableAllCaches) {
        return cache_result_t(false, c2me_nan());
    }
    F64Ref buf = F64Ref(interpolatorAddr);
    const interpolation_pos_t pos = df_get_interpolation_pos(paramsAddr, x, y, z);
    const double res = math_lerp3(
        double(pos.cellBlockX) / double(genShapeCfg_horizontalCellBlockCount()),
        double(pos.cellBlockY) / double(genShapeCfg_verticalCellBlockCount()),
        double(pos.cellBlockZ) / double(genShapeCfg_horizontalCellBlockCount()),
        buf.v[df_address_interpolator_buffer(paramsAddr, cacheIndex, pos.cellRelX,     pos.cellRelY,     pos.cellRelZ)],
        buf.v[df_address_interpolator_buffer(paramsAddr, cacheIndex, pos.cellRelX + 1, pos.cellRelY,     pos.cellRelZ)],
        buf.v[df_address_interpolator_buffer(paramsAddr, cacheIndex, pos.cellRelX,     pos.cellRelY + 1, pos.cellRelZ)],
        buf.v[df_address_interpolator_buffer(paramsAddr, cacheIndex, pos.cellRelX + 1, pos.cellRelY + 1, pos.cellRelZ)],
        buf.v[df_address_interpolator_buffer(paramsAddr, cacheIndex, pos.cellRelX,     pos.cellRelY,     pos.cellRelZ + 1)],
        buf.v[df_address_interpolator_buffer(paramsAddr, cacheIndex, pos.cellRelX + 1, pos.cellRelY,     pos.cellRelZ + 1)],
        buf.v[df_address_interpolator_buffer(paramsAddr, cacheIndex, pos.cellRelX,     pos.cellRelY + 1, pos.cellRelZ + 1)],
        buf.v[df_address_interpolator_buffer(paramsAddr, cacheIndex, pos.cellRelX + 1, pos.cellRelY + 1, pos.cellRelZ + 1)]
    );
    return cache_result_t(true, res);
}

cache_result_t df_cachelike_flatcache(uint64_t paramsAddr, uint64_t dataAddr, const uint cacheIndex,
                                      const int x, const int y, const int z, const uint interpolationState) {
    if (paramsAddr == 0ul || (interpolationState & MASK_enableFlatCache) != MASK_enableFlatCache) {
        return cache_result_t(false, c2me_nan());
    }
    WorldgenParamsRef params = WorldgenParamsRef(paramsAddr);
    const int offsetX = math_block2biome(x) - params.startBiomeX;
    const int offsetZ = math_block2biome(z) - params.startBiomeZ;
    if (offsetX >= 0 && offsetZ >= 0 && offsetX <= params.sizeBiomeX && offsetZ <= params.sizeBiomeZ) {
        const double res = F64Ref(dataAddr).v[df_address_flatcache_buffer(paramsAddr, cacheIndex, uint(offsetX), uint(offsetZ))];
        return cache_result_t(true, res);
    }
    return cache_result_t(false, c2me_nan());
}

cache_result_t df_cachelike_cache2d(uint64_t paramsAddr, uint64_t dataAddr, const uint cacheIndex,
                                    const int x, const int y, const int z, const uint interpolationState) {
    if (paramsAddr == 0ul || (interpolationState & MASK_enableAllCaches) != MASK_enableAllCaches) {
        return cache_result_t(false, c2me_nan());
    }
    WorldgenParamsRef params = WorldgenParamsRef(paramsAddr);
    const int offsetX = x - params.cache2d_startX;
    const int offsetZ = z - params.cache2d_startZ;
    if (offsetX >= 0 && offsetZ >= 0 && offsetX < params.cache2d_sizeX && offsetZ < params.cache2d_sizeZ) {
        const double res = F64Ref(dataAddr).v[df_address_cache2d_buffer(paramsAddr, cacheIndex, uint(offsetX), uint(offsetZ))];
        return cache_result_t(true, res);
    }
    return cache_result_t(false, c2me_nan());
}

double df_caveScaler_scaleCaves(const double value) {
    if (value < -0.75lf) {
        return 0.5lf;
    } else if (value < -0.5lf) {
        return 0.75lf;
    } else if (value < 0.5lf) {
        return 1.0lf;
    } else {
        return value < 0.75lf ? 2.0lf : 3.0lf;
    }
}

double df_caveScaler_scaleTunnels(const double value) {
    if (value < -0.5lf) {
        return 0.75lf;
    } else if (value < 0.0lf) {
        return 1.0lf;
    } else {
        return value < 0.5lf ? 1.5lf : 2.0lf;
    }
}

int df_spline_findRangeForLocation(uint64_t locationsAddr, const uint locations_len, const float x) {
    F32Ref locations = F32Ref(locationsAddr);
    int minIdx = 0;
    int i = int(locations_len);

    while (i > 0) {
        int j = i / 2;
        int k = minIdx + j;
        if (x < locations.v[k]) {
            i = j;
        } else {
            minIdx = k + 1;
            i -= j + 1;
        }
    }

    return minIdx - 1;
}

float df_spline_sampleOutsideRange(const float point, uint64_t locationsAddr, const float value,
                                   uint64_t derivativesAddr, const int i) {
    float f = F32Ref(derivativesAddr).v[i];
    return f == 0.0 ? value : value + f * (point - F32Ref(locationsAddr).v[i]);
}

const int SWSTA_NONE        = 0;
const int SWSTA_BURY        = 1;
const int SWSTA_BEARD_THIN  = 2;
const int SWSTA_BEARD_BOX   = 3;
const int SWSTA_ENCAPSULATE = 4;

layout(buffer_reference, scalar, buffer_reference_align = 4) buffer SwsIndexRef {
    int  startX;   // chunk pos
    int  startZ;
    uint sizeX;
    uint sizeZ;
};
const uint SWS_INDEX_SIZE = 16u;

layout(buffer_reference, scalar, buffer_reference_align = 4) buffer SwsDataRef {
    uint pieceLength;
    int  boxStartX;
    int  boxStartY;
    int  boxStartZ;
    int  boxEndX;
    int  boxEndY;
    int  boxEndZ;
    int  groundLevelDelta;
    int  terrainAdjustment;

    uint funcLength;
    int  sourceX;
    int  sourceGroundY;
    int  sourceZ;

    int  affectedBox_startX;
    int  affectedBox_startY;
    int  affectedBox_startZ;
    int  affectedBox_endX;
    int  affectedBox_endY;
    int  affectedBox_endZ;
};

double c2me_sws_getMagnitudeWeight(const double x, const double y, const double z) {
    double d = sqrt(x * x + y * y + z * z);
    if (d > 6.0lf) {
        return 0.0lf;
    } else {
        return 1.0lf - d / 6.0lf;
    }
}

double math_fastInverseSqrt(double a) {
    uvec2 u = unpackDouble2x32(a);
    uint64_t l = (uint64_t(u.y) << 32ul) | uint64_t(u.x);
    double d = 0.5lf * a;
    l = 6910469410427058090ul - (l >> 1ul);
    double x = packDouble2x32(uvec2(uint(l & 0xFFFFFFFFul), uint(l >> 32ul)));
    return x * (1.5lf - d * x * x);
}

double c2me_sws_getStructureWeight(uint64_t tableAddr, const double x, const double y, const double z,
                                   const double yy) {
    int i = int(x) + 12;
    int j = int(y) + 12;
    int k = int(z) + 12;
    if (i >= 0 && i < 24 && j >= 0 && j < 24 && k >= 0 && k < 24) {
        double d = yy + 0.5lf;
        double e = (x * x) + (d * d) + (z * z);
        double f = -d * math_fastInverseSqrt(e / 2.0lf) / 2.0lf;
        return f * double(F32Ref(tableAddr).v[k * 24 * 24 + i * 24 + j]);
    } else {
        return 0.0lf;
    }
}

double df_structureWeightSampler_sample(uint64_t tableAddr, uint64_t indexAddr,
                                        const int x, const int y, const int z) {
    SwsIndexRef data_index = SwsIndexRef(indexAddr);

    const int chunkX = x >> 4;
    const int chunkZ = z >> 4;
    const uint dataRelX = uint(clamp(chunkX - data_index.startX, 0, int(data_index.sizeX) - 1));
    const uint dataRelZ = uint(clamp(chunkZ - data_index.startZ, 0, int(data_index.sizeZ) - 1));

    U32Ref sws_data_offsets = U32Ref(indexAddr + uint64_t(SWS_INDEX_SIZE));
    const uint sws_current_offset = sws_data_offsets.v[dataRelX * data_index.sizeZ + dataRelZ];

    if (sws_current_offset == 0u) return 0.0lf;

    const uint64_t dataAddr = indexAddr + uint64_t(sws_current_offset);
    SwsDataRef data = SwsDataRef(dataAddr);

    if (x < data.affectedBox_startX || x > data.affectedBox_endX ||
        y < data.affectedBox_startY || y > data.affectedBox_endY ||
        z < data.affectedBox_startZ || z > data.affectedBox_endZ) {
        return 0.0lf;
    }

    I32Ref boxStartX         = I32Ref(dataAddr + uint64_t(data.boxStartX));
    I32Ref boxStartY         = I32Ref(dataAddr + uint64_t(data.boxStartY));
    I32Ref boxStartZ         = I32Ref(dataAddr + uint64_t(data.boxStartZ));
    I32Ref boxEndX           = I32Ref(dataAddr + uint64_t(data.boxEndX));
    I32Ref boxEndY           = I32Ref(dataAddr + uint64_t(data.boxEndY));
    I32Ref boxEndZ           = I32Ref(dataAddr + uint64_t(data.boxEndZ));
    I32Ref groundLevelDelta  = I32Ref(dataAddr + uint64_t(data.groundLevelDelta));
    I32Ref terrainAdjustment = I32Ref(dataAddr + uint64_t(data.terrainAdjustment));

    I32Ref sourceX           = I32Ref(dataAddr + uint64_t(data.sourceX));
    I32Ref sourceGroundY     = I32Ref(dataAddr + uint64_t(data.sourceGroundY));
    I32Ref sourceZ           = I32Ref(dataAddr + uint64_t(data.sourceZ));

    double d = 0.0lf;

    for (uint i = 0u; i < data.pieceLength; i++) {
        int m = max(0, max(boxStartX.v[i] - x, x - boxEndX.v[i]));
        int n = max(0, max(boxStartZ.v[i] - z, z - boxEndZ.v[i]));
        int o = boxStartY.v[i] + groundLevelDelta.v[i];
        int pp = y - o;

        int adjustment = terrainAdjustment.v[i];
        if (adjustment == SWSTA_NONE) {
            d += 0.0lf;
        } else if (adjustment == SWSTA_BURY) {
            d += c2me_sws_getMagnitudeWeight(double(m), double(pp) / 2.0lf, double(n));
        } else if (adjustment == SWSTA_BEARD_THIN) {
            d += c2me_sws_getStructureWeight(tableAddr, double(m), double(pp), double(n), double(pp)) * 0.8lf;
        } else if (adjustment == SWSTA_BEARD_BOX) {
            d += c2me_sws_getStructureWeight(tableAddr, double(m),
                    double(max(0, max(o - y, y - boxEndY.v[i]))), double(n), double(pp)) * 0.8lf;
        } else if (adjustment == SWSTA_ENCAPSULATE) {
            d += c2me_sws_getMagnitudeWeight(double(m) / 2.0lf,
                    double(max(0, max(boxStartY.v[i] - y, y - boxEndY.v[i]))) / 2.0lf,
                    double(n) / 2.0lf) * 0.8lf;
        } else {
            // The OpenCL backend trapped on an unknown adjustment; NaN propagates instead.
            return c2me_nan();
        }
    }

    for (uint i = 0u; i < data.funcLength; i++) {
        int r = x - sourceX.v[i];
        int l = y - sourceGroundY.v[i];
        int m = z - sourceZ.v[i];
        d += c2me_sws_getStructureWeight(tableAddr, double(r), double(l), double(m), double(l)) * 0.4lf;
    }

    return d;
}


double c2me_helper_keepalive(sample_int32_ctx_t ctx) {
    double acc = 0.0lf;
    acc += math_clampedMap(double(ctx.y), 0.0lf, 1.0lf, 0.0lf, 1.0lf);
    acc += double(math_lerpf(0.5, 0.0, 1.0));
    acc += double(df_data_offset_global(ctx.rw_data, 0) & 1ul);
    acc += double(df_spline_findRangeForLocation(ctx.const_data, 1u, 0.0));
    acc += double(df_spline_sampleOutsideRange(0.0, ctx.const_data, 0.0, ctx.const_data, 0));
    acc += df_cachelike_interpolator(ctx.rw_data, ctx.rw_data, 0u, ctx.x, ctx.y, ctx.z, ctx.sample_flags).res;
    acc += df_cachelike_flatcache(ctx.rw_data, ctx.rw_data, 0u, ctx.x, ctx.y, ctx.z, ctx.sample_flags).res;
    acc += df_cachelike_cache2d(ctx.rw_data, ctx.rw_data, 0u, ctx.x, ctx.y, ctx.z, ctx.sample_flags).res;
    acc += math_noise_perlin_double_octave_sample_global_noinline(ctx.const_data, 0.0lf, 0.0lf, 0.0lf);
    acc += math_noise_perlin_interpolated_sample_global_noinline(ctx.const_data, 0.0lf, 0.0lf, 0.0lf);
    acc += double(math_end_islands_sample_global(ctx.const_data, ctx.x, ctx.z));
    acc += df_structureWeightSampler_sample(ctx.const_data, ctx.rw_data, ctx.x, ctx.y, ctx.z);
    acc += math_fastInverseSqrt(2.0lf) + df_caveScaler_scaleCaves(0.0lf) + df_caveScaler_scaleTunnels(0.0lf);
    acc += math_lerp3(0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf);
    acc += math_clampedLerpFromProgress(0.0lf, 0.0lf, 1.0lf, 0.0lf, 1.0lf);
    acc += math_octave_maintainPrecision(1.0lf) + math_square(2.0lf) + math_perlinFade(0.5lf);
    acc += math_simplex_grad(0, 0.0lf, 0.0lf, 0.0lf, 1.0lf);
    acc += c2me_math_simplex_grad(0, 0.0lf, 0.0lf, 0.0lf, 1.0lf);
    acc += math_noise_simplex_sample2d_global(ctx.const_data, 0.0lf, 0.0lf);
    acc += double(math_floorMod(ctx.x, 4) + math_biome2block(1) + math_block2biome(4));
    acc += double(math_rotateLeftU64(1ul, 3ul) & 1ul);
    acc += double(c2me_math_simplex_map_global(ctx.const_data, 0));
    acc += double(math_biome_access_sample(1L, ctx.x, ctx.y, ctx.z));
    return acc;
}

#define DF_BINDING_STUB(name)                                                                        \
    double df_binding_##name##_uncached(sample_int32_ctx_t ctx)       { return c2me_helper_keepalive(ctx); } \
    double df_binding_##name##_flatcache_only(sample_int32_ctx_t ctx) { return c2me_helper_keepalive(ctx); } \
    double df_binding_##name##_fully_cached(sample_int32_ctx_t ctx)   { return c2me_helper_keepalive(ctx); } \
    double df_binding_##name(sample_int32_ctx_t ctx)                  { return c2me_helper_keepalive(ctx); }

DF_BINDING_STUB(barrier)
DF_BINDING_STUB(fluid_level_floodedness)
DF_BINDING_STUB(fluid_level_spread)
DF_BINDING_STUB(lava)
DF_BINDING_STUB(temperature)
DF_BINDING_STUB(vegetation)
DF_BINDING_STUB(continents)
DF_BINDING_STUB(erosion)
DF_BINDING_STUB(depth)
DF_BINDING_STUB(ridges)
DF_BINDING_STUB(preliminary_surface_level)
DF_BINDING_STUB(final_density)
DF_BINDING_STUB(vein_toggle)
DF_BINDING_STUB(vein_ridged)
DF_BINDING_STUB(vein_gap)
DF_BINDING_STUB(final_final_density)

const uint64_t RANDOM_Checked             = 0ul;
const uint64_t RANDOM_Xoroshiro128PlusPlus = 1ul;

struct random_state_t {
    uint64_t type;
    uint64_t seedLo;
    uint64_t seedHi;
};

int64_t math_hashCode_int32x3(int x, int y, int z) {
    // x * 3129871 is a 32-bit multiply that wraps before being widened.
    uint64_t l = uint64_t(int64_t(int(uint(x) * 3129871u)))
               ^ uint64_t(int64_t(z) * 116129781L)
               ^ uint64_t(int64_t(y));
    l = l * l * 42317861ul + l * 11ul;
    return int64_t(l) >> 16; // arithmetic
}

uint64_t math_mixStafford13(uint64_t seed) {
    seed = (seed ^ (seed >> 30ul)) * uint64_t(-4658895280553007687L);
    seed = (seed ^ (seed >> 27ul)) * uint64_t(-7723592293110705685L);
    return seed ^ (seed >> 31ul);
}

void random_state_set_seed(inout random_state_t state, int64_t seed) {
    if (state.type == RANDOM_Checked) {
        state.seedLo = (uint64_t(seed) ^ 25214903917ul) & 281474976710655ul;
    } else if (state.type == RANDOM_Xoroshiro128PlusPlus) {
        state.seedLo = uint64_t(seed) ^ 7640891576956012809ul;
        state.seedHi = state.seedLo + uint64_t(-7046029254386353131L);
        state.seedLo = math_mixStafford13(state.seedLo);
        state.seedHi = math_mixStafford13(state.seedHi);
    }
}

void random_state_split_coords(inout random_state_t state, int x, int y, int z) {
    if (state.type == RANDOM_Checked) {
        state.seedLo ^= uint64_t(math_hashCode_int32x3(x, y, z));
        random_state_set_seed(state, int64_t(state.seedLo));
    } else if (state.type == RANDOM_Xoroshiro128PlusPlus) {
        state.seedLo ^= uint64_t(math_hashCode_int32x3(x, y, z));
        if ((state.seedLo | state.seedHi) == 0ul) {
            state.seedLo = uint64_t(-7046029254386353131L);
            state.seedHi = 7640891576956012809ul;
        }
    }
}

int random_state_Checked_next(inout random_state_t state, int bits) {
    int m = int(state.seedLo * 25214903917ul + 11ul & 281474976710655ul);
    state.seedLo = uint64_t(int64_t(m)); // sign-extended, as the C assignment does
    return m >> (48 - bits);
}

int64_t random_state_Xoroshiro128PlusPlus_next0(inout random_state_t state) {
    uint64_t l = state.seedLo;
    uint64_t m = state.seedHi;
    uint64_t n = math_rotateLeftU64(l + m, 17ul) + l;
    m ^= l;
    state.seedLo = math_rotateLeftU64(l, 49ul) ^ m ^ (m << 21ul);
    state.seedHi = math_rotateLeftU64(m, 28ul);
    return int64_t(n);
}

int64_t random_state_Xoroshiro128PlusPlus_next(inout random_state_t state, int bits) {
    return int64_t(uint64_t(random_state_Xoroshiro128PlusPlus_next0(state)) >> uint64_t(64 - bits));
}

float random_state_nextFloat(inout random_state_t state) {
    if (state.type == RANDOM_Checked) {
        return float(random_state_Checked_next(state, 24)) * 5.9604645E-8;
    } else if (state.type == RANDOM_Xoroshiro128PlusPlus) {
        return float(random_state_Xoroshiro128PlusPlus_next(state, 24)) * 5.9604645E-8;
    }
    return c2me_nanf();
}

int random_state_nextIntBounded(inout random_state_t state, int bound) {
    if (bound <= 0) {
        return 0; // the OpenCL backend trapped
    }
    if (state.type == RANDOM_Checked) {
        if ((bound & (bound - 1)) == 0) {
            return int((int64_t(bound) * int64_t(random_state_Checked_next(state, 31))) >> 31);
        }
        int i;
        int j;
        do {
            i = random_state_Checked_next(state, 31);
            j = i - (i / bound) * bound; // GLSL leaves the sign of % undefined
        } while (i - j + (bound - 1) < 0);
        return j;
    } else if (state.type == RANDOM_Xoroshiro128PlusPlus) {
        int64_t l = int64_t(uint64_t(uint(random_state_Xoroshiro128PlusPlus_next0(state))));
        int64_t m = l * int64_t(bound);
        int64_t n = m & 4294967295L;
        if (n < int64_t(bound)) {
            uint threshold = (uint(~bound) + 1u) % uint(bound);
            while (n < int64_t(threshold)) {
                l = int64_t(uint64_t(uint(random_state_Xoroshiro128PlusPlus_next0(state))));
                m = l * int64_t(bound);
                n = m & 4294967295L;
            }
        }
        return int(m >> 32);
    }
    return 0;
}

const int BLOCK_NULL               = 0;
const int BLOCK_AIR                = 1;
const int BLOCK_DEFAULT_BLOCK      = 2;
const int BLOCK_WATER              = 3;
const int BLOCK_LAVA               = 4;
const int BLOCK_COPPER_ORE         = 5;
const int BLOCK_RAW_COPPER_BLOCK   = 6;
const int BLOCK_GRANITE            = 7;
const int BLOCK_DEEPSLATE_IRON_ORE = 8;
const int BLOCK_RAW_IRON_BLOCK     = 9;
const int BLOCK_TUFF               = 10;

layout(buffer_reference, scalar, buffer_reference_align = 4) buffer AquiferFluidLevelRef {
    int y;
    int blockState;
};
const uint AQUIFER_FLUIDLEVEL_SIZE = 8u;

int aquifer_fluidlevel_getBlockState_ptr_global(uint64_t dataAddr, const int y) {
    AquiferFluidLevelRef data = AquiferFluidLevelRef(dataAddr);
    return y < data.y ? data.blockState : BLOCK_AIR;
}

int aquifer_fluidlevel_equals_global(uint64_t data0Addr, uint64_t data1Addr) {
    AquiferFluidLevelRef d0 = AquiferFluidLevelRef(data0Addr);
    AquiferFluidLevelRef d1 = AquiferFluidLevelRef(data1Addr);
    return (d0.y == d1.y && d0.blockState == d1.blockState) ? 1 : 0;
}

const int c2me_aquifer_chunkPosOffset[26] = int[26](
    0, 0,  -2, -1,  -1, -1,  0, -1,  1, -1,  -3, 0,  -2, 0,
    -1, 0,  1, 0,  -2, 1,  -1, 1,  0, 1,  1, 1
);

int c2me_params_startBiomeX(uint64_t p)    { return WorldgenParamsRef(p).startBiomeX; }
int c2me_params_startBiomeZ(uint64_t p)    { return WorldgenParamsRef(p).startBiomeZ; }
int c2me_params_startCellX(uint64_t p)     { return WorldgenParamsRef(p).startCellX; }
int c2me_params_startCellY(uint64_t p)     { return WorldgenParamsRef(p).startCellY; }
int c2me_params_startCellZ(uint64_t p)     { return WorldgenParamsRef(p).startCellZ; }
int c2me_params_cache2d_startX(uint64_t p) { return WorldgenParamsRef(p).cache2d_startX; }
int c2me_params_cache2d_startZ(uint64_t p) { return WorldgenParamsRef(p).cache2d_startZ; }

sample_int32_ctx_t make_sample_int32_ctx(uint64_t const_data, uint64_t rw_data,
                                         const int x, const int y, const int z, const uint sample_flags) {
    return sample_int32_ctx_t(const_data, rw_data, x, y, z, sample_flags);
}

int chunkNoiseSampler_estimateSurfaceHeight0(uint64_t const_data, uint64_t rw_data,
                                             const int blockX, const int blockZ) {
    return int(math_floor(df_binding_preliminary_surface_level(
            make_sample_int32_ctx(const_data, rw_data, blockX, 0, blockZ, 0u))));
}

int chunkNoiseSampler_estimateSurfaceHeight(uint64_t const_data, uint64_t rw_data,
                                            const int blockX, const int blockZ) {
    WorldgenParamsRef params = WorldgenParamsRef(rw_data);
    uint64_t cache = rw_data + uint64_t(params.offset_estimateSurfaceHeight);
    int biomeX = math_block2biome(blockX);
    int biomeZ = math_block2biome(blockZ);
    int relX = biomeX - params.estimateSurfaceHeight_startBiomeX;
    int relZ = biomeZ - params.estimateSurfaceHeight_startBiomeZ;
    if (params.offset_estimateSurfaceHeight == 0 || relX < 0 || relZ < 0
            || relX >= params.estimateSurfaceHeight_sizeBiomeX
            || relZ >= params.estimateSurfaceHeight_sizeBiomeZ) {
        // Uncached region. The OpenCL backend trapped here rather than falling back to the slow
        // path; there is no Vulkan trap, so 0 is returned.
        return 0;
    }
    return I32Ref(cache).v[relX * params.estimateSurfaceHeight_sizeBiomeZ + relZ];
}

layout(buffer_reference, scalar, buffer_reference_align = 8) buffer RandomStateRef {
    uint64_t type;
    uint64_t seedLo;
    uint64_t seedHi;
};

const int DimensionType_field_35479 = -32512; // copied from debugger

uint64_t fluidLevelSampler_getFluidLevel_ptr(uint64_t rw_data, const int y) {
    WorldgenParamsRef params = WorldgenParamsRef(rw_data);
    uint64_t fluidLevels = rw_data + uint64_t(params.offset_fluidLevelSampler);
    const int relY = y - genShapeCfg_minimumY;
    return fluidLevels + uint64_t(clamp(relY, 0, genShapeCfg_height - 1)) * uint64_t(AQUIFER_FLUIDLEVEL_SIZE);
}

bool math_VanillaBiomeParameters_inDeepDarkParameters(const sample_int32_ctx_t ctx) {
    return df_binding_erosion(ctx) < -0.225lf && df_binding_depth(ctx) > 0.9lf;
}

int c2me_aquifer_getNoiseBasedFluidLevel(uint64_t const_data, int blockX, int blockY, int blockZ,
                                         int surfaceHeightEstimate) {
    int k = blockX >> 4;
    int l = math_floorDiv(blockY, 40);
    int m = blockZ >> 4;
    int n = l * 40 + 20;
    double d = df_binding_fluid_level_spread(make_sample_int32_ctx(const_data, 0ul, k, l, m, 0u)) * 10.0lf;
    int pp = math_roundDownToMultiple(d, 3);
    int q = n + pp;
    return min(surfaceHeightEstimate, q);
}

int c2me_aquifer_getFluidBlockY(uint64_t const_data, int blockX, int blockY, int blockZ,
                                uint64_t defaultFluidLevel, int surfaceHeightEstimate, bool bl) {
    const sample_int32_ctx_t unblendedNoisePos =
            make_sample_int32_ctx(const_data, 0ul, blockX, blockY, blockZ, 0u);
    double d;
    double e;
    if (math_VanillaBiomeParameters_inDeepDarkParameters(unblendedNoisePos)) {
        d = -1.0lf;
        e = -1.0lf;
    } else {
        int i = surfaceHeightEstimate + 8 - blockY;
        double f = bl ? math_clampedLerp(1.0lf, 0.0lf, double(i) / 64.0lf) : 0.0lf;
        double g = clamp(df_binding_fluid_level_floodedness(unblendedNoisePos), -1.0lf, 1.0lf);
        d = g + 0.8lf + (f - 1.0lf) * 1.2lf;
        e = g + 0.3lf + (f - 1.0lf) * 1.1lf;
    }

    if (e > 0.0lf) {
        return AquiferFluidLevelRef(defaultFluidLevel).y;
    } else if (d > 0.0lf) {
        return c2me_aquifer_getNoiseBasedFluidLevel(const_data, blockX, blockY, blockZ, surfaceHeightEstimate);
    }
    return DimensionType_field_35479;
}

int c2me_aquifer_getFluidBlockState(uint64_t const_data, int blockX, int blockY, int blockZ,
                                    uint64_t defaultFluidLevel, int fluidLevel) {
    int blockState = AquiferFluidLevelRef(defaultFluidLevel).blockState;
    if (fluidLevel <= -10 && fluidLevel != DimensionType_field_35479 && blockState != BLOCK_LAVA) {
        int k = blockX >> 6;
        int l = math_floorDiv(blockY, 40);
        int m = blockZ >> 6;
        double d = df_binding_lava(make_sample_int32_ctx(const_data, 0ul, k, l, m, 0u));
        if (abs(d) > 0.3lf) {
            blockState = BLOCK_LAVA;
        }
    }
    return blockState;
}

void aquifer_data_prefill(uint64_t const_data, uint64_t rw_data, int gx, int gy, int gz) {
    WorldgenParamsRef params = WorldgenParamsRef(rw_data);
    if (params.offset_aquifer == 0) {
        return; // the OpenCL backend trapped
    }

    uint64_t dataAddr = rw_data + uint64_t(params.offset_aquifer);
    AquiferDataRef data = AquiferDataRef(dataAddr);

    RandomStateRef randomDeriver = RandomStateRef(dataAddr + uint64_t(data.randomDeriver));
    uint64_t waterLevels = dataAddr + uint64_t(data.waterLevels);
    U16Ref packedBlockPositions = U16Ref(dataAddr + uint64_t(data.packedBlockPositions));

    const int curX = data.startX + gx;
    const int curY = data.startY + gy;
    const int curZ = data.startZ + gz;

    random_state_t derived;
    derived.type = randomDeriver.type;
    derived.seedLo = randomDeriver.seedLo;
    derived.seedHi = randomDeriver.seedHi;
    random_state_split_coords(derived, curX, curY, curZ);
    int r0 = random_state_nextIntBounded(derived, 10);
    int r1 = random_state_nextIntBounded(derived, 9);
    int r2 = random_state_nextIntBounded(derived, 10);
    int blockX = curX * 16 + r0;
    int blockY = curY * 12 + r1;
    int blockZ = curZ * 16 + r2;
    uint index = math_aquifer_index_global(dataAddr, curX, curY, curZ);
    packedBlockPositions.v[index] = uint16_t((r0 << 8) | (r1 << 4) | r2);

    uint64_t waterLevelHere = waterLevels + uint64_t(index) * uint64_t(AQUIFER_FLUIDLEVEL_SIZE);

    int i = 0x7FFFFFFF;
    int j = blockY + 12;
    int k = blockY - 12;
    bool bl = false;

    uint64_t fluidLevel = fluidLevelSampler_getFluidLevel_ptr(rw_data, blockY);
    for (uint idx = 0u; idx < 13u; idx++) {
        const int offX = c2me_aquifer_chunkPosOffset[idx * 2u + 0u];
        const int offZ = c2me_aquifer_chunkPosOffset[idx * 2u + 1u];
        int l = blockX + (offX << 4);
        int m = blockZ + (offZ << 4);
        int n = chunkNoiseSampler_estimateSurfaceHeight(const_data, rw_data, l, m);
        int o = n + 8;
        bool bl2 = offX == 0 && offZ == 0;
        if (bl2 && k > o) {
            AquiferFluidLevelRef(waterLevelHere).y = AquiferFluidLevelRef(fluidLevel).y;
            AquiferFluidLevelRef(waterLevelHere).blockState = AquiferFluidLevelRef(fluidLevel).blockState;
            return;
        }

        bool bl3 = j > o;
        if (bl3 || bl2) {
            uint64_t fluidLevel2 = fluidLevelSampler_getFluidLevel_ptr(rw_data, o);
            if (aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel2, o) != BLOCK_AIR) {
                if (bl2) {
                    bl = true;
                }
                if (bl3) {
                    AquiferFluidLevelRef(waterLevelHere).y = AquiferFluidLevelRef(fluidLevel2).y;
                    AquiferFluidLevelRef(waterLevelHere).blockState = AquiferFluidLevelRef(fluidLevel2).blockState;
                    return;
                }
            }
        }

        i = min(i, n);
    }

    int pp = c2me_aquifer_getFluidBlockY(const_data, blockX, blockY, blockZ, fluidLevel, i, bl);
    AquiferFluidLevelRef(waterLevelHere).y = pp;
    AquiferFluidLevelRef(waterLevelHere).blockState =
            c2me_aquifer_getFluidBlockState(const_data, blockX, blockY, blockZ, fluidLevel, pp);
}

struct aquifer_result_t {
    int blockState;
    bool needsFluidTick;
};

double math_aquifer_maxDistance(int i, int a) {
    return 1.0lf - double(abs(a - i)) / 25.0lf;
}

// = maxDistance(square(10), square(12)) = -0.76
const double aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD = -0.76lf;

double c2me_aquifer_getQ(const double i, const double d, const double j) {
    double e = i + 0.5lf - d;
    double f = j / 2.0lf;
    double o = f - abs(e);
    double q;
    if (e > 0.0lf) {
        q = o > 0.0lf ? o / 1.5lf : o / 2.5lf;
    } else {
        double pp = 3.0lf + o;
        q = pp > 0.0lf ? pp / 3.0lf : pp / 10.0lf;
    }
    return q;
}

double c2me_aquifer_postCalculateDensityModified(const sample_int32_ctx_t ctx, const double q,
                                                 inout double mutableDoubleThingy) {
    double r;
    if (!(q < -2.0lf) && !(q > 2.0lf)) {
        double sv = mutableDoubleThingy;
        if (isnan(sv)) {
            double t = df_binding_barrier(ctx);
            mutableDoubleThingy = t;
            r = t;
        } else {
            r = sv;
        }
    } else {
        r = 0.0lf;
    }
    return 2.0lf * (r + q);
}

double c2me_aquifer_calculateDensityModified(const sample_int32_ctx_t ctx, uint64_t fluidLevel,
                                             uint64_t fluidLevel2, inout double mutableDoubleThingy) {
    int i = ctx.y;
    int blockState = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel, i);
    int blockState2 = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel2, i);
    if ((blockState != BLOCK_LAVA || blockState2 != BLOCK_WATER)
            && (blockState != BLOCK_WATER || blockState2 != BLOCK_LAVA)) {
        int y0 = AquiferFluidLevelRef(fluidLevel).y;
        int y1 = AquiferFluidLevelRef(fluidLevel2).y;
        int j = abs(y0 - y1);
        if (j == 0) {
            return 0.0lf;
        }
        double d = 0.5lf * double(y0 + y1);
        const double q = c2me_aquifer_getQ(double(i), d, double(j));
        return c2me_aquifer_postCalculateDensityModified(ctx, q, mutableDoubleThingy);
    }
    return 2.0lf;
}

bool c2me_aquifer_extractedCheckFG(const sample_int32_ctx_t ctx, const double density, const double d,
                                   uint64_t fluidLevel2, const double f, uint64_t fluidLevel4,
                                   inout double mutableDoubleThingy) {
    if (f > 0.0lf) {
        double g = d * f * c2me_aquifer_calculateDensityModified(ctx, fluidLevel2, fluidLevel4, mutableDoubleThingy);
        if (density + g > 0.0lf) {
            return true;
        }
    }
    return false;
}

uint64_t c2me_aquifer_waterLevel(uint64_t rw_data, int posIdx) {
    WorldgenParamsRef params = WorldgenParamsRef(rw_data);
    uint64_t dataAddr = rw_data + uint64_t(params.offset_aquifer);
    uint64_t waterLevels = dataAddr + uint64_t(AquiferDataRef(dataAddr).waterLevels);
    return waterLevels + uint64_t(posIdx) * uint64_t(AQUIFER_FLUIDLEVEL_SIZE);
}

aquifer_result_t c2me_aquifer_getFinalBlockState(const sample_int32_ctx_t ctx, const double density,
                                                 const double d, uint64_t fluidLevel2, uint64_t fluidLevel3,
                                                 const int blockState, uint64_t packedRes[4],
                                                 inout double mutableDoubleThingy) {
    uint64_t fluidLevel4 = c2me_aquifer_waterLevel(ctx.rw_data, math_aquifer_unpackPackedPosIdx(packedRes[2]));

    int dist1 = math_aquifer_unpackPackedDist(packedRes[0]);
    int dist2 = math_aquifer_unpackPackedDist(packedRes[1]);
    int dist3 = math_aquifer_unpackPackedDist(packedRes[2]);
    int dist4 = math_aquifer_unpackPackedDist(packedRes[3]);
    double f = math_aquifer_maxDistance(dist1, dist3);

    aquifer_result_t nullResult;
    nullResult.blockState = BLOCK_NULL;
    nullResult.needsFluidTick = false;

    if (c2me_aquifer_extractedCheckFG(ctx, density, d, fluidLevel2, f, fluidLevel4, mutableDoubleThingy)) {
        return nullResult;
    }

    double h = math_aquifer_maxDistance(dist2, dist3);
    if (c2me_aquifer_extractedCheckFG(ctx, density, d, fluidLevel3, h, fluidLevel4, mutableDoubleThingy)) {
        return nullResult;
    }

    bool needsFluidTick;
    bool bl = aquifer_fluidlevel_equals_global(fluidLevel2, fluidLevel3) == 0;
    bool bl2 = h >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD
            && aquifer_fluidlevel_equals_global(fluidLevel3, fluidLevel4) == 0;
    bool bl3 = f >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD
            && aquifer_fluidlevel_equals_global(fluidLevel2, fluidLevel4) == 0;
    if (!bl && !bl2 && !bl3) {
        uint64_t fluidLevel5 = c2me_aquifer_waterLevel(ctx.rw_data, math_aquifer_unpackPackedPosIdx(packedRes[3]));
        needsFluidTick = f >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD
                && math_aquifer_maxDistance(dist1, dist4) >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD
                && aquifer_fluidlevel_equals_global(fluidLevel2, fluidLevel5) == 0;
    } else {
        needsFluidTick = true;
    }

    aquifer_result_t result;
    result.blockState = blockState;
    result.needsFluidTick = needsFluidTick;
    return result;
}

aquifer_result_t c2me_aquifer_applyPost(const sample_int32_ctx_t ctx, const double density,
                                        const int j, const int i, const int k, uint64_t packedRes[4]) {
    uint64_t fluidLevel2 = c2me_aquifer_waterLevel(ctx.rw_data, math_aquifer_unpackPackedPosIdx(packedRes[0]));
    double d = math_aquifer_maxDistance(math_aquifer_unpackPackedDist(packedRes[0]),
                                        math_aquifer_unpackPackedDist(packedRes[1]));
    int blockState = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel2, j);

    aquifer_result_t result;
    if (d <= 0.0lf) {
        bool needsFluidTick = false;
        if (d >= aquifer_NEEDS_FLUID_TICK_DISTANCE_THRESHOLD) {
            uint64_t fluidLevel3 = c2me_aquifer_waterLevel(ctx.rw_data, math_aquifer_unpackPackedPosIdx(packedRes[1]));
            needsFluidTick = aquifer_fluidlevel_equals_global(fluidLevel2, fluidLevel3) == 0;
        }
        result.blockState = blockState;
        result.needsFluidTick = needsFluidTick;
        return result;
    } else if (blockState == BLOCK_WATER
            && aquifer_fluidlevel_getBlockState_ptr_global(
                    fluidLevelSampler_getFluidLevel_ptr(ctx.rw_data, j - 1), j - 1) == BLOCK_LAVA) {
        result.blockState = blockState;
        result.needsFluidTick = true;
        return result;
    } else {
        double mutableDoubleThingy = c2me_nan();
        uint64_t fluidLevel3 = c2me_aquifer_waterLevel(ctx.rw_data, math_aquifer_unpackPackedPosIdx(packedRes[1]));
        double e = d * c2me_aquifer_calculateDensityModified(ctx, fluidLevel2, fluidLevel3, mutableDoubleThingy);
        if (density + e > 0.0lf) {
            result.blockState = BLOCK_NULL;
            result.needsFluidTick = false;
            return result;
        }
        return c2me_aquifer_getFinalBlockState(ctx, density, d, fluidLevel2, fluidLevel3, blockState,
                packedRes, mutableDoubleThingy);
    }
}

aquifer_result_t aquifer_sample(const sample_int32_ctx_t ctx, const double density) {
    WorldgenParamsRef params = WorldgenParamsRef(ctx.rw_data);

    aquifer_result_t result;
    result.blockState = BLOCK_NULL;
    result.needsFluidTick = false;

    if (params.offset_aquifer == 0) {
        if (density > 0.0lf) {
            return result;
        }
        uint64_t fluidLevel = fluidLevelSampler_getFluidLevel_ptr(ctx.rw_data, ctx.y);
        result.blockState = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel, ctx.y);
        return result;
    }

    uint64_t aquiferAddr = ctx.rw_data + uint64_t(params.offset_aquifer);
    AquiferDataRef aquifer_data = AquiferDataRef(aquiferAddr);

    int i = ctx.x;
    int j = ctx.y;
    int k = ctx.z;

    if (density > 0.0lf) {
        return result;
    }

    uint64_t fluidLevel = fluidLevelSampler_getFluidLevel_ptr(ctx.rw_data, j);
    if (j > aquifer_data.samplingYLowPassCutoff) {
        result.blockState = aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel, j);
        return result;
    }
    if (aquifer_fluidlevel_getBlockState_ptr_global(fluidLevel, j) == BLOCK_LAVA) {
        result.blockState = BLOCK_LAVA;
        return result;
    }

    uint64_t packedBlockPositions = aquiferAddr + uint64_t(aquifer_data.packedBlockPositions);
    uint64_t packedRes[4];
    math_aquifer_refreshDistPosIdx_global(packedBlockPositions, packedRes, aquiferAddr, i, j, k);
    return c2me_aquifer_applyPost(ctx, density, j, i, k, packedRes);
}

struct vein_type_t {
    int ore;
    int rawOreBlock;
    int stone;
    int minY;
    int maxY;
};

int ore_vein_sample(const sample_int32_ctx_t ctx) {
    WorldgenParamsRef params = WorldgenParamsRef(ctx.rw_data);
    if (params.offset_oreVeinRandom == 0) return BLOCK_NULL;
    RandomStateRef veinRandom = RandomStateRef(ctx.rw_data + uint64_t(params.offset_oreVeinRandom));

    double d = df_binding_vein_toggle(ctx);
    vein_type_t veinType = d > 0.0lf
            ? vein_type_t(BLOCK_COPPER_ORE, BLOCK_RAW_COPPER_BLOCK, BLOCK_GRANITE, 0, 50)
            : vein_type_t(BLOCK_DEEPSLATE_IRON_ORE, BLOCK_RAW_IRON_BLOCK, BLOCK_TUFF, -60, -8);

    double e = abs(d);
    int j = veinType.maxY - ctx.y;
    int k = ctx.y - veinType.minY;
    if (k < 0 || j < 0) {
        return BLOCK_NULL;
    }

    int l = min(j, k);
    double f = math_clampedMap(double(l), 0.0lf, 20.0lf, -0.2lf, 0.0lf);
    if (e + f < 0.4lf) {
        return BLOCK_NULL;
    }

    random_state_t randomState;
    randomState.type = veinRandom.type;
    randomState.seedLo = veinRandom.seedLo;
    randomState.seedHi = veinRandom.seedHi;
    random_state_split_coords(randomState, ctx.x, ctx.y, ctx.z);

    if (random_state_nextFloat(randomState) > 0.7) {
        return BLOCK_NULL;
    }
    if (df_binding_vein_ridged(ctx) >= 0.0lf) {
        return BLOCK_NULL;
    }

    double g = math_clampedMap(e, 0.4lf, 0.6lf, 0.1lf, 0.3lf);
    if (double(random_state_nextFloat(randomState)) < g && df_binding_vein_gap(ctx) > -0.3lf) {
        return random_state_nextFloat(randomState) < 0.02 ? veinType.rawOreBlock : veinType.ore;
    }
    return veinType.stone;
}

const uint BIOME_NODE_SIZE = 32u;

uint64_t c2me_biome_node(uint64_t nodes, uint index) {
    return nodes + uint64_t(index) * uint64_t(BIOME_NODE_SIZE);
}

uint c2me_biome_node_state(uint64_t node) {
    return U32Ref(node).v[0];
}

bool c2me_biome_tree_is_branch(uint64_t node) {
    return (c2me_biome_node_state(node) & (1u << 31)) != 0u;
}

bool c2me_biome_tree_is_branch_children(uint64_t node) {
    return (c2me_biome_node_state(node) & (1u << 30)) != 0u;
}

uint64_t c2me_biome_tree_distance_func(uint64_t node, int target[7]) {
    I16Ref minmax = I16Ref(node + 4ul);
    uint64_t res = 0ul;
    for (uint i = 0u; i < 7u; i++) {
        int64_t l = int64_t(target[i]) - int64_t(int(minmax.v[i]));
        int64_t m = int64_t(int(minmax.v[7u + i])) - int64_t(target[i]);
        int64_t dist = l >= 0L ? l : max(m, 0L);
        res += uint64_t(dist * dist);
    }
    return res;
}

uint math_biome_search_tree_calc(uint64_t nodes, int target[7], const uint nodes_c) {
    if (!c2me_biome_tree_is_branch(c2me_biome_node(nodes, 1u))) {
        return c2me_biome_node_state(c2me_biome_node(nodes, 1u)) & 0x3FFFFFFFu;
    }

    uint workingNode[BIOME_SEARCH_TREE_MAX_DEPTH];
    uint workingIter[BIOME_SEARCH_TREE_MAX_DEPTH];
    uint top = 0u;
    uint current_optimal_node = 1u;
    uint64_t current_optimal_dist = 0xFFFFFFFFFFFFFFFFul;

    workingNode[top] = 1u;
    workingIter[top] = 0u;
    top++;

    while (top != 0u) {
        uint cur_node = workingNode[top - 1u];
        uint iter_i = workingIter[top - 1u];

        uint child_node = 0u;
        if (iter_i < 7u) {
            child_node = U32Ref(c2me_biome_node(nodes, cur_node + 1u) + 4ul).v[iter_i];
        }
        if (iter_i >= 7u || child_node == 0u) {
            top--;
            continue;
        }

        workingIter[top - 1u] = iter_i + 1u;

        uint64_t childAddr = c2me_biome_node(nodes, child_node);
        uint64_t d = c2me_biome_tree_distance_func(childAddr, target);

        if (d >= current_optimal_dist) {
            continue;
        }

        if (c2me_biome_tree_is_branch(childAddr)) {
            // The OpenCL backend trapped on overflow; dropping the node instead keeps the search
            // going with whatever optimum it has.
            if (top + 1u >= BIOME_SEARCH_TREE_MAX_DEPTH) {
                continue;
            }
            workingNode[top] = child_node;
            workingIter[top] = 0u;
            top++;
        } else {
            current_optimal_dist = d;
            current_optimal_node = child_node;
        }
    }

    return c2me_biome_node_state(c2me_biome_node(nodes, current_optimal_node)) & 0x3FFFFFFFu;
}

int c2me_convert_short_sat(double value) {
    float scaled = float(value) * 10000.0;
    return clamp(int(int64_t(scaled)), -32768, 32767);
}

double c2me_prefill_keepalive(uint64_t const_data, uint64_t rw_data, int a, int b, int c) {
    // Referenced so the symbols survive into the prefill variants; SpirVGen overwrites the stub
    // bodies below, and its replacements call exactly these.
    double acc = 0.0lf;
    acc += double(c2me_params_startBiomeX(rw_data) + c2me_params_startBiomeZ(rw_data)
                + c2me_params_startCellX(rw_data) + c2me_params_startCellY(rw_data)
                + c2me_params_startCellZ(rw_data) + c2me_params_cache2d_startX(rw_data)
                + c2me_params_cache2d_startZ(rw_data));
    acc += double(math_biome2block(a) + int(genShapeCfg_horizontalCellBlockCount())
                + int(genShapeCfg_verticalCellBlockCount()));
    acc += double(df_address_flatcache_buffer(rw_data, 0u, uint(a), uint(b))
                + df_address_cache2d_buffer(rw_data, 0u, uint(a), uint(b))
                + df_address_interpolator_buffer(rw_data, 0u, a, b, c));
    acc += double(df_data_offset_global(rw_data, 0) & 1ul);
    acc += make_sample_int32_ctx(const_data, rw_data, a, b, c, 0u).x;
    acc += c2me_helper_keepalive(make_sample_int32_ctx(const_data, rw_data, a, b, c, 0u));
    return acc;
}

void df_flatcache_prefill(uint64_t const_data, uint64_t rw_data, uint64_t extra_out,
                          uint cacheIndex, int offsetX, int offsetZ) {
    if (extra_out != 0ul) {
        F64Ref(extra_out).v[0] = c2me_prefill_keepalive(const_data, rw_data, offsetX, offsetZ, 0);
    }
}

void df_cache2d_prefill(uint64_t const_data, uint64_t rw_data, uint64_t extra_out,
                        int offsetX, int offsetZ) {
    if (extra_out != 0ul) {
        F64Ref(extra_out).v[0] = c2me_prefill_keepalive(const_data, rw_data, offsetX, offsetZ, 0);
    }
}

void df_interpolator_buffer_prefill(uint64_t const_data, uint64_t rw_data, uint64_t extra_out,
                                    int cellRelX, int cellRelY, int cellRelZ) {
    if (extra_out != 0ul) {
        F64Ref(extra_out).v[0] = c2me_prefill_keepalive(const_data, rw_data, cellRelX, cellRelY, cellRelZ);
    }
}

double c2me_keepalive(sample_int32_ctx_t ctx) {
    double acc = 0.0lf;
    acc += df_data_offset_global(ctx.rw_data, 0) != 0ul ? 1.0lf : 0.0lf;
    acc += math_clampedMap(double(ctx.y), 0.0lf, 1.0lf, 0.0lf, 1.0lf);
    acc += double(math_lerpf(0.5, 0.0, 1.0));
    acc += math_lerp3(0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf, 0.0lf);
    acc += math_clampedLerpFromProgress(0.0lf, 0.0lf, 1.0lf, 0.0lf, 1.0lf);
    acc += math_octave_maintainPrecision(1.0lf);
    acc += math_square(2.0lf);
    acc += math_simplex_grad(0, 0.0lf, 0.0lf, 0.0lf, 1.0lf);
    acc += c2me_math_simplex_grad(0, 0.0lf, 0.0lf, 0.0lf, 1.0lf);
    acc += double(math_floorMod(ctx.x, 4) + math_biome2block(1) + math_block2biome(4));
    acc += double(math_rotateLeftU64(1ul, 3ul));
    acc += double(c2me_math_simplex_map_global(ctx.const_data, 0));
    acc += math_perlinFade(0.5lf);
    acc += math_noise_simplex_sample2d_global(ctx.const_data, 0.0lf, 0.0lf);
    acc += double(math_end_islands_sample_global(ctx.const_data, ctx.x, ctx.z));
    acc += double(math_roundDownToMultiple(1.5lf, 2)) + double(genShapeCfg_verticalCellBlockCount())
         + double(genShapeCfg_horizontalCellBlockCount());
    acc += double(math_biome_access_sample(1L, ctx.x, ctx.y, ctx.z));
    acc += double(math_aquifer_index_global(ctx.rw_data, ctx.x, ctx.y, ctx.z));
    acc += double(math_aquifer_unpackPackedDist(1ul) + math_aquifer_unpackPackedPosIdx(1ul));
    {
        uint64_t nearest[4];
        math_aquifer_refreshDistPosIdx_global(ctx.rw_data, nearest, ctx.rw_data, ctx.x, ctx.y, ctx.z);
        acc += double(nearest[0] & 1ul);
    }
    acc += df_cachelike_interpolator(ctx.rw_data, ctx.rw_data, 0u, ctx.x, ctx.y, ctx.z, ctx.sample_flags).res;
    acc += df_cachelike_flatcache(ctx.rw_data, ctx.rw_data, 0u, ctx.x, ctx.y, ctx.z, ctx.sample_flags).res;
    acc += df_cachelike_cache2d(ctx.rw_data, ctx.rw_data, 0u, ctx.x, ctx.y, ctx.z, ctx.sample_flags).res;
    acc += df_caveScaler_scaleCaves(0.0lf) + df_caveScaler_scaleTunnels(0.0lf);
    acc += double(df_spline_findRangeForLocation(ctx.const_data, 1u, 0.0));
    acc += double(df_spline_sampleOutsideRange(0.0, ctx.const_data, 0.0, ctx.const_data, 0));
    acc += math_fastInverseSqrt(2.0lf);
    acc += df_structureWeightSampler_sample(ctx.const_data, ctx.rw_data, ctx.x, ctx.y, ctx.z);
    acc += double(chunkNoiseSampler_estimateSurfaceHeight0(ctx.const_data, ctx.rw_data, ctx.x, ctx.z));
    acc += double(chunkNoiseSampler_estimateSurfaceHeight(ctx.const_data, ctx.rw_data, ctx.x, ctx.z));
    df_flatcache_prefill(ctx.const_data, ctx.rw_data, pc.out_data, 0u, ctx.x, ctx.z);
    df_cache2d_prefill(ctx.const_data, ctx.rw_data, pc.out_data, ctx.x, ctx.z);
    df_interpolator_buffer_prefill(ctx.const_data, ctx.rw_data, pc.out_data, ctx.x, ctx.y, ctx.z);
    acc += double(df_address_flatcache_buffer(ctx.rw_data, 0u, 0u, 0u)
                + df_address_cache2d_buffer(ctx.rw_data, 0u, 0u, 0u)
                + df_address_interpolator_buffer(ctx.rw_data, 0u, 0, 0, 0));
    acc += make_sample_int32_ctx(ctx.const_data, ctx.rw_data, 0, 0, 0, 0u).x;
    {
        random_state_t rng;
        rng.type = RANDOM_Xoroshiro128PlusPlus;
        rng.seedLo = 1ul;
        rng.seedHi = 2ul;
        random_state_set_seed(rng, 1L);
        random_state_split_coords(rng, ctx.x, ctx.y, ctx.z);
        acc += double(random_state_nextFloat(rng));
        acc += double(random_state_nextIntBounded(rng, 4));
        acc += double(random_state_Xoroshiro128PlusPlus_next(rng, 24));
        acc += double(random_state_Checked_next(rng, 24));
        acc += double(math_hashCode_int32x3(ctx.x, ctx.y, ctx.z) & 1L);
        acc += double(math_mixStafford13(1ul) & 1ul);
    }
    acc += double(aquifer_fluidlevel_getBlockState_ptr_global(ctx.rw_data, ctx.y)
                + aquifer_fluidlevel_equals_global(ctx.rw_data, ctx.rw_data)
                + c2me_aquifer_chunkPosOffset[ctx.x & 25]);
    acc += math_noise_perlin_double_octave_sample_global_noinline(ctx.const_data, 0.0lf, 0.0lf, 0.0lf);
    acc += math_noise_perlin_double_octave_sample_global(ctx.const_data, 0.0lf, 0.0lf, 0.0lf);
    acc += math_noise_perlin_interpolated_sample_global_noinline(ctx.const_data, 0.0lf, 0.0lf, 0.0lf);
    // All four cache variants of every binding are replaced by SpirVGen, so all four
    // must survive glslang's dead code elimination.
    acc += df_binding_barrier(ctx) + df_binding_barrier_uncached(ctx)
         + df_binding_barrier_flatcache_only(ctx) + df_binding_barrier_fully_cached(ctx);
    acc += df_binding_fluid_level_floodedness(ctx) + df_binding_fluid_level_floodedness_uncached(ctx)
         + df_binding_fluid_level_floodedness_flatcache_only(ctx) + df_binding_fluid_level_floodedness_fully_cached(ctx);
    acc += df_binding_fluid_level_spread(ctx) + df_binding_fluid_level_spread_uncached(ctx)
         + df_binding_fluid_level_spread_flatcache_only(ctx) + df_binding_fluid_level_spread_fully_cached(ctx);
    acc += df_binding_lava(ctx) + df_binding_lava_uncached(ctx)
         + df_binding_lava_flatcache_only(ctx) + df_binding_lava_fully_cached(ctx);
    acc += df_binding_temperature(ctx) + df_binding_temperature_uncached(ctx)
         + df_binding_temperature_flatcache_only(ctx) + df_binding_temperature_fully_cached(ctx);
    acc += df_binding_vegetation(ctx) + df_binding_vegetation_uncached(ctx)
         + df_binding_vegetation_flatcache_only(ctx) + df_binding_vegetation_fully_cached(ctx);
    acc += df_binding_continents(ctx) + df_binding_continents_uncached(ctx)
         + df_binding_continents_flatcache_only(ctx) + df_binding_continents_fully_cached(ctx);
    acc += df_binding_erosion(ctx) + df_binding_erosion_uncached(ctx)
         + df_binding_erosion_flatcache_only(ctx) + df_binding_erosion_fully_cached(ctx);
    acc += df_binding_depth(ctx) + df_binding_depth_uncached(ctx)
         + df_binding_depth_flatcache_only(ctx) + df_binding_depth_fully_cached(ctx);
    acc += df_binding_ridges(ctx) + df_binding_ridges_uncached(ctx)
         + df_binding_ridges_flatcache_only(ctx) + df_binding_ridges_fully_cached(ctx);
    acc += df_binding_preliminary_surface_level(ctx) + df_binding_preliminary_surface_level_uncached(ctx)
         + df_binding_preliminary_surface_level_flatcache_only(ctx) + df_binding_preliminary_surface_level_fully_cached(ctx);
    acc += df_binding_final_density(ctx) + df_binding_final_density_uncached(ctx)
         + df_binding_final_density_flatcache_only(ctx) + df_binding_final_density_fully_cached(ctx);
    acc += df_binding_vein_toggle(ctx) + df_binding_vein_toggle_uncached(ctx)
         + df_binding_vein_toggle_flatcache_only(ctx) + df_binding_vein_toggle_fully_cached(ctx);
    acc += df_binding_vein_ridged(ctx) + df_binding_vein_ridged_uncached(ctx)
         + df_binding_vein_ridged_flatcache_only(ctx) + df_binding_vein_ridged_fully_cached(ctx);
    acc += df_binding_vein_gap(ctx) + df_binding_vein_gap_uncached(ctx)
         + df_binding_vein_gap_flatcache_only(ctx) + df_binding_vein_gap_fully_cached(ctx);
    acc += df_binding_final_final_density(ctx) + df_binding_final_final_density_uncached(ctx)
         + df_binding_final_final_density_flatcache_only(ctx) + df_binding_final_final_density_fully_cached(ctx);
    return acc;
}

void main() {
#if defined(DF_COMPILE_ESTIMATE_SURFACE_HEIGHT)

    // arg0/arg1: starting chunk position, arg2: cache width in biome columns.
    int relX = int(gl_GlobalInvocationID.x);
    int relZ = int(gl_GlobalInvocationID.y);
    int biomeX = (pc.arg0 << 2) + relX;
    int biomeZ = (pc.arg1 << 2) + relZ;
    I32Ref(pc.out_data).v[relX * pc.arg2 + relZ] =
            chunkNoiseSampler_estimateSurfaceHeight0(pc.const_data, pc.rw_data,
                    math_biome2block(biomeX), math_biome2block(biomeZ));

#elif defined(DF_COMPILE_FLAT_CACHE_PREFILL)

    // arg2 selects the prefill: the OpenCL backend emitted one kernel per index
    // (df_flatcache_prefill_kernel_<i>), which a single-entry-point module cannot do.
    df_flatcache_prefill(pc.const_data, pc.rw_data, pc.out_data, uint(pc.arg2),
            int(gl_GlobalInvocationID.x), int(gl_GlobalInvocationID.y));

#elif defined(DF_COMPILE_BIOME_MULTINOISE_KERNEL)

    // res_biomes: [relY][relZ][relX]. arg0/arg1/arg2: startBiomeX, startBiomeZ, startBiomeY.
    const int sizeX = int(gl_NumWorkGroups.x * gl_WorkGroupSize.x);
    const int sizeZ = int(gl_NumWorkGroups.y * gl_WorkGroupSize.y);
    const int relX = int(gl_GlobalInvocationID.x);
    const int relZ = int(gl_GlobalInvocationID.y);
    const int relY = int(gl_GlobalInvocationID.z);

    sample_int32_ctx_t ctx = make_sample_int32_ctx(pc.const_data, pc.rw_data,
            math_biome2block(pc.arg0 + relX),
            math_biome2block(pc.arg2 + relY),
            math_biome2block(pc.arg1 + relZ),
            MASK_enableFlatCache);

    int target[7];
    target[0] = c2me_convert_short_sat(df_binding_temperature(ctx));
    target[1] = c2me_convert_short_sat(df_binding_vegetation(ctx));
    target[2] = c2me_convert_short_sat(df_binding_continents(ctx));
    target[3] = c2me_convert_short_sat(df_binding_erosion(ctx));
    target[4] = c2me_convert_short_sat(df_binding_depth(ctx));
    target[5] = c2me_convert_short_sat(df_binding_ridges(ctx));
    target[6] = 0;

    uint64_t root_node = pc.const_data + uint64_t(biome_multinoise_tree_offset);
    uint result_biome = math_biome_search_tree_calc(root_node, target, biome_multinoise_tree_nodes_c);

    uint idx = uint((relY * sizeX + relZ) * sizeZ + relX);
    U32Ref(pc.out_data).v[idx] = result_biome;

#elif defined(DF_COMPILE_NOISE_KERNEL)

    // res_blocks: [relY][relZ][relX], sign bit indicates needsFluidTick.
    // arg0/arg1 are the chunk position.
    const int sizeX = int(gl_NumWorkGroups.x * gl_WorkGroupSize.x);
    const int sizeY = int(gl_NumWorkGroups.z * gl_WorkGroupSize.z);
    const int sizeZ = int(gl_NumWorkGroups.y * gl_WorkGroupSize.y);
    const int relX = int(gl_GlobalInvocationID.x);
    const int relY = int(gl_GlobalInvocationID.z);
    const int relZ = int(gl_GlobalInvocationID.y);

    sample_int32_ctx_t ctx = make_sample_int32_ctx(pc.const_data, pc.rw_data,
            (pc.arg0 << 4) + relX,
            genShapeCfg_minimumY + relY,
            (pc.arg1 << 4) + relZ,
            MASK_enableAllCaches);

    aquifer_result_t aquifer_res = aquifer_sample(ctx, df_binding_final_final_density(ctx));
    int blockState = aquifer_res.blockState;
    if (blockState == BLOCK_NULL) {
        blockState = ore_vein_sample(ctx);
    }
    if (blockState == BLOCK_NULL) {
        blockState = WorldgenParamsRef(pc.rw_data).genConfig_defaultBlock;
    }

    uint idx = uint((relY * sizeX + relZ) * sizeZ + relX);
    U8Ref(pc.out_data).v[idx] = uint8_t(uint(blockState) | (aquifer_res.needsFluidTick ? (1u << 7) : 0u));

#elif defined(DF_COMPILE_AQUIFER_PREFILL)

    aquifer_data_prefill(pc.const_data, pc.rw_data,
            int(gl_GlobalInvocationID.x), int(gl_GlobalInvocationID.z), int(gl_GlobalInvocationID.y));

#elif defined(DF_COMPILE_CACHE2D_PREFILL)

    df_cache2d_prefill(pc.const_data, pc.rw_data, pc.out_data,
            int(gl_GlobalInvocationID.x), int(gl_GlobalInvocationID.y));

#elif defined(DF_COMPILE_INTERPOLATOR_PREFILL)

    // The OpenCL kernel read Y from global id 2 and Z from 1. vkCmdDispatch counts workgroups
    // rather than invocations, so the lattice extent is rounded up to whole groups and the
    // surplus invocations drop out here; arg0 is that extent (equal on both horizontal axes).
    if (gl_GlobalInvocationID.x < uint(pc.arg0) && gl_GlobalInvocationID.y < uint(pc.arg0)) {
        df_interpolator_buffer_prefill(pc.const_data, pc.rw_data, pc.out_data,
                int(gl_GlobalInvocationID.x), int(gl_GlobalInvocationID.z), int(gl_GlobalInvocationID.y));
    }

#else

    // AQUIFER_PREFILL, NOISE_KERNEL and BIOME_MULTINOISE_KERNEL are not ported yet. Until they
    // are, this keeps every symbol SpirVGen resolves reachable from an entry point -- without a
    // reference glslang eliminates them and preludeFunctionId throws.
    sample_int32_ctx_t ctx;
    ctx.const_data   = pc.const_data;
    ctx.rw_data      = pc.rw_data;
    ctx.x            = (pc.arg0 << 4) + int(gl_GlobalInvocationID.x);
    ctx.y            = genShapeCfg_minimumY + int(gl_GlobalInvocationID.z);
    ctx.z            = (pc.arg1 << 4) + int(gl_GlobalInvocationID.y);
    ctx.sample_flags = MASK_enableAllCaches;

    double result = c2me_keepalive(ctx);

    uint idx = gl_GlobalInvocationID.z * 256u + gl_GlobalInvocationID.y * 16u + gl_GlobalInvocationID.x;
    U32Ref(pc.out_data).v[idx] = result > 0.0lf ? 1u : 0u;

#endif
}
