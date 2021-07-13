package com.ishland.c2me.compatibility.mixin.betterend;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bclib.world.generator.BiomeMap;
import ru.betterend.registry.EndBiomes;
import ru.betterend.world.generator.BetterEndBiomeSource;
import ru.betterend.world.generator.GeneratorOptions;

@Mixin(BetterEndBiomeSource.class)
public class MixinBetterEndBiomeSource {

    private ThreadLocal<BiomeMap> mapLandThreadLocal = new ThreadLocal<>();
    private ThreadLocal<BiomeMap> mapVoidThreadLocal = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Registry<Biome> biomeRegistry, long seed, CallbackInfo ci) {
        mapLandThreadLocal = ThreadLocal.withInitial(() -> new BiomeMap(seed, GeneratorOptions.getBiomeSizeLand(), EndBiomes.LAND_BIOMES)); // TODO [VanillaCopy]
        mapVoidThreadLocal = ThreadLocal.withInitial(() -> new BiomeMap(seed, GeneratorOptions.getBiomeSizeVoid(), EndBiomes.VOID_BIOMES)); // TODO [VanillaCopy]
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lru/betterend/world/generator/BetterEndBiomeSource;mapLand:Lru/bclib/world/generator/BiomeMap;", opcode = Opcodes.GETFIELD))
    private BiomeMap redirectMapLand(BetterEndBiomeSource betterEndBiomeSource) {
        return mapLandThreadLocal.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lru/betterend/world/generator/BetterEndBiomeSource;mapVoid:Lru/bclib/world/generator/BiomeMap;", opcode = Opcodes.GETFIELD))
    private BiomeMap redirectMapVoid(BetterEndBiomeSource betterEndBiomeSource) {
        return mapVoidThreadLocal.get();
    }

}
