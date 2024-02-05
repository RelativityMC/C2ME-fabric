package com.ishland.c2me.threading.chunkio.mixin;

import com.ishland.c2me.threading.chunkio.common.AsyncSerializationManager;
import com.ishland.c2me.threading.chunkio.common.ChunkIoMainThreadTaskUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {

    @Shadow @Final private static Logger LOGGER;

    @Redirect(method = "deserialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/poi/PointOfInterestStorage;initForPalette(Lnet/minecraft/util/math/ChunkSectionPos;Lnet/minecraft/world/chunk/ChunkSection;)V"))
    private static void onPoiStorageInitForPalette(PointOfInterestStorage instance, ChunkSectionPos chunkSectionPos, ChunkSection chunkSection) {
        ChunkIoMainThreadTaskUtils.executeMain(() -> instance.initForPalette(chunkSectionPos, chunkSection));
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getBlockEntityPositions()Ljava/util/Set;"))
    private static Set<BlockPos> onChunkGetBlockEntityPositions(Chunk chunk) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        return scope != null ? scope.blockEntityPositions : chunk.getBlockEntityPositions();
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getPackedBlockEntityNbt(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/nbt/NbtCompound;"))
    private static NbtCompound onChunkGetPackedBlockEntityNbt(Chunk chunk, BlockPos pos) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        if (scope == null) return chunk.getPackedBlockEntityNbt(pos);
        final BlockEntity blockEntity = scope.blockEntities.get(pos);
        if (blockEntity != null) {
            final NbtCompound nbtCompound = blockEntity.createNbtWithIdentifyingData();
            if (chunk instanceof WorldChunk) nbtCompound.putBoolean("keepPacked", false);
            return nbtCompound;
        } else {
            final NbtCompound nbtCompound = scope.pendingBlockEntityNbtsPacked.get(pos);
            if (nbtCompound != null && chunk instanceof WorldChunk) nbtCompound.putBoolean("keepPacked", true);
            if (nbtCompound == null && AsyncSerializationManager.DEBUG) LOGGER.warn("Block Entity at {} for block {} doesn't exist", pos, chunk.getBlockState(pos).getBlock());
            return nbtCompound;
        }
    }

    @Redirect(method = "serialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/light/LightingProvider;get(Lnet/minecraft/world/LightType;)Lnet/minecraft/world/chunk/light/ChunkLightingView;"))
    private static ChunkLightingView onLightingProviderGet(LightingProvider lightingProvider, LightType lightType) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(null);
        return scope != null ? scope.lighting.get(lightType) : lightingProvider.get(lightType);
    }

}
