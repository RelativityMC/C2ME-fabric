package com.ishland.c2me.tests.testmod.mixin.pregen;

import com.google.gson.JsonObject;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPropertiesHandler.class)
public class MixinServerPropertiesHandler {

    @Shadow @Final private ServerPropertiesHandler.WorldGenProperties worldGenProperties;

    @Redirect(method = "getGeneratorOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/ServerPropertiesHandler$WorldGenProperties;createGeneratorOptions(Lnet/minecraft/util/registry/DynamicRegistryManager;)Lnet/minecraft/world/gen/GeneratorOptions;"))
    private GeneratorOptions redirectGeneratorOptions(ServerPropertiesHandler.WorldGenProperties instance, DynamicRegistryManager dynamicRegistryManager) {
        final GeneratorOptions generatorOptions = new ServerPropertiesHandler.WorldGenProperties("c2metest", new JsonObject(), false, WorldPresets.DEFAULT.getValue().toString()).createGeneratorOptions(dynamicRegistryManager);
        System.out.println("GeneratorOptions: " + generatorOptions);
        return generatorOptions;
    }

}
