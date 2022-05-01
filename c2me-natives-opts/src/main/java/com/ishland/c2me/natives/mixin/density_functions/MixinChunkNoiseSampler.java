package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.UnsafeUtil;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkNoiseSampler.class)
public class MixinChunkNoiseSampler implements CompiledDensityFunctionArg {

    @Shadow @Final private int horizontalBlockSize;
    @Shadow @Final private int verticalBlockSize;
    @Shadow private int field_36594;
    @Shadow private int field_36572;
    @Shadow private int field_36573;
    @Shadow @Final private int minimumY;
    @Shadow @Final private int height;
    @Shadow private int field_36574;
    @Shadow private int field_36575;
    @Shadow private int field_36576;
    @Unique
    private long pointer = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.pointer = NativeInterface.createChunkNoiseSamplerDataEmpty();
        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativeInterface.SIZEOF_density_function_multi_pos_args_data + NativeInterface.SIZEOF_chunk_noise_sampler_data,
                this.pointer
        );
    }


    @SuppressWarnings({"PointlessArithmeticExpression", "SuspiciousNameCombination"})
    @Override
    public long getDFAPointer() {
        // update contents in native before returning
        //
        // Offsets:
        // horizontalBlockSize: 0
        // verticalBlockSize: 4
        // baseX: 8
        // baseY: 12
        // baseZ: 16
        // offsetX: 20
        // offsetY: 24
        // offsetZ: 28
        // minimumY: 32
        // height: 36

        final long data_start = this.pointer + NativeInterface.SIZEOF_density_function_multi_pos_args_data;
        UnsafeUtil.getInstance().putInt(data_start + 0, this.horizontalBlockSize);
        UnsafeUtil.getInstance().putInt(data_start + 4, this.verticalBlockSize);
        UnsafeUtil.getInstance().putInt(data_start + 8, this.field_36594);
        UnsafeUtil.getInstance().putInt(data_start + 12, this.field_36572);
        UnsafeUtil.getInstance().putInt(data_start + 16, this.field_36573);
        UnsafeUtil.getInstance().putInt(data_start + 20, this.field_36574);
        UnsafeUtil.getInstance().putInt(data_start + 24, this.field_36575);
        UnsafeUtil.getInstance().putInt(data_start + 28, this.field_36576);
        UnsafeUtil.getInstance().putInt(data_start + 32, this.minimumY);
        UnsafeUtil.getInstance().putInt(data_start + 36, this.height);

        return this.pointer;
    }
}
