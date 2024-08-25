package com.ishland.c2me.opts.natives_math.common.isa;

import com.ishland.c2me.opts.natives_math.common.ISATarget;

public enum ISA_x86_64 implements ISATarget {
    SSE2("_sse2", true), // 0
    SSE4_1("_sse2", false), // 1, not implemented
    SSE4_2("_sse4_2", true), // 2
    AVX("_avx", true), // 3
    AVX2("_avx2", true), // 4
    AVX2ADL("_avx2adl", true), // 5
    AVX512KNL("_avx2", false), // 6, not implemented
    AVX512SKX("_avx512skx", true), // 7, disabled
    AVX512ICL("_avx512icl", true), // 8
    AVX512SPR("_avx512spr", true), // 9
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
