package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.threading.worldgen.ThreadLocalChunkRandom;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunkGenerator.OreVeinSource.class)
public class MixinOreVeinSource {

    @Mutable
    @Shadow @Final private ChunkRandom random;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.random = new ThreadLocalChunkRandom(System.nanoTime());
    }

}
