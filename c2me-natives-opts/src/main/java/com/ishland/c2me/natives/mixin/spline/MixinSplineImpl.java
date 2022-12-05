package com.ishland.c2me.natives.mixin.spline;

import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.CompiledSpline;
import com.ishland.c2me.natives.common.DensityFunctionUtils;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.UnsafeUtil;
import net.minecraft.util.function.ToFloatFunction;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.List;

@Mixin(Spline.Implementation.class)
public abstract class MixinSplineImpl<C, I extends ToFloatFunction<C>> implements CompiledSpline {

    @Shadow @Final private I locationFunction;

    @Shadow public abstract List<Spline<C, I>> values();

    @Shadow @Final private float[] locations;
    @Shadow @Final private List<Spline<C, I>> values;
    @Shadow @Final private float[] derivatives;

    @Shadow
    static <C, I extends ToFloatFunction<C>> Spline.Implementation<C, I> build(I toFloatFunction, float[] fs, List<Spline<C, I>> list, float[] gs) {
        throw new AbstractMethodError();
    }

    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        if (!DensityFunctionUtils.isCompiled(this.locationFunction) || !DensityFunctionUtils.isCompiled(this.values().toArray())) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(this, this.locationFunction, this.values);
                System.err.println("Failed to compile spline %s: ".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }

        final int length = this.locations.length;

        long ptr_locations = NativeMemoryTracker.allocateMemory(this, length * 4L);
        UnsafeUtil.getInstance().copyMemory(this.locations, Unsafe.ARRAY_SHORT_BASE_OFFSET, null, ptr_locations, length * 4L);

        long ptr_values = NativeMemoryTracker.allocateMemory(this, length * 8L);
        for (int i = 0; i < length; i ++) {
            final long splinePointer = ((CompiledSpline) this.values.get(i)).getSplinePointer();
            if (splinePointer == 0L) throw new NullPointerException();
            UnsafeUtil.getInstance().putLong(ptr_values + 8L * i, splinePointer);
        }

        long ptr_derivatives = NativeMemoryTracker.allocateMemory(this, length * 4L);
        UnsafeUtil.getInstance().copyMemory(this.derivatives, Unsafe.ARRAY_SHORT_BASE_OFFSET, null, ptr_derivatives, length * 4L);

        this.pointer = NativeInterface.createSplineImpl(
                ((CompiledDensityFunctionImpl) ((DensityFunctionTypes.Spline.DensityFunctionWrapper) this.locationFunction).function().value()).getDFIPointer(),
                ptr_locations,
                length,
                ptr_values,
                ptr_derivatives
        );
        NativeMemoryTracker.registerAllocatedMemory(this, NativeInterface.SIZEOF_spline_data_impl, this.pointer);
    }

    @Override
    public long getSplinePointer() {
        return this.pointer;
    }

    @Nullable
    @Override
    public String getCompilationFailedReason() {
        return this.errorMessage;
    }

    /**
     * @author ishland
     * @reason reduce allocs
     */
    @Overwrite
    public Spline<C, I> apply(Spline.Visitor<I> arg) {
        boolean hasChanges = false;
        final List<Spline<C, I>> list = new ArrayList<>();
        for (Spline<C, I> spline : this.values()) {
            Spline<C, I> ciSpline = spline.apply(arg);
            if (ciSpline != spline) hasChanges = true;
            list.add(ciSpline);
        }
        final I visit = arg.visit(this.locationFunction);
        if (visit != this.locationFunction) hasChanges = true;

        if (!hasChanges) return (Spline<C, I>) this;

        return build(
                visit, this.locations, list, this.derivatives
        );
    }

}
