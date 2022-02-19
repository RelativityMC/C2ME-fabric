package com.ishland.c2me.mixin.fixes.worldgen.threading;

import net.minecraft.class_7059;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(NoiseChunkGenerator.class)
public abstract class MixinNoiseChunkGenerator extends ChunkGenerator {
    public MixinNoiseChunkGenerator(Registry<class_7059> registry, Optional<RegistryEntryList<class_7059>> optional, BiomeSource biomeSource) {
        super(registry, optional, biomeSource);
    }

//    @Dynamic
//    @Redirect(method = "*", at = @At(value = "NEW", target = "net/minecraft/world/gen/StructureWeightSampler"), require = 4)
//    private static StructureWeightSampler redirectStructureWeightSamplers(StructureAccessor structureAccessor, Chunk chunk) {
//        return new ThreadLocalStructureWeightSampler(structureAccessor, chunk);
//    }

//    @Inject(method = "<init>(Lnet/minecraft/util/registry/Registry;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;JLjava/util/function/Supplier;)V", at = @At(value = "RETURN"))
//    private void onInit(CallbackInfo info) {
//        ((IChunkGenerator) this).invokeGenerateStrongholdPositions();
//        System.out.println("Stronghold positions initialized");
//    }

}
