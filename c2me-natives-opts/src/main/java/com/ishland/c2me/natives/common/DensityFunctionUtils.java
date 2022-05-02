package com.ishland.c2me.natives.common;

import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Map;

public class DensityFunctionUtils {

    public static boolean isCompiled(DensityFunction... function) {
        for (DensityFunction df : function) {
            if (df instanceof CompiledDensityFunctionImpl dfi) {
                if (dfi.getDFIPointer() == 0L) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    private static String getErrorMessage(DensityFunction function) {
        if (function instanceof CompiledDensityFunctionImpl dfi) {
            if (dfi.getCompilationFailedReason() != null) {
                return String.format("Parent (%s) failed to compile: \n    %s", function.getClass().getName(), indent(dfi.getCompilationFailedReason()));
            } else if (dfi.getDFIPointer() == 0L) {
                return String.format("Parent (%s) failed to compile for unknown reasons", function.getClass().getName());
            }
            return null;
        } else {
            return String.format("Parent (%s) can't be compiled", function.getClass().getName());
        }
    }

    public static String getErrorMessage(DensityFunction owner, Map<String, DensityFunction> map) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Density function (%s) failed to compile for these reasons: \n", owner.getClass().getName()));
        boolean hasFailures = false;
        for (Map.Entry<String, DensityFunction> entry : map.entrySet()) {
            String error = getErrorMessage(entry.getValue());
            if (error != null) {
                hasFailures = true;
                sb.append(String.format("%s: \n    %s\n", entry.getKey(), indent(error)));
            }
        }
        return hasFailures ? sb.toString() : null;
    }

    public static String indent(String s) {
        return s.replaceAll("\n", "\n    ");
    }

}
