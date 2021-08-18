package com.ishland.c2me.compatibility.mixin.betternether;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betternether.world.BNWorldGenerator;
import paulevs.betternether.world.BiomeMap;
import paulevs.betternether.world.NetherBiomeSource;

import java.lang.reflect.Field;

@Mixin(NetherBiomeSource.class)
public class MixinNetherBiomeSource {

    private ThreadLocal<BiomeMap> biomeMapThreadLocal = new ThreadLocal<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Registry<Biome> biomeRegistry, long seed, CallbackInfo ci) {
        this.biomeMapThreadLocal = ThreadLocal.withInitial(() -> new BiomeMap(seed, getField("biomeSizeXZ"), getField("biomeSizeY"), getField("volumetric")));
    }

    private <T> T getField(String name) {
        try {
            final Field declaredField = BNWorldGenerator.class.getDeclaredField(name);
            declaredField.setAccessible(true);
            return (T) declaredField.get(null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Redirect(method = "getBiomeForNoiseGen", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/NetherBiomeSource;map:Lpaulevs/betternether/world/BiomeMap;", opcode = Opcodes.GETFIELD))
    private BiomeMap redirectMap(NetherBiomeSource netherBiomeSource) {
        return biomeMapThreadLocal.get();
    }

}
