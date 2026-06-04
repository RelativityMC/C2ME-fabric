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

import com.ishland.c2me.opts.accel.opencl.common.ducks.ChunkNoiseSamplerExtension;
import com.ishland.c2me.opts.accel.opencl.common.gen.cache.Stage1Cache;
import com.ishland.c2me.opts.accel.opencl.common.util.TLUtil;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkNoiseSampler.class)
public class MixinChunkNoiseSampler implements ChunkNoiseSamplerExtension {

    @Shadow @Final private Long2IntMap surfaceHeightEstimateCache;
    @Unique
    private AquiferSampler.FluidLevelSampler c2me$fluidLevelSampler;
    @Unique
    private ChunkGeneratorSettings c2me$chunkGeneratorSettings;
    @Unique
    private NoiseConfig c2me$noiseConfig;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/noise/NoiseRouter;preliminarySurfaceLevel()Lnet/minecraft/world/gen/densityfunction/DensityFunction;"))
    private void earlyInit(int horizontalCellCount, NoiseConfig noiseConfig, int startBlockX, int startBlockZ, GenerationShapeConfig generationShapeConfig, DensityFunctionTypes.Beardifying beardifying, ChunkGeneratorSettings chunkGeneratorSettings, AquiferSampler.FluidLevelSampler fluidLevelSampler, Blender blender, CallbackInfo ci) {
        if (!TLUtil.stage1CachePassing.isBound()) {
            return;
        }
        Stage1Cache.AreaCacheEntry areaCacheEntry = TLUtil.stage1CachePassing.get();
        int chunkX = areaCacheEntry.chunkX();
        int chunkZ = areaCacheEntry.chunkZ();
        int sizeX = areaCacheEntry.sizeX() * 4 + 32;
        int sizeZ = areaCacheEntry.sizeZ() * 4 + 32;
        int[] surfaceHeights = areaCacheEntry.surfaceHeights();
        Assertions.assertTrue(sizeX * sizeZ == surfaceHeights.length);
        for (int relX = 0; relX < sizeX; relX++) {
            for (int relZ = 0; relZ < sizeZ; relZ++) {
                int cacheRelX = (((chunkX - 4) << 2) + relX);
                int cacheRelZ = (((chunkZ - 4) << 2) + relZ);
                this.surfaceHeightEstimateCache.put(ColumnPos.pack(BiomeCoords.toBlock(cacheRelX), BiomeCoords.toBlock(cacheRelZ)), surfaceHeights[relX * sizeZ + relZ]);
            }
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(int horizontalCellCount, NoiseConfig noiseConfig, int startBlockX, int startBlockZ, GenerationShapeConfig generationShapeConfig, DensityFunctionTypes.Beardifying beardifying, ChunkGeneratorSettings chunkGeneratorSettings, AquiferSampler.FluidLevelSampler fluidLevelSampler, Blender blender, CallbackInfo ci) {
        this.c2me$fluidLevelSampler = fluidLevelSampler;
        this.c2me$chunkGeneratorSettings = chunkGeneratorSettings;
        this.c2me$noiseConfig = noiseConfig;
    }

    @Override
    public AquiferSampler.FluidLevelSampler c2me$getFluidLevelSampler() {
        return this.c2me$fluidLevelSampler;
    }

    @Override
    public ChunkGeneratorSettings c2me$getChunkGeneratorSettings() {
        return this.c2me$chunkGeneratorSettings;
    }

    @Override
    public NoiseConfig c2me$getNoiseConfig() {
        return this.c2me$noiseConfig;
    }

}
