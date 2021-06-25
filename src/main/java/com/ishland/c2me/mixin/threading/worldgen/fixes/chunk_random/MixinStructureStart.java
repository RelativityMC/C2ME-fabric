package com.ishland.c2me.mixin.threading.worldgen.fixes.chunk_random;

import com.ishland.c2me.common.threading.worldgen.ThreadLocalChunkRandom;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public class MixinStructureStart<C extends FeatureConfig> {

    @Mutable
    @Shadow
    @Final
    protected ChunkRandom random;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(StructureFeature<C> feature, ChunkPos pos, int references, long seed, CallbackInfo ci) {
        this.random = new ThreadLocalChunkRandom(seed,
                chunkRandom -> chunkRandom.setCarverSeed(seed, pos.x, pos.z) // TODO [VanillaCopy]
        );
    }

}
