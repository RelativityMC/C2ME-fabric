package com.ishland.c2me.natives.common;

import javax.annotation.Nullable;

public interface CompiledDensityFunctionImpl {

    default void initializeHook() {
        DeferredCompilationUtil.deferCompilation(this::runDeferredCompilation);
    }

    void runDeferredCompilation();

    long getDFIPointer();

    @Nullable
    default String getCompilationFailedReason() {
        return null;
    }

    default Type getDFIType() {
        return Type.COMPILED;
    }

    enum Type {
        COMPILED("compile"),
        PASS_THROUGH("pass-through");

        private final String verb;

        Type(String verb) {
            this.verb = verb;
        }

        public String verb() {
            return verb;
        }
    }

}
