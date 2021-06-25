package com.ishland.c2me.mixin.threading.worldgen.fixes.chunk_random;

import com.ishland.c2me.common.threading.worldgen.ThreadLocalChunkRandom;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoiseChunkGenerator.OreVeinSource.class)
public class MixinOreVeinSource {

    @Dynamic
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/gen/ChunkRandom"))
    private ChunkRandom redirectNewChunkRandom() {
        return new ThreadLocalChunkRandom(System.nanoTime());
    }

}
