package com.ishland.c2me.rewrites.chunksystem.mixin.async_serialization.gc_free_serializer;

import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.AsyncSerializationManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
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
        return scope != null ? scope.blockEntities.keySet() : chunk.getBlockEntityPositions();
    }

    @Redirect(method = "Lcom/ishland/c2me/rewrites/chunk_serializer/common/ChunkDataSerializer;write(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lcom/ishland/c2me/rewrites/chunk_serializer/common/NbtWriter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getPackedBlockEntityNbt(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/nbt/NbtCompound;"))
    private static NbtCompound onChunkGetPackedBlockEntityNbt(Chunk chunk, BlockPos pos, RegistryWrapper.WrapperLookup wrapperLookup) {
        final AsyncSerializationManager.Scope scope = AsyncSerializationManager.getScope(chunk.getPos());
        if (scope == null) return chunk.getPackedBlockEntityNbt(pos, wrapperLookup);
        return scope.blockEntities.get(pos);
    }

}
