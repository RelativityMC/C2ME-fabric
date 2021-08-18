package com.ishland.c2me.tests.testmod.mixin.fix;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = {
        "supercoder79.ecotones.Ecotones",
        "supercoder79.ecotones.util.RegistryReport",
        "supercoder79.ecotones.util.AiLog"
})
public class MixinFixDevLaunch {

    @Dynamic
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/api/FabricLoader;isDevelopmentEnvironment()Z"), require = 0)
    private boolean redirectDevEnvironment(FabricLoader unused) {
        return false;
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/api/FabricLoader;isDevelopmentEnvironment()Z"), require = 0)
    private static boolean redirectDevEnvironmentStatic(FabricLoader unused) {
        return false;
    }

}
