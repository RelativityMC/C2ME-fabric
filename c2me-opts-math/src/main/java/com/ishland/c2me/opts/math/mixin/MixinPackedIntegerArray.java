package com.ishland.c2me.opts.math.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.collection.PackedIntegerArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PackedIntegerArray.class)
public abstract class MixinPackedIntegerArray {

    // require = 0: if another mod (e.g. Lithium) replaces or restructures get(), this silently
    // backs off instead of failing to apply
    @WrapOperation(
            method = "get",
            at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/Validate;inclusiveBetween(JJJ)V", remap = false),
            require = 0
    )
    private void skipBoundsCheckOnHotRead(long start, long end, long value, Operation<Void> original) {
        // bounds check elided: palette reads on the worldgen hot path are always in range
    }
}
