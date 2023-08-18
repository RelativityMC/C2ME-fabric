package com.ishland.c2me.threading.lighting.mixin.starlight.access;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import ca.spottedleaf.starlight.common.light.SkyStarLightEngine;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StarLightInterface.class)
public interface IStarLightInterface {

    @Accessor(remap = false)
    int getMinSection();

    @Accessor(remap = false)
    int getMaxSection();

    @Invoker(remap = false)
    SkyStarLightEngine invokeGetSkyLightEngine();

    @Invoker(remap = false)
    void invokeReleaseSkyLightEngine(SkyStarLightEngine engine);

    @Invoker(remap = false)
    BlockStarLightEngine invokeGetBlockLightEngine();

    @Invoker(remap = false)
    void invokeReleaseBlockLightEngine(BlockStarLightEngine engine);
}
