/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.natives_math.common.isa;

import com.ishland.c2me.opts.natives_math.common.ISATarget;

public enum ISA_x86_64 implements ISATarget {
    SSE2("_sse2", true), // 0
    SSE4_1("_sse2", false), // 1, not implemented
    SSE4_2("_sse4_2", true), // 2
    AVX("_avx", true), // 3
    AVX11("_avx", false), // 4, not implemented
    AVX2("_avx2", true), // 5
    AVX2VNNI("_avx2vnni", true), // 6
    KNL_AVX512("_avx2", false), // 7, not implemented
    SKX_AVX512("_avx512skx", true), // 8
    ICL_AVX512("_avx512icl", true), // 9
    SPR_AVX512("_avx512spr", true), // 10
    GNR_AVX512("_avx512spr", false), // 11, not implemented
    DMR_AVX10_2("_avx10_2dmr", true), // 12
    NVL_AVX10_2("_avx10_2nvl", true), // 13
    ;

    private final String suffix;
    private final boolean nativelySupported;

    ISA_x86_64(String suffix, boolean nativelySupported) {
        this.suffix = suffix;
        this.nativelySupported = nativelySupported;
    }

    @Override
    public String getSuffix() {
        return this.suffix;
    }

    @Override
    public boolean isNativelySupported() {
        return this.nativelySupported;
    }
}
