package com.ishland.c2me.compatibility.mixin.betterend;

import com.ishland.c2me.compatibility.common.betterend.ThreadLocalMutableBlockPos;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.betterend.world.features.BiomeIslandFeature;

@Pseudo
@Mixin(BiomeIslandFeature.class)
public class MixinBiomeIslandFeature {

    @Mutable
    @Shadow(remap = false)
    @Final
    private static BlockPos.Mutable CENTER;

    @Dynamic
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo info) {
        CENTER = new ThreadLocalMutableBlockPos();
    }

}
