package com.ishland.c2me.natives.common;

public interface CompiledDensityFunctionImpl {

    long getDFIPointer();

    void compileIfNeeded(boolean includeParents);

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
