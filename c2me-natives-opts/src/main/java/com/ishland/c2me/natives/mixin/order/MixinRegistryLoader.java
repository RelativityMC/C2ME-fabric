package com.ishland.c2me.natives.mixin.order;

import com.ishland.c2me.natives.common.DensityFunctionUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Mixin(RegistryLoader.class)
public class MixinRegistryLoader {

    @Mutable
    @Shadow @Final public static List<RegistryLoader.Entry<?>> DYNAMIC_REGISTRIES;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo ci) {
        final ArrayList<RegistryLoader.Entry<?>> entries = new ArrayList<>(DYNAMIC_REGISTRIES);
        final ListIterator<RegistryLoader.Entry<?>> iterator = entries.listIterator();
        int noise_settings_index = 0;
        int density_function_index = 0;
        while (iterator.hasNext()) {
            final RegistryLoader.Entry<?> entry = iterator.next();
            if (entry.key().equals(RegistryKeys.CHUNK_GENERATOR_SETTINGS)) {
                noise_settings_index = iterator.previousIndex();
            } else if (entry.key().equals(RegistryKeys.DENSITY_FUNCTION)) {
                density_function_index = iterator.previousIndex();
            }
        }
        if (density_function_index > noise_settings_index) {
            final RegistryLoader.Entry<?> noise_settings_entry = entries.remove(noise_settings_index);
            entries.add(density_function_index, noise_settings_entry);
        }
        DYNAMIC_REGISTRIES = List.copyOf(entries);
    }

    @Inject(method = "method_45128", at = @At("RETURN"))
    private static void postFreeze(Map<RegistryKey<?>, Exception> map, Pair<MutableRegistry<?>, ?> loader, CallbackInfo ci) {
        if (loader.getFirst().getKey().equals(RegistryKeys.DENSITY_FUNCTION)) {
            System.out.println("=".repeat(80));
            final MutableRegistry<DensityFunction> registry = (MutableRegistry<DensityFunction>) loader.getFirst();
            for (DensityFunction function : registry) {
                DensityFunctionUtils.triggerCompilationIfNeeded(function);
            }
            System.out.println("=".repeat(80));
        }
    }

}
