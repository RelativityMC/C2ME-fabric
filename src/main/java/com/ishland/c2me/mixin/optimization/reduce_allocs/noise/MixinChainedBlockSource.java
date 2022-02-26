package com.ishland.c2me.mixin.optimization.reduce_allocs.noise;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChainedBlockSource.class)
public class MixinChainedBlockSource {

    @Unique
    private ChunkNoiseSampler.BlockStateSampler[] samplersArray;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(List<ChunkNoiseSampler.BlockStateSampler> samplers, CallbackInfo ci) {
        this.samplersArray = samplers.toArray(ChunkNoiseSampler.BlockStateSampler[]::new);
    }

    /**
     * @author ishland
     * @reason reduce allocs using array
     */
    @Overwrite
    public @Nullable BlockState sample(DensityFunction.NoisePos arg) {
        // TODO [VanillaCopy]
        for (ChunkNoiseSampler.BlockStateSampler blockStateSampler : this.samplersArray) {
            BlockState blockState = blockStateSampler.sample(arg);
            if (blockState != null) {
                return blockState;
            }
        }

        return null;
    }

}
