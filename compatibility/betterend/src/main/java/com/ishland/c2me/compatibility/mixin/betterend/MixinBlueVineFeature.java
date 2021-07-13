package com.ishland.c2me.compatibility.mixin.betterend;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.betterend.world.features.BlueVineFeature;

@Pseudo
@Mixin(BlueVineFeature.class)
public class MixinBlueVineFeature {

    private final ThreadLocal<Boolean> smallThreadLocal = ThreadLocal.withInitial(() -> false);

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lru/betterend/world/features/BlueVineFeature;small:Z", opcode = Opcodes.PUTFIELD))
    private void redirectSetSmall(BlueVineFeature blueVineFeature, boolean value) {
        smallThreadLocal.set(value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lru/betterend/world/features/BlueVineFeature;small:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectGetSmall(BlueVineFeature blueVineFeature) {
        return smallThreadLocal.get();
    }

}
