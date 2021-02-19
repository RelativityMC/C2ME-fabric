package org.yatopiamc.c2me.mixin.optimization.worldgen.threadlocal_block_interpolator;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GrimstoneInterpolator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrimstoneInterpolator.class)
public class MixinGrimstoneInterpolator {

    @Shadow @Final private BlockState defaultBlock;
    @Shadow @Final private BlockState grimstone;
    @Shadow @Final private long seed;
    private ThreadLocal<ChunkRandom> chunkRandomThreadLocal = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(long seed, BlockState defaultBlock, BlockState grimstone, CallbackInfo ci) {
        chunkRandomThreadLocal = ThreadLocal.withInitial(() -> new ChunkRandom(seed));
    }

    /**
     * @author ishland
     * @reason use thread local
     */
    @Overwrite
    public BlockState sample(int x, int y, int z, ChunkGeneratorSettings settings) {
        // [VanillaCopy]
        if (!settings.hasGrimstone()) {
            return this.defaultBlock;
        } else {
            final ChunkRandom chunkRandom = this.chunkRandomThreadLocal.get(); // C2ME - use thread local
            chunkRandom.setGrimstoneSeed(this.seed, x, y, z);
            double d = MathHelper.clampedLerpFromProgress((double)y, -8.0D, 0.0D, 1.0D, 0.0D);
            return (double) chunkRandom.nextFloat() < d ? this.grimstone : this.defaultBlock;
        }
    }

}
