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

package com.ishland.c2me.opts.accel.opencl.mixin.chunksystem_integration;

import com.ishland.c2me.opts.accel.opencl.common.chunksystem_integration.BatchingBiomeNoiseStatus;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLServerBatchedBiomeNoiseContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.statuses.VanillaWorldGenerationDelegate;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Mixin(VanillaWorldGenerationDelegate.class)
public abstract class MixinVanillaWorldGenerationDelegate {

    @Shadow(remap = false) @Final
    private ChunkStatus status;

    @Dynamic
    @ModifyReturnValue(method = "getDependencies", at = @At("RETURN"), remap = false)
    private KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] appendBatchingDependency(KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] original, ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        if (this.status == ChunkStatus.BIOMES) {
            ChunkPos pos = holder.getKey();
            if (!CLServerBatchedBiomeNoiseContext.isAligned(pos.x(), pos.z())) { // aligned pos implicitly depends on itself
                KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] newArray = Arrays.copyOf(original, original.length + 1);
                newArray[original.length] = new KeyStatusPair<>(new ChunkPos(pos.x() & ~CLServerBatchedBiomeNoiseContext.BATCH_MASK, pos.z() & ~CLServerBatchedBiomeNoiseContext.BATCH_MASK), BatchingBiomeNoiseStatus.getInstance());
                return newArray;
            }
        }

        return original;
    }

    @Dynamic
    @ModifyReturnValue(method = "getCurDepLen", at = @At("RETURN"), remap = false)
    private int hackArrayLength(int original, ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        if (this.status == ChunkStatus.BIOMES) {
            ChunkPos pos = holder.getKey();
            if (!CLServerBatchedBiomeNoiseContext.isAligned(pos.x(), pos.z())) { // aligned pos implicitly depends on itself
                return original - 1;
            }
        }

        return original;
    }

}
