package com.ishland.c2me.natives.common;

import javax.annotation.Nullable;

public interface CompiledDensityFunctionImpl {

    long getDFIPointer();

    @Nullable
    default String getCompilationFailedReason() {
        return null;
    }

}
