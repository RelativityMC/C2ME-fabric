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

package com.ishland.c2me.opts.accel.vulkan.mixin;

import com.google.common.base.Stopwatch;
import com.ishland.c2me.opts.accel.vulkan.common.Config;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.GeneratedSpirVSource;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.SpirVGen;
import com.ishland.c2me.opts.accel.vulkan.common.compiler.emitters.misc.CLBlockStateMappings;
import com.ishland.c2me.opts.accel.vulkan.common.ducks.MinecraftServerExtension;
import com.ishland.c2me.opts.accel.vulkan.common.ducks.TACSExtension;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VkServerGlobalContext;
import com.ishland.c2me.opts.accel.vulkan.common.gen.VkServerWorldContext;
import com.ishland.c2me.opts.dfc.common.ast.opto.OptoPasses;
import com.ishland.c2me.opts.dfc.common.ducks.NoiseRouterExtension;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.noise.NoiseRouter;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkLoadingManager.class)
public class MixinThreadedAnvilChunkStorage implements TACSExtension {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private ServerWorld world;
    @Shadow
    @Final
    private NoiseConfig noiseConfig;
    @Shadow
    @Final
    private ChunkGenerationContext generationContext;
    @Unique
    private VkServerWorldContext c2me$clContext;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        ChunkGenerator generator = this.generationContext.generator();
        ChunkGeneratorSettings settings;
        if (generator instanceof NoiseChunkGenerator noiseChunkGenerator) {
            settings = noiseChunkGenerator.getSettings().value();
        } else {
            settings = ChunkGeneratorSettings.createMissingSettings();
        }
        GenerationShapeConfig trimmed = settings.generationShapeConfig().trimHeight(this.world);
        Reference2ReferenceMap<DensityFunction, OptoPasses.AstPair> optoCache = new Reference2ReferenceOpenHashMap<>();
        NoiseRouter originalNoiseRouter = ((NoiseRouterExtension) (Object) this.noiseConfig.getNoiseRouter()).c2me$getOriginalNoiseRouter();

        Stopwatch compilationStopwatch = Stopwatch.createStarted();
        GeneratedSpirVSource generatedSpirVSource;
        try {
            generatedSpirVSource = SpirVGen.compile(
                    originalNoiseRouter,
                    trimmed,
                    optoCache,
                    ((NoiseRouterExtension) (Object) originalNoiseRouter).c2me$getFinalFinalDensity(),
                    generator.getBiomeSource()
            );
        } catch (Throwable t) {
            LOGGER.error("Vulkan codegen for world {} failed", this.world.getRegistryKey().getValue(), t);
            if (!Config.allowIncompatibilityFallback) {
                throw new RuntimeException("Vulkan codegen failed", t);
            }
            generatedSpirVSource = null;
        }

        compilationStopwatch.stop();
        LOGGER.info("Vulkan codegen for world {} finished in {}", this.world.getRegistryKey().getValue(), compilationStopwatch);

        VkServerGlobalContext globalContext = ((MinecraftServerExtension) this.world.getServer()).c2me$getCLContext();
        if (globalContext == null) {
            LOGGER.warn("World {} cannot use Vulkan since the global context is not initialized", this.world.getRegistryKey().getValue());
            if (!Config.allowIncompatibilityFallback) {
                throw new IllegalStateException("Vulkan global context is not initialized");
            }
            return;
        }
        if (generatedSpirVSource == null) {
            LOGGER.warn("World {} does not have compiled CL code. Is it incompatible with CL?", this.world.getRegistryKey().getValue());
            if (!Config.allowIncompatibilityFallback) {
                throw new IllegalStateException("Vulkan codegen failed");
            }
            return;
        }
        LOGGER.info("Source size: {} bytes, const_data size: {} bytes", generatedSpirVSource.getGeneratedSizeBytes(), generatedSpirVSource.getConstData().length);
        this.c2me$clContext = new VkServerWorldContext(globalContext, this.world.getRegistryKey().getValue().toString(), generatedSpirVSource, trimmed, CLBlockStateMappings.defaultMappings(settings.defaultBlock(), settings.defaultFluid()));
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void postClose(CallbackInfo ci) {
        if (this.c2me$clContext != null) {
            this.c2me$clContext.releaseAllDevices();
            this.c2me$clContext = null;
        }
    }

    @Override
    public VkServerWorldContext c2me$getCLContext() {
        return this.c2me$clContext;
    }

}
