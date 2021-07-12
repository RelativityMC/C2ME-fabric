package com.ishland.c2me.compatibility.mixin.terra;

import com.dfsek.terra.api.math.noise.NoiseSampler;
import com.dfsek.terra.api.math.paralithic.noise.NoiseFunction2;
import com.dfsek.terra.carving.CarverCache;
import com.dfsek.terra.carving.UserDefinedCarver;
import com.ishland.c2me.compatibility.common.terra.ThreadLocalNoiseFunction2;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(UserDefinedCarver.class)
public class MixinUserDefinedCarver {

    @Dynamic
    @Redirect(method = "lambda$new$0", at = @At(value = "NEW", target = "com/dfsek/terra/api/math/paralithic/noise/NoiseFunction2"), remap = false)
    private static NoiseFunction2 redirectNoiseFunction2(NoiseSampler gen) {
        return new ThreadLocalNoiseFunction2(gen);
    }

    private final ThreadLocal<Map<Long, CarverCache>> cacheMapThreadLocal = ThreadLocal.withInitial(Long2ObjectOpenHashMap::new);

    @Redirect(method = "carve", at = @At(value = "FIELD", target = "Lcom/dfsek/terra/carving/UserDefinedCarver;cacheMap:Ljava/util/Map;"), remap = false)
    private Map<Long, CarverCache> redirectCacheMap(UserDefinedCarver unused) {
        return cacheMapThreadLocal.get();
    }

}
