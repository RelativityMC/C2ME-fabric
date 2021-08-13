package com.ishland.c2me.compatibility.mixin.betternether;

import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import paulevs.betternether.world.structures.CityFeature;
import paulevs.betternether.world.structures.city.CityGenerator;
import paulevs.betternether.world.structures.city.palette.CityPalette;
import paulevs.betternether.world.structures.piece.CityPiece;

import java.util.ArrayList;
import java.util.Random;

@Mixin(CityFeature.CityStart.class)
public abstract class MixinCityFeatureCityStart extends StructureStart<DefaultFeatureConfig> {

    private static final ThreadLocal<CityGenerator> cityGeneratorThreadLocal = ThreadLocal.withInitial(CityGenerator::new);

    public MixinCityFeatureCityStart(StructureFeature<DefaultFeatureConfig> feature, ChunkPos pos, int references, long seed) {
        super(feature, pos, references, seed);
    }

    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lpaulevs/betternether/world/structures/city/CityGenerator;generate(Lnet/minecraft/util/math/BlockPos;Ljava/util/Random;Lpaulevs/betternether/world/structures/city/palette/CityPalette;)Ljava/util/ArrayList;"), remap = false)
    private ArrayList<CityPiece> redirectGenerate(CityGenerator cityGenerator, BlockPos pos, Random random, CityPalette palette) {
        return cityGeneratorThreadLocal.get().generate(pos, random, palette);
    }

}
