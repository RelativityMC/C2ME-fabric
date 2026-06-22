package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.base.common.util.InvokingExecutorService;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collector;
import java.util.stream.Stream;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator {

    @Unique
    private final ConcurrentHashMap<Registry<Structure>, Object> c2me$structuresByStepCache = new ConcurrentHashMap<>();

    @Redirect(method = "populateBiomes", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMainWorkerExecutor()Ljava/util/concurrent/ExecutorService;"))
    private ExecutorService redirectBiomeExecutor() {
        return InvokingExecutorService.INSTANCE;
    }

    // vanilla re-groups the entire structure registry by generation step for every generated
    // chunk; the result only depends on the registry, so compute it once and reuse it.
    // the cached map is read-only after construction and safe for concurrent readers.
    // require = 0: if another mod replaces generateFeatures, this cache silently backs off.
    @WrapOperation(
            method = "generateFeatures",
            at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;collect(Ljava/util/stream/Collector;)Ljava/lang/Object;"),
            require = 0
    )
    private Object cacheStructuresByStep(Stream<?> instance, Collector<?, ?, ?> collector, Operation<Object> original, @Local Registry<Structure> registry) {
        Object cached = this.c2me$structuresByStepCache.get(registry);
        if (cached == null) {
            cached = original.call(instance, collector);
            this.c2me$structuresByStepCache.putIfAbsent(registry, cached);
        }
        return cached;
    }
}
