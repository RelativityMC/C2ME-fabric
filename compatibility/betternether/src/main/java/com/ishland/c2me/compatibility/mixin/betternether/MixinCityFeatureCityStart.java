package com.ishland.c2me.compatibility.mixin.betternether;

import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import paulevs.betternether.world.structures.CityFeature;
import paulevs.betternether.world.structures.city.CityGenerator;

@Mixin(CityFeature.CityStart.class)
public abstract class MixinCityFeatureCityStart extends StructureStart<DefaultFeatureConfig> {

    private static final ThreadLocal<CityGenerator> cityGeneratorThreadLocal = ThreadLocal.withInitial(CityGenerator::new);

    public MixinCityFeatureCityStart(StructureFeature<DefaultFeatureConfig> feature, ChunkPos pos, int references, long seed) {
        super(feature, pos, references, seed);
    }

    // Wrong autocomplete
    @Dynamic
    @Redirect(method = "init", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/structures/CityFeature;generator:Lpaulevs/betternether/world/structures/city/CityGenerator;", opcode = Opcodes.GETSTATIC), remap = false)
    private CityGenerator redirectCityGenerator() {
        return cityGeneratorThreadLocal.get();
    }

}
