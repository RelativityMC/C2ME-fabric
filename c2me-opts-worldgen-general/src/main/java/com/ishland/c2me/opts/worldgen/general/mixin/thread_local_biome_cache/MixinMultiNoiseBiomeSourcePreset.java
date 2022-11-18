package com.ishland.c2me.opts.worldgen.general.mixin.thread_local_biome_cache;

import com.ishland.c2me.opts.worldgen.general.common.threadlocal_biome_cache.ThreadLocalCachingMultiNoiseBiomeSource;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(MultiNoiseBiomeSource.Preset.class)
public class MixinMultiNoiseBiomeSourcePreset {

    @Dynamic
    @Redirect(method = "getBiomeSource(Lnet/minecraft/world/biome/source/MultiNoiseBiomeSource$Instance;Z)Lnet/minecraft/world/biome/source/MultiNoiseBiomeSource;", at = @At(value = "NEW", target = "net/minecraft/world/biome/source/MultiNoiseBiomeSource"))
    private static MultiNoiseBiomeSource redirectConstruct(MultiNoiseUtil.Entries<RegistryEntry<Biome>> entries, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<MultiNoiseBiomeSource.Instance> optional) {
        return new ThreadLocalCachingMultiNoiseBiomeSource(entries, optional);
    }

}
