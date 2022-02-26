package com.ishland.c2me.tests.testmod.mixin.pregen;

import com.google.gson.JsonObject;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPropertiesHandler.class)
public class MixinServerPropertiesHandler {

    @Redirect(method = "getGeneratorOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/GeneratorOptions;fromProperties(Lnet/minecraft/util/registry/DynamicRegistryManager;Lnet/minecraft/server/dedicated/ServerPropertiesHandler$WorldGenProperties;)Lnet/minecraft/world/gen/GeneratorOptions;"))
    private GeneratorOptions redirectGeneratorOptions(DynamicRegistryManager registryManager, ServerPropertiesHandler.WorldGenProperties arg) {
        final GeneratorOptions generatorOptions = GeneratorOptions.fromProperties(registryManager, new ServerPropertiesHandler.WorldGenProperties(
                "c2metest",
                new JsonObject(),
                true,
                "default"
        ));
        System.out.println("GeneratorOptions: " + generatorOptions);
        return generatorOptions;
    }

}
