package com.ishland.c2me.fixes.worldgen.vanilla_bugs.mixin.mc_276863;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerating;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGenerating.class)
public class MixinChunkGenerating {

    @ModifyReturnValue(method = "light", at = @At("RETURN"))
    private static CompletableFuture<Chunk> applyPostprocessingAfterLighting(CompletableFuture<Chunk> original, ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld serverWorld = context.world();
        ChunkRegion chunkRegion = new ChunkRegion(serverWorld, chunks, step, chunk);

        return original.thenApply(chunk1 -> {
            ChunkPos chunkPos = chunk1.getPos();

            ShortList[] postProcessingLists = chunk1.getPostProcessingLists();
            for (int i = 0; i < postProcessingLists.length; i++) {
                if (postProcessingLists[i] != null) {
                    for (ShortListIterator iterator = postProcessingLists[i].iterator(); iterator.hasNext(); ) {
                        short short_ = iterator.nextShort();
                        BlockPos blockPos = ProtoChunk.joinBlockPos(short_, chunk1.sectionIndexToCoord(i), chunkPos);
                        BlockState blockState = chunk1.getBlockState(blockPos);

                        if (blockState.getBlock() == Blocks.BROWN_MUSHROOM || blockState.getBlock() == Blocks.RED_MUSHROOM) {
                            if (!blockState.canPlaceAt(chunkRegion, blockPos)) {
                                chunkRegion.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
                            }
                        }
                    }
                }
            }

            return chunk1;
        });
    }

}
