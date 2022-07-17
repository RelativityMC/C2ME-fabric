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
    @Shadow private int startBlockX;
    @Shadow private int startBlockY;
    @Shadow private int startBlockZ;
    @Shadow @Final private int minimumCellY;
    @Shadow @Final private int cellHeight;
    @Shadow private int cellBlockX;
    @Shadow private int cellBlockY;
    @Shadow private int cellBlockZ;
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
        UnsafeUtil.getInstance().putInt(data_start + 8, this.startBlockX);
        UnsafeUtil.getInstance().putInt(data_start + 12, this.startBlockY);
        UnsafeUtil.getInstance().putInt(data_start + 16, this.startBlockZ);
        UnsafeUtil.getInstance().putInt(data_start + 20, this.cellBlockX);
        UnsafeUtil.getInstance().putInt(data_start + 24, this.cellBlockY);
        UnsafeUtil.getInstance().putInt(data_start + 28, this.cellBlockZ);
        UnsafeUtil.getInstance().putInt(data_start + 32, this.minimumCellY);
        UnsafeUtil.getInstance().putInt(data_start + 36, this.cellHeight);

        return this.pointer;
    }
}
