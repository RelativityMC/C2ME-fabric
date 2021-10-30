package com.ishland.c2me.tests.testmod.mixin.fix.client;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Set;

@Mixin(EntityModelLayers.class)
public class MixinEntityModelLayers {

    @Shadow @Final private static Set<EntityModelLayer> LAYERS;
    @SuppressWarnings("unused")
    private static Set<EntityModelLayer> ALL_MODELS = Collections.emptySet();

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo ci) {
        ALL_MODELS = LAYERS;
    }

}
