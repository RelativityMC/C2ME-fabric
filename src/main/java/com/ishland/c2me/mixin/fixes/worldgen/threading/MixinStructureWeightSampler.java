package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.fixes.worldgen.threading.ThreadLocalStructureWeightSampler;
import net.minecraft.world.gen.StructureWeightSampler;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureWeightSampler.class)
public class MixinStructureWeightSampler {

    @Mutable
    @Shadow @Final public static StructureWeightSampler INSTANCE;

    @Dynamic
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo info) {
        INSTANCE = new ThreadLocalStructureWeightSampler();
    }

}
