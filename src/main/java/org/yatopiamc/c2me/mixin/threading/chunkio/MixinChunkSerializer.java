package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.yatopiamc.c2me.common.threading.chunkio.ChunkIoMainThreadTaskUtils;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedChunkTickScheduler;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedLightingProvider;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedServerTickScheduler;

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

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ChunkTickScheduler;toNbt()Lnet/minecraft/nbt/ListTag;"))
    private static ListTag onChunkTickSchedulerToNbt(@SuppressWarnings("rawtypes") ChunkTickScheduler chunkTickScheduler) {
        if (chunkTickScheduler instanceof ICachedChunkTickScheduler) {
            return ((ICachedChunkTickScheduler) chunkTickScheduler).getCachedNbt();
        } else {
            new IllegalStateException("Unable to take cached ticklist. Falling back to uncached query. This will affect data integrity. Incompatible mods?").printStackTrace();
            return chunkTickScheduler.toNbt();
        }
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/SimpleTickScheduler;toNbt()Lnet/minecraft/nbt/ListTag;"))
    private static ListTag onSimpleTickSchedulerToNbt(@SuppressWarnings("rawtypes") SimpleTickScheduler simpleTickScheduler) {
        if (simpleTickScheduler instanceof ICachedChunkTickScheduler) {
            return ((ICachedChunkTickScheduler) simpleTickScheduler).getCachedNbt();
        } else {
            new IllegalStateException("Unable to take cached ticklist. Falling back to uncached query. This will affect data integrity. Incompatible mods?").printStackTrace();
            return simpleTickScheduler.toNbt();
        }
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;toNbt(Lnet/minecraft/util/math/ChunkPos;)Lnet/minecraft/nbt/ListTag;"))
    private static ListTag onServerTickSchedulerToTag(@SuppressWarnings("rawtypes") ServerTickScheduler serverTickScheduler, ChunkPos chunkPos) {
        if (serverTickScheduler instanceof ICachedServerTickScheduler) {
            return ((ICachedServerTickScheduler) serverTickScheduler).getCachedNbt(chunkPos);
        } else {
            new IllegalStateException("Unable to take cached ticklist. Falling back to uncached query. This will affect data integrity. Incompatible mods?").printStackTrace();
            return serverTickScheduler.toNbt(chunkPos);
        }
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/light/ChunkLightingView;getLightSection(Lnet/minecraft/util/math/ChunkSectionPos;)Lnet/minecraft/world/chunk/ChunkNibbleArray;"))
    private static ChunkNibbleArray onGetLightSection(ChunkLightingView chunkLightingView, ChunkSectionPos pos) {
        return null;
    }

    @Inject(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putByte(Ljava/lang/String;B)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void afterChunkSectionInit(ServerWorld world, Chunk chunk, CallbackInfoReturnable<CompoundTag> cir, ChunkPos chunkPos, CompoundTag compoundTag, CompoundTag compoundTag2, ChunkSection[] chunkSections, ListTag listTag, LightingProvider lightingProvider, boolean bl, int i, int j, ChunkSection chunkSection, ChunkNibbleArray chunkNibbleArray, ChunkNibbleArray chunkNibbleArray2, CompoundTag compoundTag3) {
        if (lightingProvider instanceof ICachedLightingProvider) {
            final ICachedLightingProvider.LightData lightData = ((ICachedLightingProvider) lightingProvider).takeLightData(ChunkSectionPos.from(chunkPos, i));
            if (lightData == null) {
                System.err.println("Tried to serialize lighting with no cached light data! This will affect performance. Incompatible mods?");
                return;
            }
            if (lightData.blockLight != null)
                compoundTag3.putByteArray("BlockLight", lightData.blockLight);
            if (lightData.skyLight != null)
                compoundTag3.putByteArray("SkyLight", lightData.skyLight);
        }
    }

}
