package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import com.ishland.c2me.common.threading.worldgen.fixes.threading_fixes.ThreadLocalNoiseInterpolator;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.NoiseInterpolator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoiseChunkGenerator.NoodleCavesSampler.class)
public class MixinNoiseChunkGeneratorNoodleCavesSampler {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/gen/NoiseInterpolator"))
    private NoiseInterpolator redirectNoiseInterpolator(int sizeX, int sizeY, int sizeZ, ChunkPos pos, int minY, NoiseInterpolator.ColumnSampler columnSampler) {
        return new ThreadLocalNoiseInterpolator(sizeX, sizeY, sizeZ, pos, minY, columnSampler);
    }

}
