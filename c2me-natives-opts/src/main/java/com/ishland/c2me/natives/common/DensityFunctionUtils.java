package com.ishland.c2me.natives.common;

import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.Iterator;
import java.util.Map;

public class DensityFunctionUtils {

    public static final boolean DEBUG = Boolean.getBoolean("com.ishland.c2me.natives.debug");

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
                return String.format("Parent (%s) failed to %s: \n    %s", function.getClass().getName(), dfi.getDFIType().verb(), indent(dfi.getCompilationFailedReason(), false));
            } else if (dfi.getDFIPointer() == 0L) {
                return String.format("Parent (%s) failed to %s for unknown reasons", function.getClass().getName(), dfi.getDFIType().verb());
            }
            return null;
        } else {
            return String.format("Parent (%s) can't be compiled", function.getClass().getName());
        }
    }

    public static String getErrorMessage(CompiledDensityFunctionImpl owner, Map<String, DensityFunction> map) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Density function (%s) failed to %s for these reasons: \n", owner.getClass().getName(), owner.getDFIType().verb()));
        boolean hasFailures = false;
        for (Iterator<Map.Entry<String, DensityFunction>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, DensityFunction> entry = iterator.next();
            String error = getErrorMessage(entry.getValue());
            if (error != null) {
                hasFailures = true;
                sb.append(String.format("%s: \n%s   %s\n", entry.getKey(), iterator.hasNext() ? "|" : " ", indent(error, iterator.hasNext())));
            }
        }
        return hasFailures ? sb.toString().stripTrailing() : null;
    }

    public static String indent(String s, boolean needAlignLine) {
        if (needAlignLine)
            return s.replaceAll("\n", "\n|   ");
        else
            return s.replaceAll("\n", "\n    ");
    }

    public static short mapOperationToNative(DensityFunctionTypes.Operation.Type type) {
        return switch (type) {
            case ADD -> 0;
            case MUL -> 1;
            case MAX -> 2;
            case MIN -> 3;
        };
    }

}
