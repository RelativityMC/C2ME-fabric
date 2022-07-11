package com.ishland.c2me.natives.common;

import javax.annotation.Nullable;

public interface CompiledSpline {

    long getSplinePointer();

    @Nullable
    default String getCompilationFailedReason() {
        return null;
    }

}
