package com.ishland.c2me.mixin.optimization.reduce_allocs.noise;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.BlockSource;
import net.minecraft.world.gen.ChainedBlockSource;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
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
    private BlockSource[] samplersArray;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(List<BlockSource> samplers, CallbackInfo ci) {
        this.samplersArray = samplers.toArray(BlockSource[]::new);
    }

    /**
     * @author ishland
     * @reason reduce allocs using array
     */
    @Overwrite
    public @Nullable BlockState apply(ChunkNoiseSampler chunkNoiseSampler, int i, int j, int k) {
        // TODO [VanillaCopy]
        for (BlockSource blockSource : this.samplersArray) { // iterate array
            BlockState blockState = blockSource.apply(chunkNoiseSampler, i, j, k);
            if (blockState != null) {
                return blockState;
            }
        }

        return null;
    }

}
