package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.NativeStruct;
import com.ishland.c2me.natives.common.NativesInterface;
import com.ishland.c2me.natives.common.UnsafeUtil;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/world/gen/chunk/ChunkNoiseSampler$1")
public class MixinChunkNoiseSampler1 implements NativeStruct {

    @Shadow @Final private ChunkNoiseSampler field_36595;

    @Unique
    private long pointer = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.pointer = NativesInterface.createChunkNoiseSampler1DataEmpty();
        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativesInterface.SIZEOF_density_function_multi_pos_args_data + NativesInterface.SIZEOF_chunk_noise_sampler_data,
                this.pointer
        );
    }

    @SuppressWarnings({"PointlessArithmeticExpression"})
    @Override
    public long getNativePointer() {
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

        final long data_start = this.pointer + NativesInterface.SIZEOF_density_function_multi_pos_args_data;
        UnsafeUtil.getInstance().putInt(data_start + 0, ((IChunkNoiseSampler) this.field_36595).getHorizontalBlockSize());
        UnsafeUtil.getInstance().putInt(data_start + 4, ((IChunkNoiseSampler) this.field_36595).getVerticalBlockSize());
        UnsafeUtil.getInstance().putInt(data_start + 8, ((IChunkNoiseSampler) this.field_36595).getBaseX());
        UnsafeUtil.getInstance().putInt(data_start + 12, ((IChunkNoiseSampler) this.field_36595).getBaseY());
        UnsafeUtil.getInstance().putInt(data_start + 16, ((IChunkNoiseSampler) this.field_36595).getBaseZ());
        UnsafeUtil.getInstance().putInt(data_start + 20, ((IChunkNoiseSampler) this.field_36595).getOffsetX());
        UnsafeUtil.getInstance().putInt(data_start + 24, ((IChunkNoiseSampler) this.field_36595).getOffsetY());
        UnsafeUtil.getInstance().putInt(data_start + 28, ((IChunkNoiseSampler) this.field_36595).getOffsetZ());
        UnsafeUtil.getInstance().putInt(data_start + 32, ((IChunkNoiseSampler) this.field_36595).getMinimumY());
        UnsafeUtil.getInstance().putInt(data_start + 36, ((IChunkNoiseSampler) this.field_36595).getHeight());

        return this.pointer;
    }
}
