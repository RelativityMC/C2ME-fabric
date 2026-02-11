package com.ishland.c2me.client.uncapvd.mixin.rendering;

import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    @ModifyArg(method = "applyFog(Lnet/minecraft/client/render/Camera;ILnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lnet/minecraft/client/render/fog/FogData;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;getFogColor(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IFLorg/joml/Vector4f;)V"), index = 3)
    private int overrideViewDistance(int viewDistance) {
        return Math.clamp(viewDistance, 2, 32);
    }

}
