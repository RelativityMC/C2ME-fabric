package com.ishland.c2me.opts.natives_math.common.isa;

import com.ishland.c2me.opts.natives_math.common.ISATarget;

public enum ISA_aarch64 implements ISATarget {
    GENERIC("_generic", true)
    ;

    private final String suffix;
    private final boolean nativelySupported;

    ISA_aarch64(String suffix, boolean nativelySupported) {
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
