package com.ishland.c2me.opts.worldgen.general.mixin.random_instances;

import com.ishland.c2me.opts.worldgen.general.common.random_instances.SimplifiedAtomicSimpleRandom;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.feature.GeodeFeature;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {
        NoiseChunkGenerator.class,
        GeodeFeature.class,
        ChunkGenerator.class,
        RandomSpreadStructurePlacement.class,
})
public class MixinRedirectAtomicSimpleRandom {

    @Redirect(method = "*", at = @At(value = "NEW", target = "net/minecraft/world/gen/random/AtomicSimpleRandom"))
    private AtomicSimpleRandom redirectAtomicSimpleRandom(long l) {
        return new SimplifiedAtomicSimpleRandom(l);
    }

}
