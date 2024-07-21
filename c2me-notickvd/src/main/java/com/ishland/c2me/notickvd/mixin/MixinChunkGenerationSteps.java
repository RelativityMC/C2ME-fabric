package com.ishland.c2me.notickvd.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkGenerationSteps.class)
public class MixinChunkGenerationSteps {

    @ModifyReturnValue(method = {"method_60531", "method_60519"}, at = @At("RETURN"), require = 2)
    private static ChunkGenerationStep.Builder requireNeighborsLit(ChunkGenerationStep.Builder original) {
        return original.dependsOn(ChunkStatus.LIGHT, 1);
    }

}
