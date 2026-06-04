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
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.compat.internal_api.NewStatusHook;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(NewStatusHook.class)
public class MixinNewStatusHook {

    @Dynamic
    @Inject(method = "beforeVanillaStatusRegister", at = @At("RETURN"), remap = false)
    private static void onAdd(ArrayList<NewChunkStatus> pending, ChunkStatus nextStatus, CallbackInfo ci) {
        if (nextStatus == ChunkStatus.BIOMES) {
            BatchingBiomeNoiseStatus status = new BatchingBiomeNoiseStatus(pending.size());
            BatchingBiomeNoiseStatus.setInstance(status);
            pending.add(status);
        }
    }

}
