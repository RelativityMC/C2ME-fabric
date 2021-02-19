package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.c2me.common.threading.chunkio.ChunkIoMainThreadTaskUtils;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {

    @Redirect(method = "deserialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/light/LightingProvider;setRetainData(Lnet/minecraft/util/math/ChunkPos;Z)V"))
    private static void onLightingProviderSetRetainData(LightingProvider lightingProvider, ChunkPos pos, boolean retainData) {
        ChunkIoMainThreadTaskUtils.executeMain(() -> lightingProvider.setRetainData(pos, retainData));
    }

    @Redirect(method = "deserialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/light/LightingProvider;enqueueSectionData(Lnet/minecraft/world/LightType;Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/world/chunk/ChunkNibbleArray;Z)V"))
    private static void onLightingProviderEnqueueSectionData(LightingProvider lightingProvider, LightType lightType, ChunkSectionPos pos, ChunkNibbleArray nibbles, boolean bl) {
        ChunkIoMainThreadTaskUtils.executeMain(() -> lightingProvider.enqueueSectionData(lightType, pos, nibbles, bl));
    }

    @Redirect(method = "deserialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/poi/PointOfInterestStorage;initForPalette(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/ChunkSection;)V"))
    private static void onPoiStorageInitForPalette(PointOfInterestStorage pointOfInterestStorage, ChunkPos chunkPos, ChunkSection chunkSection) {
        ChunkIoMainThreadTaskUtils.executeMain(() -> pointOfInterestStorage.initForPalette(chunkPos, chunkSection));
    }

}
