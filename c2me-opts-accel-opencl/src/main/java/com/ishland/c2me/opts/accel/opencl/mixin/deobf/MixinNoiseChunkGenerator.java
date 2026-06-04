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

package com.ishland.c2me.opts.accel.opencl.mixin.deobf;

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseChunkGenerator.class)
public abstract class MixinNoiseChunkGenerator {

    @Shadow protected abstract ChunkNoiseSampler createChunkNoiseSampler(Chunk chunk, StructureAccessor world, Blender blender, NoiseConfig noiseConfig);

    @Shadow @Final private RegistryEntry<ChunkGeneratorSettings> settings;

    @Shadow protected abstract BlockState getBlockState(ChunkNoiseSampler chunkNoiseSampler, int x, int y, int z, BlockState state);

    @Shadow @Final private static BlockState AIR;

    /**
     * @author ishland
     * @reason deobf
     */
    @Overwrite
    private Chunk populateNoise(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {
        ChunkNoiseSampler chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(
                chunkx -> this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig)
        );
        Heightmap oceanFloorHeightmap = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap worldSurfaceHeightmap = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk.getPos();
        int chunkStartX = chunkPos.getStartX();
        int chunkStartZ = chunkPos.getStartZ();
        AquiferSampler aquifer = chunkNoiseSampler.getAquiferSampler();
        chunkNoiseSampler.sampleStartDensity();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int horizontalCellBlockCount = ((IChunkNoiseSampler) chunkNoiseSampler).getHorizontalCellBlockCount();
        int verticalCellBlockCount = ((IChunkNoiseSampler) chunkNoiseSampler).getVerticalCellBlockCount();
        int m = 16 / horizontalCellBlockCount;
        int n = 16 / horizontalCellBlockCount;

        for (int cellX = 0; cellX < m; cellX++) {
            chunkNoiseSampler.sampleEndDensity(cellX);

            for (int cellZ = 0; cellZ < n; cellZ++) {
                int curSectionIndex = chunk.countVerticalSections() - 1;
                ChunkSection chunkSection = chunk.getSection(curSectionIndex);

                for (int cellY = cellHeight - 1; cellY >= 0; cellY--) {
                    chunkNoiseSampler.onSampledCellCorners(cellY, cellZ);

                    for (int verticalCellBlock = verticalCellBlockCount - 1; verticalCellBlock >= 0; verticalCellBlock--) {
                        int blockY = (minimumCellY + cellY) * verticalCellBlockCount + verticalCellBlock;
                        int blockYInSection = blockY & 15;
                        int v = chunk.getSectionIndex(blockY);

                        if (curSectionIndex != v) {
                            curSectionIndex = v;
                            chunkSection = chunk.getSection(v);
                        }

                        double verticalCellProgress = (double) verticalCellBlock / (double) verticalCellBlockCount;
                        for (int cellBlockX = 0; cellBlockX < horizontalCellBlockCount; cellBlockX++) {
                            int blockX = chunkStartX + cellX * horizontalCellBlockCount + cellBlockX;
                            int blockXInSection = blockX & 15;
                            double cellXProgress = (double) cellBlockX / (double) horizontalCellBlockCount;
                            for (int cellBlockZ = 0; cellBlockZ < horizontalCellBlockCount; cellBlockZ++) {
                                int blockZ = chunkStartZ + cellZ * horizontalCellBlockCount + cellBlockZ;
                                int blockZInSection = blockZ & 15;
                                double cellZProgress = (double) cellBlockZ / (double) horizontalCellBlockCount;

                                // all moved here for clarity
                                chunkNoiseSampler.interpolateY(blockY, verticalCellProgress);
                                chunkNoiseSampler.interpolateX(blockX, cellXProgress);
                                chunkNoiseSampler.interpolateZ(blockZ, cellZProgress);

                                BlockState blockState = ((IChunkNoiseSampler) chunkNoiseSampler).invokeSampleBlockState();
                                if (blockState == null) {
                                    blockState = this.settings.value().defaultBlock();
                                }
                                blockState = this.getBlockState(chunkNoiseSampler, blockX, blockY, blockZ, blockState); // always null
                                if (blockState != AIR && !SharedConstants.isOutsideGenerationArea(chunk.getPos())) {
                                    chunkSection.setBlockState(blockXInSection, blockYInSection, blockZInSection, blockState, false);
                                    oceanFloorHeightmap.trackUpdate(blockXInSection, blockY, blockZInSection, blockState);
                                    worldSurfaceHeightmap.trackUpdate(blockXInSection, blockY, blockZInSection, blockState);
                                    if (aquifer.needsFluidTick() && !blockState.getFluidState().isEmpty()) {
                                        mutable.set(blockX, blockY, blockZ);
                                        chunk.markBlockForPostProcessing(mutable);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            chunkNoiseSampler.swapBuffers();
        }

        chunkNoiseSampler.stopInterpolation();
        return chunk;
    }

}
