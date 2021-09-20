package com.ishland.c2me.mixin.optimization.worldgen.thread_local_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.threadlocal_biome_cache.ThreadLocalCachingMultiNoiseBiomeSource;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.registry.Registry;
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
    @Redirect(method = "method_38175", at = @At(value = "NEW", target = "net/minecraft/world/biome/source/MultiNoiseBiomeSource"))
    private static MultiNoiseBiomeSource redirectConstructNether(MultiNoiseUtil.Entries<Biome> entries, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> optional) {
        return new ThreadLocalCachingMultiNoiseBiomeSource(entries, optional);
    }

    @Dynamic
    @Redirect(method = "method_31088", at = @At(value = "NEW", target = "net/minecraft/world/biome/source/MultiNoiseBiomeSource"))
    private static MultiNoiseBiomeSource redirectConstructOverWorld(MultiNoiseUtil.Entries<Biome> entries, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> optional) {
        return new ThreadLocalCachingMultiNoiseBiomeSource(entries, optional);
    }

}
