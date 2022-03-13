package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.structure.MineshaftGenerator;
import net.minecraft.util.math.BlockBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(MineshaftGenerator.MineshaftRoom.class)
public class MixinMineshaftGeneratorMineshaftRoom {

    @Mutable
    @Shadow @Final private List<BlockBox> entrances;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.entrances = Collections.synchronizedList(this.entrances);
    }

}
