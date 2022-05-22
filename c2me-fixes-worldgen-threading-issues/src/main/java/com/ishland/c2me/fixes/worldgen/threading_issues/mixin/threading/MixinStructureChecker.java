package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.StructureLocator;
import net.minecraft.world.gen.structure.StructureType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(StructureLocator.class)
public class MixinStructureChecker {

    @Mutable
    @Shadow @Final private Long2ObjectMap<Object2IntMap<StructureType>> cachedFeaturesByChunkPos;

    @Mutable
    @Shadow @Final private Map<StructureType, Long2BooleanMap> generationPossibilityByFeature;

    @Unique
    private Object mapMutex = new Object();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo info) {
        this.mapMutex = new Object();
        this.cachedFeaturesByChunkPos = Long2ObjectMaps.synchronize(this.cachedFeaturesByChunkPos);
        this.generationPossibilityByFeature = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>(), this.mapMutex);
    }

    @Redirect(method = "cache(JLit/unimi/dsi/fastutil/objects/Object2IntMap;)V", at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V"))
    private void redirectForEach(Collection<Long2BooleanMap> instance, Consumer<Long2BooleanMap> consumer) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.mapMutex) {
            final Iterator<Long2BooleanMap> iterator = instance.iterator();
            while (iterator.hasNext()) {
                final Long2BooleanMap next = iterator.next();
                consumer.accept(next);
                if (next.isEmpty()) iterator.remove();
            }
        }
    }

}
