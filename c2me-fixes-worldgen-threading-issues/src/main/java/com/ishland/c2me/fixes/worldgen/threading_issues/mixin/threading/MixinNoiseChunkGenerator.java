package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.structure.StructureSet;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(NoiseChunkGenerator.class)
public abstract class MixinNoiseChunkGenerator extends ChunkGenerator {
    public MixinNoiseChunkGenerator(Registry<StructureSet> registry, Optional<RegistryEntryList<StructureSet>> optional, BiomeSource biomeSource) {
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
