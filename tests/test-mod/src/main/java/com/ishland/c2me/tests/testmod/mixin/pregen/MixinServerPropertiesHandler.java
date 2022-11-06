package com.ishland.c2me.tests.testmod.mixin.pregen;

import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.world.gen.GeneratorOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPropertiesHandler.class)
public class MixinServerPropertiesHandler {

    @Mutable
    @Shadow @Final public GeneratorOptions generatorOptions;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.generatorOptions = new GeneratorOptions(GeneratorOptions.parseSeed("c2metest"), true, false);
    }

}
