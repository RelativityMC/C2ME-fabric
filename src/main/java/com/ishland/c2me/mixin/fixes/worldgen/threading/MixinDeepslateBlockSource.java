package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.threading.worldgen.ThreadLocalChunkRandom;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.DeepslateBlockSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DeepslateBlockSource.class)
public class MixinDeepslateBlockSource {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/gen/ChunkRandom"))
    private ChunkRandom redirectNewChunkRandom(long seed) {
        return new ThreadLocalChunkRandom(seed);
    }

}
