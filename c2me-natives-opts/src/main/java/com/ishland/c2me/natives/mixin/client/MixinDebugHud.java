package com.ishland.c2me.natives.mixin.client;

import com.ishland.c2me.natives.common.MathUtil;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class MixinDebugHud {

    @Inject(method = "getRightText", at = @At("RETURN"))
    private void modifyRightText(CallbackInfoReturnable<List<String>> cir) {
        final List<String> list = cir.getReturnValue();
        list.add(3, String.format("C2ME Natives: %s", MathUtil.humanReadableByteCountBin(NativeMemoryTracker.getAllocatedBytes())));
    }

}
