package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import net.minecraft.world.chunk.light.LightStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LightStorage.class)
public class MixinLightStorage<M extends ChunkToNibbleArrayMap<M>> {

    @Shadow
    @Final
    protected Long2ObjectMap<ChunkNibbleArray> queuedSections;

    @WrapMethod(method = "updateLight")
    private void wrapUpdateLight(ChunkLightProvider<M, ?> lightProvider, Operation<Void> original) {
        synchronized (this.queuedSections) { // protect the iterator
            original.call(lightProvider);
        }
    }

}
