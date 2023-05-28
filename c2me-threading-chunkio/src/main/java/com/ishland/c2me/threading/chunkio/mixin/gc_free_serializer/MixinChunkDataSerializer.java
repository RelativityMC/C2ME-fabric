package com.ishland.c2me.threading.chunkio.mixin.gc_free_serializer;

import com.ishland.c2me.threading.chunkio.common.AsyncSerializationManager;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Pseudo
@Mixin(targets = "com.ishland.c2me.rewrites.chunk_serializer.common.ChunkDataSerializer")
public class MixinChunkDataSerializer {

    @Redirect(method = "Lcom/ishland/c2me/rewrites/chunk_serializer/common/ChunkDataSerializer;write(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lcom/ishland/c2me/rewrites/chunk_serializer/common/NbtWriter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getBlockEntityPositions()Ljava/util/Set;"))
    private static Set<BlockPos> onChunkGetBlockEntityPositions(Chunk chunk) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        return scope != null ? scope.blockEntityPositions : chunk.getBlockEntityPositions();
    }

    @Redirect(method = "Lcom/ishland/c2me/rewrites/chunk_serializer/common/ChunkDataSerializer;write(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lcom/ishland/c2me/rewrites/chunk_serializer/common/NbtWriter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getPackedBlockEntityNbt(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/nbt/NbtCompound;"))
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
//            if (nbtCompound == null) LOGGER.warn("Block Entity at {} for block {} doesn't exist", pos, chunk.getBlockState(pos).getBlock());
            return nbtCompound;
        }
    }

    @Redirect(method = "Lcom/ishland/c2me/rewrites/chunk_serializer/common/ChunkDataSerializer;writeSectionDataVanilla(Lcom/ishland/c2me/rewrites/chunk_serializer/common/NbtWriter;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/ChunkPos;[Lcom/ishland/c2me/base/mixin/access/IChunkSection;Lnet/minecraft/world/chunk/light/LightingProvider;Lnet/minecraft/registry/Registry;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/light/LightingProvider;get(Lnet/minecraft/world/LightType;)Lnet/minecraft/world/chunk/light/ChunkLightingView;"))
    private static ChunkLightingView onLightingProviderGet(LightingProvider lightingProvider, LightType lightType) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(null);
        return scope != null ? scope.lighting.get(lightType) : lightingProvider.get(lightType);
    }

}
