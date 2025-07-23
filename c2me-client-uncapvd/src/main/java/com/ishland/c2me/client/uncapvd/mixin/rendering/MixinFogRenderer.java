package com.ishland.c2me.client.uncapvd.mixin.rendering;

import net.minecraft.client.render.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    @ModifyArg(method = "applyFog(Lnet/minecraft/client/render/Camera;IZLnet/minecraft/client/render/RenderTickCounter;FLnet/minecraft/client/world/ClientWorld;)Lorg/joml/Vector4f;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/fog/FogRenderer;getFogColor(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IFZ)Lorg/joml/Vector4f;"), index = 3)
    private int overrideViewDistance(int viewDistance) {
        return Math.clamp(viewDistance, 2, 32);
    }

}
