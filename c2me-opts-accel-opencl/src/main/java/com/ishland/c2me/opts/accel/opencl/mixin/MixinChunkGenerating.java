/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl.mixin;

import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.opts.accel.opencl.common.ducks.TACSExtension;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLServerWorldContext;
import com.ishland.c2me.opts.accel.opencl.common.util.TLUtil;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerating;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGenerating.class)
public class MixinChunkGenerating {

    @WrapMethod(method = "generateStructures")
    private static CompletableFuture<Chunk> wrapStructureStarts(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk, Operation<CompletableFuture<Chunk>> original) {
        CLServerWorldContext clContext = ((TACSExtension) context.world().getChunkManager().chunkLoadingManager).c2me$getCLContext();
        return clContext.getEstimateSurfaceHeightCache().getChunkCache(chunk.getPos().x(), chunk.getPos().z()).thenComposeAsync(cacheEntry -> {
            if (TLUtil.stage1CachePassing.isBound()) {
                throw new IllegalStateException("Reentrance");
            }
            return ScopedValue.where(TLUtil.stage1CachePassing, cacheEntry).call(() -> original.call(context, step, chunks, chunk));
        }, ((IVanillaChunkManager) context.world().getChunkManager().chunkLoadingManager).c2me$getSchedulingManager().positionedExecutor(chunk.getPos().toLong()));
    }

//    @WrapOperation(method = "populateNoise", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;"))
//    private static CompletableFuture<Chunk> wrapPopulateNoise(ChunkGenerator instance, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk, Operation<CompletableFuture<Chunk>> original, ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk1) {
//        if (TLUtil.worldContextPassing.get() != null) {
//            throw new IllegalStateException("Reentrance");
//        }
//        TLUtil.worldContextPassing.set(((TACSExtension) context.world().getChunkManager().chunkLoadingManager).c2me$getCLContext());
//        try {
//            return original.call(instance, blender, noiseConfig, structureAccessor, chunk);
//        } finally {
//            TLUtil.worldContextPassing.remove();
//        }
//    }
//
//    @WrapOperation(method = "populateBiomes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;populateBiomes(Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)Ljava/util/concurrent/CompletableFuture;"))
//    private static CompletableFuture<Chunk> wrapPopulateBiomes(ChunkGenerator instance, NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk, Operation<CompletableFuture<Chunk>> original, ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk1) {
//        if (TLUtil.worldContextPassing.get() != null) {
//            throw new IllegalStateException("Reentrance");
//        }
//        TLUtil.worldContextPassing.set(((TACSExtension) context.world().getChunkManager().chunkLoadingManager).c2me$getCLContext());
//        try {
//            return original.call(instance, noiseConfig, blender, structureAccessor, chunk);
//        } finally {
//            TLUtil.worldContextPassing.remove();
//        }
//    }

}
