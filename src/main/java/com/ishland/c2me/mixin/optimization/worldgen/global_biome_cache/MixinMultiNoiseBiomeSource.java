package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.GlobalCachingMultiNoiseBiomeSource;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Function9;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.class_6452;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(MultiNoiseBiomeSource.class)
public class MixinMultiNoiseBiomeSource {

    @Redirect(method = "method_35242", at = @At(value = "NEW", target = "net/minecraft/world/biome/source/MultiNoiseBiomeSource"))
    private static MultiNoiseBiomeSource redirectConstruct(long l,
                                                           class_6452.class_6455<Biome> arg,
                                                           MultiNoiseBiomeSource.NoiseParameters noiseParameters,
                                                           MultiNoiseBiomeSource.NoiseParameters noiseParameters2,
                                                           MultiNoiseBiomeSource.NoiseParameters noiseParameters3,
                                                           MultiNoiseBiomeSource.NoiseParameters noiseParameters4,
                                                           MultiNoiseBiomeSource.NoiseParameters noiseParameters5,
                                                           int i,
                                                           int j,
                                                           boolean bl,
                                                           Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> optional) {
        return new GlobalCachingMultiNoiseBiomeSource(l, arg, noiseParameters, noiseParameters2, noiseParameters3, noiseParameters4, noiseParameters5, i, j, bl, optional);
    }

    @Dynamic
    @Redirect(method = "method_37688", at = @At(value = "INVOKE", target = "com/mojang/datafixers/Products$P9.apply(Lcom/mojang/datafixers/kinds/Applicative;Lcom/mojang/datafixers/util/Function9;)Lcom/mojang/datafixers/kinds/App;"))
    private static App redirectDeserialize(Products.P9<RecordCodecBuilder.Mu<MultiNoiseBiomeSource>, Long, class_6452.class_6455<Biome>, MultiNoiseBiomeSource.NoiseParameters, MultiNoiseBiomeSource.NoiseParameters, MultiNoiseBiomeSource.NoiseParameters, MultiNoiseBiomeSource.NoiseParameters, MultiNoiseBiomeSource.NoiseParameters, Integer, Integer> p9, Applicative applicative, Function9 function9) {
        return p9.apply(applicative, (aLong, class_6455, noiseParameters, noiseParameters2, noiseParameters3, noiseParameters4, noiseParameters5, integer, integer2) -> {
            return new GlobalCachingMultiNoiseBiomeSource(aLong, class_6455, noiseParameters, noiseParameters2, noiseParameters3, noiseParameters4, noiseParameters5, integer, integer, false, Optional.empty());
        });
    }

}
