package com.ishland.c2me.mixin.optimization.worldgen.threadlocal_block_interpolator;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.DeepslateBlockSource;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(DeepslateBlockSource.class)
public class MixinDeepslateInterpolator {

    private ThreadLocal<ChunkRandom> chunkRandomThreadLocal = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, BlockState defaultBlock, BlockState deepslateState, ChunkGeneratorSettings chunkGeneratorSettings, CallbackInfo ci) {
        chunkRandomThreadLocal = ThreadLocal.withInitial(() -> new ChunkRandom(seed));
    }

    @Redirect(method = "sample", at = @At(value = "FIELD", target = "Lnet/minecraft/world/gen/DeepslateBlockSource;random:Lnet/minecraft/world/gen/ChunkRandom;"))
    private ChunkRandom redirectRandomUsage(DeepslateBlockSource source) {
        return chunkRandomThreadLocal.get();
    }

}
