#include <stddef.h>
#include <stdint.h>
#include <float.h>

typedef int make_iso_compilers_happy;

#ifdef WIN32

// ld.lld: error: <root>: undefined symbol: DllMainCRTStartup
int __stdcall DllMainCRTStartup(void* instance, unsigned reason, void* reserved)
{
  (void) instance;
  (void) reason;
  (void) reserved;
  return 1;
}

// ld.lld: error: undefined symbol: _fltused
int _fltused = 0;

// ld.lld: error: undefined symbol: abort
void abort(void)
{
  __builtin_trap();
}

#endif // WIN32

/*
The following code is from musl, original license below:

musl as a whole is licensed under the following standard MIT license:

----------------------------------------------------------------------
Copyright © 2005-2020 Rich Felker, et al.

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
----------------------------------------------------------------------
 */

// src/internal/libm.h
#if LDBL_MANT_DIG == 53 && LDBL_MAX_EXP == 1024
#elif LDBL_MANT_DIG == 64 && LDBL_MAX_EXP == 16384 && __BYTE_ORDER == __LITTLE_ENDIAN
union ldshape {
    long double f;
    struct {
        uint64_t m;
        uint16_t se;
    } i;
};
#elif LDBL_MANT_DIG == 64 && LDBL_MAX_EXP == 16384 && __BYTE_ORDER == __BIG_ENDIAN
/* This is the m68k variant of 80-bit long double, and this definition only works
 * on archs where the alignment requirement of uint64_t is <= 4. */
union ldshape {
    long double f;
    struct {
        uint16_t se;
        uint16_t pad;
        uint64_t m;
    } i;
};
#elif LDBL_MANT_DIG == 113 && LDBL_MAX_EXP == 16384 && __BYTE_ORDER == __LITTLE_ENDIAN
union ldshape {
    long double f;
    struct {
        uint64_t lo;
        uint32_t mid;
        uint16_t top;
        uint16_t se;
    } i;
    struct {
        uint64_t lo;
        uint64_t hi;
    } i2;
};
#elif LDBL_MANT_DIG == 113 && LDBL_MAX_EXP == 16384 && __BYTE_ORDER == __BIG_ENDIAN
union ldshape {
    long double f;
    struct {
        uint16_t se;
        uint16_t top;
        uint32_t mid;
        uint64_t lo;
    } i;
    struct {
        uint64_t hi;
        uint64_t lo;
    } i2;
};
#else
#error Unsupported long double representation
#endif

/* Support non-nearest rounding mode.  */
#define WANT_ROUNDING 1
/* Support signaling NaNs.  */
#define WANT_SNAN 0

#if WANT_SNAN
#error SNaN is unsupported
#else
#define issignalingf_inline(x) 0
#define issignaling_inline(x) 0
#endif

#ifndef TOINT_INTRINSICS
#define TOINT_INTRINSICS 0
#endif

#if TOINT_INTRINSICS
/* Round x to nearest int in all rounding modes, ties have to be rounded
   consistently with converttoint so the results match.  If the result
   would be outside of [-2^31, 2^31-1] then the semantics is unspecified.  */
static double_t roundtoint(double_t);

/* Convert x to nearest int in all rounding modes, ties have to be rounded
   consistently with roundtoint.  If the result is not representible in an
   int32_t then the semantics is unspecified.  */
static int32_t converttoint(double_t);
#endif

/* Helps static branch prediction so hot path can be better optimized.  */
#ifdef __GNUC__
#define predict_true(x) __builtin_expect(!!(x), 1)
#define predict_false(x) __builtin_expect(x, 0)
#else
#define predict_true(x) (x)
#define predict_false(x) (x)
#endif

/* Evaluate an expression as the specified type. With standard excess
   precision handling a type cast or assignment is enough (with
   -ffloat-store an assignment is required, in old compilers argument
   passing and return statement may not drop excess precision).  */

static inline float eval_as_float(float x) {
    float y = x;
    return y;
}

static inline double eval_as_double(double x) {
    double y = x;
    return y;
}

/* fp_barrier returns its input, but limits code transformations
   as if it had a side-effect (e.g. observable io) and returned
   an arbitrary value.  */

#ifndef fp_barrierf
#define fp_barrierf fp_barrierf

static inline float fp_barrierf(float x) {
    volatile float y = x;
    return y;
}

#endif

#ifndef fp_barrier
#define fp_barrier fp_barrier

static inline double fp_barrier(double x) {
    volatile double y = x;
    return y;
}

#endif

#ifndef fp_barrierl
#define fp_barrierl fp_barrierl

static inline long double fp_barrierl(long double x) {
    volatile long double y = x;
    return y;
}

#endif

/* fp_force_eval ensures that the input value is computed when that's
   otherwise unused.  To prevent the constant folding of the input
   expression, an additional fp_barrier may be needed or a compilation
   mode that does so (e.g. -frounding-math in gcc). Then it can be
   used to evaluate an expression for its fenv side-effects only.   */

#ifndef fp_force_evalf
#define fp_force_evalf fp_force_evalf

static inline void fp_force_evalf(float x) {
    volatile float y;
    y = x;
}

#endif

#ifndef fp_force_eval
#define fp_force_eval fp_force_eval

static inline void fp_force_eval(double x) {
    volatile double y;
    y = x;
}

#endif

#ifndef fp_force_evall
#define fp_force_evall fp_force_evall

static inline void fp_force_evall(long double x) {
    volatile long double y;
    y = x;
}

#endif

#define FORCE_EVAL(x) do {                        \
    if (sizeof(x) == sizeof(float)) {         \
        fp_force_evalf(x);                \
    } else if (sizeof(x) == sizeof(double)) { \
        fp_force_eval(x);                 \
    } else {                                  \
        fp_force_evall(x);                \
    }                                         \
} while(0)

#define asuint(f) ((union{float _f; uint32_t _i;}){f})._i
#define asfloat(i) ((union{uint32_t _i; float _f;}){i})._f
#define asuint64(f) ((union{double _f; uint64_t _i;}){f})._i
#define asdouble(i) ((union{uint64_t _i; double _f;}){i})._f

#define EXTRACT_WORDS(hi, lo, d)                    \
do {                                              \
  uint64_t __u = asuint64(d);                     \
  (hi) = __u >> 32;                               \
  (lo) = (uint32_t)__u;                           \
} while (0)

#define GET_HIGH_WORD(hi, d)                       \
do {                                              \
  (hi) = asuint64(d) >> 32;                       \
} while (0)

#define GET_LOW_WORD(lo, d)                        \
do {                                              \
  (lo) = (uint32_t)asuint64(d);                   \
} while (0)

#define INSERT_WORDS(d, hi, lo)                     \
do {                                              \
  (d) = asdouble(((uint64_t)(hi)<<32) | (uint32_t)(lo)); \
} while (0)

#define SET_HIGH_WORD(d, hi)                       \
  INSERT_WORDS(d, hi, (uint32_t)asuint64(d))

#define SET_LOW_WORD(d, lo)                        \
  INSERT_WORDS(d, asuint64(d)>>32, lo)

#define GET_FLOAT_WORD(w, d)                       \
do {                                              \
  (w) = asuint(d);                                \
} while (0)

#define SET_FLOAT_WORD(d, w)                       \
do {                                              \
  (d) = asfloat(w);                               \
} while (0)

static int __rem_pio2_large(double *, double *, int, int, int);

static int __rem_pio2(double, double *);

static double __sin(double, double, int);

static double __cos(double, double);

static double __tan(double, double, int);

static double __expo2(double, double);

static int __rem_pio2f(float, double *);

static float __sindf(double);

static float __cosdf(double);

static float __tandf(double, int);

static float __expo2f(float, float);

static int __rem_pio2l(long double, long double *);

static long double __sinl(long double, long double, int);

static long double __cosl(long double, long double);

static long double __tanl(long double, long double, int);

static long double __polevll(long double, const long double *, int);

static long double __p1evll(long double, const long double *, int);

//extern int __signgam;
static double __lgamma_r(double, int *);

static float __lgammaf_r(float, int *);

/* error handling functions */
static float __math_xflowf(uint32_t, float);

static float __math_uflowf(uint32_t);

static float __math_oflowf(uint32_t);

static float __math_divzerof(uint32_t);

static float __math_invalidf(float);

static double __math_xflow(uint32_t, double);

static double __math_uflow(uint32_t);

static double __math_oflow(uint32_t);

static double __math_divzero(uint32_t);

static double __math_invalid(double);

#if LDBL_MANT_DIG != DBL_MANT_DIG

static long double __math_invalidl(long double);

#endif

// src/math/__math_invalidf.c
static float __math_invalidf(float x)
{
	return (x - x) / (x - x);
}

// src/math/truncf.c

float truncf(float x) {
    union {
        float f;
        uint32_t i;
    } u = {x};
    int e = (int) (u.i >> 23 & 0xff) - 0x7f + 9;
    uint32_t m;

    if (e >= 23 + 9)
        return x;
    if (e < 9)
        e = 1;
    m = -1U >> e;
    if ((u.i & m) == 0)
        return x;
    FORCE_EVAL(x + 0x1p120f);
    u.i &= ~m;
    return u.f;
}

// src/math/floor.c

#if FLT_EVAL_METHOD == 0 || FLT_EVAL_METHOD == 1
#define EPS DBL_EPSILON
#elif FLT_EVAL_METHOD == 2
#define EPS LDBL_EPSILON
#endif
static const double toint = 1 / EPS;

double floor(double x) {
    union {
        double f;
        uint64_t i;
    } u = {x};
    int e = u.i >> 52 & 0x7ff;
    double y;

    if (e >= 0x3ff + 52 || x == 0)
        return x;
    /* y = int(x) - x, where int(x) is an integer neighbor of x */
    if (u.i >> 63)
        y = x - toint + toint - x;
    else
        y = x + toint - toint - x;
    /* special case because of non-nearest rounding modes */
    if (e <= 0x3ff - 1) {
        FORCE_EVAL(y);
        return u.i >> 63 ? -1 : 0;
    }
    if (y > 0)
        return x + y - 1;
    return x + y;
}

// src/math/fmodf.c

float fmodf(float x, float y) {
    union {
        float f;
        uint32_t i;
    } ux = {x}, uy = {y};
    int ex = ux.i >> 23 & 0xff;
    int ey = uy.i >> 23 & 0xff;
    uint32_t sx = ux.i & 0x80000000;
    uint32_t i;
    uint32_t uxi = ux.i;

    if (uy.i << 1 == 0 || __builtin_isnan(y) || ex == 0xff)
        return (x * y) / (x * y);
    if (uxi << 1 <= uy.i << 1) {
        if (uxi << 1 == uy.i << 1)
            return 0 * x;
        return x;
    }

    /* normalize x and y */
    if (!ex) {
        for (i = uxi << 9; i >> 31 == 0; ex--, i <<= 1);
        uxi <<= -ex + 1;
    } else {
        uxi &= -1U >> 9;
        uxi |= 1U << 23;
    }
    if (!ey) {
        for (i = uy.i << 9; i >> 31 == 0; ey--, i <<= 1);
        uy.i <<= -ey + 1;
    } else {
        uy.i &= -1U >> 9;
        uy.i |= 1U << 23;
    }

    /* x mod y */
    for (; ex > ey; ex--) {
        i = uxi - uy.i;
        if (i >> 31 == 0) {
            if (i == 0)
                return 0 * x;
            uxi = i;
        }
        uxi <<= 1;
    }
    i = uxi - uy.i;
    if (i >> 31 == 0) {
        if (i == 0)
            return 0 * x;
        uxi = i;
    }
    for (; uxi >> 23 == 0; uxi <<= 1, ex--);

    /* scale result up */
    if (ex > 0) {
        uxi -= 1U << 23;
        uxi |= (uint32_t) ex << 23;
    } else {
        uxi >>= -ex + 1;
    }
    uxi |= sx;
    ux.i = uxi;
    return ux.f;
}

// src/string/memset.c

void *memset(void *dest, int c, size_t n) {
    unsigned char *s = dest;
    size_t k;

    /* Fill head and tail with minimal branching. Each
     * conditional ensures that all the subsequently used
     * offsets are well-defined and in the dest region. */

    if (!n) return dest;
    s[0] = c;
    s[n - 1] = c;
    if (n <= 2) return dest;
    s[1] = c;
    s[2] = c;
    s[n - 2] = c;
    s[n - 3] = c;
    if (n <= 6) return dest;
    s[3] = c;
    s[n - 4] = c;
    if (n <= 8) return dest;

    /* Advance pointer to align it at a 4-byte boundary,
     * and truncate n to a multiple of 4. The previous code
     * already took care of any head/tail that get cut off
     * by the alignment. */

    k = -(uintptr_t) s & 3;
    s += k;
    n -= k;
    n &= -4;

#ifdef __GNUC__
    typedef uint32_t __attribute__((__may_alias__)) u32;
    typedef uint64_t __attribute__((__may_alias__)) u64;

    u32 c32 = ((u32) -1) / 255 * (unsigned char) c;

    /* In preparation to copy 32 bytes at a time, aligned on
     * an 8-byte bounary, fill head/tail up to 28 bytes each.
     * As in the initial byte-based head/tail fill, each
     * conditional below ensures that the subsequent offsets
     * are valid (e.g. !(n<=24) implies n>=28). */

    *(u32 *) (s + 0) = c32;
    *(u32 *) (s + n - 4) = c32;
    if (n <= 8) return dest;
    *(u32 *) (s + 4) = c32;
    *(u32 *) (s + 8) = c32;
    *(u32 *) (s + n - 12) = c32;
    *(u32 *) (s + n - 8) = c32;
    if (n <= 24) return dest;
    *(u32 *) (s + 12) = c32;
    *(u32 *) (s + 16) = c32;
    *(u32 *) (s + 20) = c32;
    *(u32 *) (s + 24) = c32;
    *(u32 *) (s + n - 28) = c32;
    *(u32 *) (s + n - 24) = c32;
    *(u32 *) (s + n - 20) = c32;
    *(u32 *) (s + n - 16) = c32;

    /* Align to a multiple of 8 so we can fill 64 bits at a time,
     * and avoid writing the same bytes twice as much as is
     * practical without introducing additional branching. */

    k = 24 + ((uintptr_t) s & 4);
    s += k;
    n -= k;

    /* If this loop is reached, 28 tail bytes have already been
     * filled, so any remainder when n drops below 32 can be
     * safely ignored. */

    u64 c64 = c32 | ((u64) c32 << 32);
    for (; n >= 32; n -= 32, s += 32) {
        *(u64 *) (s + 0) = c64;
        *(u64 *) (s + 8) = c64;
        *(u64 *) (s + 16) = c64;
        *(u64 *) (s + 24) = c64;
    }
#else
    /* Pure C fallback with no aliasing violations. */
    for (; n; n--, s++) *s = c;
#endif

    return dest;
}

// src/math/sqrt_data.[c|h]

/* if x in [1,2): i = (int)(64*x);
   if x in [2,4): i = (int)(32*x-64);
   __rsqrt_tab[i]*2^-16 is estimating 1/sqrt(x) with small relative error:
   |__rsqrt_tab[i]*0x1p-16*sqrt(x) - 1| < -0x1.fdp-9 < 2^-8 */
extern const uint16_t __rsqrt_tab[128] = {
        0xb451, 0xb2f0, 0xb196, 0xb044, 0xaef9, 0xadb6, 0xac79, 0xab43,
        0xaa14, 0xa8eb, 0xa7c8, 0xa6aa, 0xa592, 0xa480, 0xa373, 0xa26b,
        0xa168, 0xa06a, 0x9f70, 0x9e7b, 0x9d8a, 0x9c9d, 0x9bb5, 0x9ad1,
        0x99f0, 0x9913, 0x983a, 0x9765, 0x9693, 0x95c4, 0x94f8, 0x9430,
        0x936b, 0x92a9, 0x91ea, 0x912e, 0x9075, 0x8fbe, 0x8f0a, 0x8e59,
        0x8daa, 0x8cfe, 0x8c54, 0x8bac, 0x8b07, 0x8a64, 0x89c4, 0x8925,
        0x8889, 0x87ee, 0x8756, 0x86c0, 0x862b, 0x8599, 0x8508, 0x8479,
        0x83ec, 0x8361, 0x82d8, 0x8250, 0x81c9, 0x8145, 0x80c2, 0x8040,
        0xff02, 0xfd0e, 0xfb25, 0xf947, 0xf773, 0xf5aa, 0xf3ea, 0xf234,
        0xf087, 0xeee3, 0xed47, 0xebb3, 0xea27, 0xe8a3, 0xe727, 0xe5b2,
        0xe443, 0xe2dc, 0xe17a, 0xe020, 0xdecb, 0xdd7d, 0xdc34, 0xdaf1,
        0xd9b3, 0xd87b, 0xd748, 0xd61a, 0xd4f1, 0xd3cd, 0xd2ad, 0xd192,
        0xd07b, 0xcf69, 0xce5b, 0xcd51, 0xcc4a, 0xcb48, 0xca4a, 0xc94f,
        0xc858, 0xc764, 0xc674, 0xc587, 0xc49d, 0xc3b7, 0xc2d4, 0xc1f4,
        0xc116, 0xc03c, 0xbf65, 0xbe90, 0xbdbe, 0xbcef, 0xbc23, 0xbb59,
        0xba91, 0xb9cc, 0xb90a, 0xb84a, 0xb78c, 0xb6d0, 0xb617, 0xb560,
};

// src/math/sqrtf.c
#define FENV_SUPPORT 1

static inline uint32_t mul32(uint32_t a, uint32_t b) {
    return (uint64_t) a * b >> 32;
}

/* see sqrt.c for more detailed comments.  */

float sqrtf(float x) {
    uint32_t ix, m, m1, m0, even, ey;

    ix = asuint(x);
    if (predict_false(ix - 0x00800000 >= 0x7f800000 - 0x00800000)) {
        /* x < 0x1p-126 or inf or nan.  */
        if (ix * 2 == 0)
            return x;
        if (ix == 0x7f800000)
            return x;
        if (ix > 0x7f800000)
            return __math_invalidf(x);
        /* x is subnormal, normalize it.  */
        ix = asuint(x * 0x1p23f);
        ix -= 23 << 23;
    }

    /* x = 4^e m; with int e and m in [1, 4).  */
    even = ix & 0x00800000;
    m1 = (ix << 8) | 0x80000000;
    m0 = (ix << 7) & 0x7fffffff;
    m = even ? m0 : m1;

    /* 2^e is the exponent part of the return value.  */
    ey = ix >> 1;
    ey += 0x3f800000 >> 1;
    ey &= 0x7f800000;

    /* compute r ~ 1/sqrt(m), s ~ sqrt(m) with 2 goldschmidt iterations.  */
    static const uint32_t three = 0xc0000000;
    uint32_t r, s, d, u, i;
    i = (ix >> 17) % 128;
    r = (uint32_t) __rsqrt_tab[i] << 16;
    /* |r*sqrt(m) - 1| < 0x1p-8 */
    s = mul32(m, r);
    /* |s/sqrt(m) - 1| < 0x1p-8 */
    d = mul32(s, r);
    u = three - d;
    r = mul32(r, u) << 1;
    /* |r*sqrt(m) - 1| < 0x1.7bp-16 */
    s = mul32(s, u) << 1;
    /* |s/sqrt(m) - 1| < 0x1.7bp-16 */
    d = mul32(s, r);
    u = three - d;
    s = mul32(s, u);
    /* -0x1.03p-28 < s/sqrt(m) - 1 < 0x1.fp-31 */
    s = (s - 1) >> 6;
    /* s < sqrt(m) < s + 0x1.08p-23 */

    /* compute nearest rounded result.  */
    uint32_t d0, d1, d2;
    float y, t;
    d0 = (m << 16) - s * s;
    d1 = s - d0;
    d2 = d1 + s + 1;
    s += d1 >> 31;
    s &= 0x007fffff;
    s |= ey;
    y = asfloat(s);
    if (FENV_SUPPORT) {
        /* handle rounding and inexact exception. */
        uint32_t tiny = predict_false(d2 == 0) ? 0 : 0x01000000;
        tiny |= (d1 ^ d2) & 0x80000000;
        t = asfloat(tiny);
        y = eval_as_float(y + t);
    }
    return y;
}

