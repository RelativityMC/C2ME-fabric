package com.ishland.c2me.opts.allocs.mixin.object_pooling_caching;

import com.ishland.c2me.opts.allocs.common.ObjectCachingUtils;
import net.minecraft.world.gen.feature.OreFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.BitSet;

@Mixin(OreFeature.class)
public class MixinOreFeature {

    @Redirect(method = "generateVeinPart", at = @At(value = "NEW", target = "java/util/BitSet"))
    private BitSet redirectNewBitSet(int nbits) {
        return ObjectCachingUtils.getCachedOrNewBitSet(nbits);
    }

}
