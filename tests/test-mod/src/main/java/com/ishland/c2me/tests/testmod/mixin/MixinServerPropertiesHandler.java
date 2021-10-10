package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.util.Properties;

@Mixin(ServerPropertiesHandler.class)
public class MixinServerPropertiesHandler {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/GeneratorOptions;fromProperties(Lnet/minecraft/util/registry/DynamicRegistryManager;Ljava/util/Properties;)Lnet/minecraft/world/gen/GeneratorOptions;"))
    private GeneratorOptions redirectGeneratorOptions(DynamicRegistryManager registryManager, Properties properties) throws IOException {
        final Properties properties1 = new Properties();
        properties1.put("level-seed", "c2metest");
        final GeneratorOptions generatorOptions = GeneratorOptions.fromProperties(registryManager, properties1);
        properties1.store(System.err, "C2ME Test Generated Generator Settings");
        return generatorOptions;
    }

}
