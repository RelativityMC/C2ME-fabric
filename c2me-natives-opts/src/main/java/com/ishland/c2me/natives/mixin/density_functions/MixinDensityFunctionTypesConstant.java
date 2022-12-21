package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DensityFunctionTypes.Constant.class)
public class MixinDensityFunctionTypesConstant implements CompiledDensityFunctionImpl {

    @Shadow @Final private double value;
    @Unique
    private long pointer = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
//        System.out.println("Compiling density function: constant %s".formatted(this));
        this.pointer = NativeInterface.createDFIConstant(this.value);
        NativeMemoryTracker.registerAllocatedMemory(this, NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_constant_data, this.pointer);
    }

    @Override
    public void compileIfNeeded(boolean includeParents) {
        // no-op: always compilable
    }

    @Override
    public long getDFIPointer() {
        return this.pointer;
    }

}
