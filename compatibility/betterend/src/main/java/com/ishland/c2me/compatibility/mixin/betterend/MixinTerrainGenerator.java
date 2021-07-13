package com.ishland.c2me.compatibility.mixin.betterend;

import com.ishland.c2me.compatibility.common.betterend.ThreadLocalIslandLayer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.betterend.world.generator.IslandLayer;
import ru.betterend.world.generator.LayerOptions;
import ru.betterend.world.generator.TerrainBoolCache;
import ru.betterend.world.generator.TerrainGenerator;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(TerrainGenerator.class)
public class MixinTerrainGenerator {

    private static final ThreadLocal<ReentrantLock> LOCK_THREAD_LOCAL = ThreadLocal.withInitial(ReentrantLock::new);
    private static final ThreadLocal<Point> POINT_THREAD_LOCAL = ThreadLocal.withInitial(Point::new);

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lru/betterend/world/generator/TerrainGenerator;LOCKER:Ljava/util/concurrent/locks/ReentrantLock;", opcode = Opcodes.GETSTATIC), remap = false)
    private static ReentrantLock redirectLocker() {
        return LOCK_THREAD_LOCAL.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lru/betterend/world/generator/TerrainGenerator;POS:Ljava/awt/Point;", opcode = Opcodes.GETSTATIC), remap = false)
    private static Point redirectPoint() {
        return POINT_THREAD_LOCAL.get();
    }

    private static final AtomicInteger cleanCount = new AtomicInteger();
    private static final ThreadLocal<AtomicInteger> cleanCountThreadLocal = ThreadLocal.withInitial(AtomicInteger::new);
    private static final ThreadLocal<Map<Point, TerrainBoolCache>> TERRAIN_BOOL_CACHE_MAP_THREAD_LOCAL = ThreadLocal.withInitial(Object2ObjectOpenHashMap::new);

    @Inject(method = "initNoise", at = @At("RETURN"), remap = false)
    private static void onInitNoise(CallbackInfo ci) {
        cleanCount.incrementAndGet();
    }

    @Redirect(method = "isLand", at = @At(value = "FIELD", target = "Lru/betterend/world/generator/TerrainGenerator;TERRAIN_BOOL_CACHE_MAP:Ljava/util/Map;", opcode = Opcodes.GETSTATIC), remap = false)
    private static Map<Point, TerrainBoolCache> redirectTerrainBoolCacheMap() {
        if (cleanCount.get() != cleanCountThreadLocal.get().get()) {
            TERRAIN_BOOL_CACHE_MAP_THREAD_LOCAL.get().clear();
            cleanCountThreadLocal.get().set(cleanCount.get());
        }
        return TERRAIN_BOOL_CACHE_MAP_THREAD_LOCAL.get();
    }

    @Redirect(method = "initNoise", at = @At(value = "NEW", target = "ru/betterend/world/generator/IslandLayer"), remap = false)
    private static IslandLayer redirectIslandLayerNew(int seed, LayerOptions options) {
        return new ThreadLocalIslandLayer(seed, options);
    }

}
