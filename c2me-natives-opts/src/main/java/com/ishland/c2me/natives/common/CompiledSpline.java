package com.ishland.c2me.natives.common;


public interface CompiledSpline {

    long getSplinePointer();

    void compileIfNeeded(boolean includeParents);

    default String getCompilationFailedReason() {
        return null;
    }

}
