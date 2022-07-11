package com.ishland.c2me.natives.common;

import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DensityFunctionUtils {

    public static final boolean DEBUG = Boolean.getBoolean("com.ishland.c2me.natives.debug");

    public static boolean isSafeForNative(DensityFunction.NoisePos pos) {
        return pos.getBlender() == Blender.getNoBlending();
    }

    public static boolean isSafeForNative(DensityFunction.EachApplier dfa) {
        if (dfa instanceof ChunkNoiseSampler sampler) {
            return sampler.getBlender() == Blender.getNoBlending();
        }
        return true;
    }

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

    public static <C, I extends ToFloatFunction<C>> boolean isCompiled(Spline<C, I>... splines) {
        for (Spline<C, I> spline : splines) {
            if (spline instanceof CompiledSpline compiledSpline) {
                if (compiledSpline.getSplinePointer() == 0L) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    private static String getErrorMessage(Object object) {
        if (object instanceof DensityFunction df) {
            return getErrorMessage(df);
        } else if (object instanceof Spline<?,?> spline) {
            return getErrorMessage(spline);
        } else {
            return "unknown type " + object.getClass().getName();
        }
    }

    public static boolean isCompiled(Object... objects) {
        for (Object object : objects) {
            if (object instanceof DensityFunction df) {
                if (!isCompiled(df)) return false;
            } else if (object instanceof Spline<?,?> spline) {
                if (!isCompiled(spline)) return false;
            } else if (object instanceof DensityFunctionTypes.Spline.DensityFunctionWrapper wrapper) {
                if (wrapper.function().value() == null || !isCompiled(wrapper.function().value())) return false;
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

    public static String getErrorMessage(CompiledDensityFunctionImpl owner, Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Density function (%s) failed to %s for these reasons: \n", owner.getClass().getName(), owner.getDFIType().verb()));
        boolean hasFailures = false;
        final Iterator<Map.Entry<String, Object>> iterator = map.entrySet().stream().filter(entry -> !isCompiled(entry.getValue())).iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String error = getErrorMessage(entry.getValue());
            if (error != null) {
                hasFailures = true;
                sb.append(String.format("%s: \n%s   %s\n", entry.getKey(), iterator.hasNext() ? "|" : " ", indent(error, iterator.hasNext())));
            }
        }
        return hasFailures ? sb.toString().stripTrailing() : null;
    }

    public static <C, I extends ToFloatFunction<C>> String getErrorMessage(Spline<C, I> spline) {
        if (spline instanceof CompiledSpline compiledSpline) {
            if (compiledSpline.getCompilationFailedReason() != null) {
                return String.format("Spline (%s) failed to compile: \n    %s", spline.getClass().getName(), indent(compiledSpline.getCompilationFailedReason(), false));
            } else if (compiledSpline.getSplinePointer() == 0L) {
                return String.format("Spline (%s) failed to compile for unknown reasons", spline.getClass().getName());
            }
            return null;
        } else {
            return String.format("Spline (%s) can't be compiled", spline.getClass().getName());
        }
    }

    public static <C, I extends ToFloatFunction<C>> String getErrorMessage(CompiledSpline spline, Object locationFunction, List<Spline<C, I>> values) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Spline (%s) failed to compile for these reasons: \n", spline.getClass().getName()));
        boolean hasFailures = false;
        final Iterator<Object> iterator = Stream.concat(values.stream(), Stream.of(locationFunction))
                .filter(entry -> !isCompiled(entry)).iterator();
        while (iterator.hasNext()) {
            Object entry = iterator.next();
            String error = getErrorMessage(entry);
            if (error != null) {
                hasFailures = true;
                sb.append(String.format("%s: \n%s   %s\n", entry, iterator.hasNext() ? "|" : " ", indent(error, iterator.hasNext())));
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

    public static short mapOperationToNative(DensityFunctionTypes.BinaryOperationLike.Type type) {
        return switch (type) {
            case ADD -> 0;
            case MUL -> 1;
            case MAX -> 2;
            case MIN -> 3;
        };
    }

    public static short mapSingleOperationToNative(DensityFunctionTypes.UnaryOperation.Type type) {
        return switch (type) {
            case ABS -> 0;
            case SQUARE -> 1;
            case CUBE -> 2;
            case HALF_NEGATIVE -> 3;
            case QUARTER_NEGATIVE -> 4;
            case SQUEEZE -> 5;
        };
    }

}
