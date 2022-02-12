package com.ishland.c2me.mixin;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.fixes.DataFixerUpperClasspathFix;
import com.ishland.c2me.common.util.ModuleUtil;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class C2MEMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("C2ME Mixin");

    private static final boolean I_REALLY_NEED_VECTORIZATION = Boolean.getBoolean("com.ishland.c2me.mixin.IReallyNeedVectorizationAndIKnowWhatIAmDoing");

    @Override
    public void onLoad(String mixinPackage) {
        //noinspection ResultOfMethodCallIgnored
        C2MEConfig.threadedWorldGenConfig.getClass().getName(); // Load configuration
        LOGGER.info("Successfully loaded configuration for C2ME");
        DataFixerUpperClasspathFix.fix();
        if (I_REALLY_NEED_VECTORIZATION && ModuleUtil.isModuleLoaded) {
            LOGGER.info("Successfully loaded submodule for additional acceleration");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.threading.worldgen."))
            return C2MEConfig.threadedWorldGenConfig.enabled;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.threading.chunkio."))
            return C2MEConfig.ioSystemConfig.async;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache."))
            return C2MEConfig.threadedWorldGenConfig.enabled && C2MEConfig.threadedWorldGenConfig.useGlobalBiomeCache;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.worldgen.thread_local_biome_cache."))
            return !(C2MEConfig.threadedWorldGenConfig.enabled && C2MEConfig.threadedWorldGenConfig.useGlobalBiomeCache);
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.worldgen.vanilla_optimization.the_end_biome_cache."))
            return C2MEConfig.vanillaWorldGenOptimizationsConfig.useEndBiomeCache;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.chunkaccess.async_chunk_request."))
            return C2MEConfig.generalOptimizationsConfig.optimizeAsyncChunkRequest;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.notickvd."))
            return C2MEConfig.noTickViewDistanceConfig.enabled;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.chunkio.compression.modify_default_chunk_compression."))
            return C2MEConfig.generalOptimizationsConfig.chunkStreamVersion != -1;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.vectorizations."))
            return I_REALLY_NEED_VECTORIZATION && ModuleUtil.isModuleLoaded;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.threading.async_scheduling."))
            return C2MEConfig.asyncSchedulingConfig.enabled;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.threading.lighting."))
            return !FabricLoader.getInstance().isModLoaded("lightbench");
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.chunkscheduling.mid_tick_chunk_tasks."))
            return C2MEConfig.generalOptimizationsConfig.doMidTickChunkTasks;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.chunkio."))
            return C2MEConfig.ioSystemConfig.replaceImpl;
        if (mixinClassName.equals("com.ishland.c2me.mixin.optimization.reduce_allocs.MixinNbtCompound") ||
                mixinClassName.equals("com.ishland.c2me.mixin.optimization.reduce_allocs.MixinNbtCompound1"))
            return !FabricLoader.getInstance().isModLoaded("lithium");
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.chunkscheduling.idle_tasks.autosave.disable_vanilla_mid_tick_autosave."))
            return C2MEConfig.generalOptimizationsConfig.autoSaveConfig.mode != C2MEConfig.GeneralOptimizationsConfig.AutoSaveConfig.Mode.VANILLA;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.chunkscheduling.idle_tasks.autosave.enhanced_autosave."))
            return C2MEConfig.generalOptimizationsConfig.autoSaveConfig.mode == C2MEConfig.GeneralOptimizationsConfig.AutoSaveConfig.Mode.ENHANCED;
        if (mixinClassName.startsWith("com.ishland.c2me.mixin.optimization.worldgen.vanilla_optimization.aquifer."))
            return C2MEConfig.vanillaWorldGenOptimizationsConfig.optimizeAquifer;
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
