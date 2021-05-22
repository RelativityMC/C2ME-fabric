package org.yatopiamc.c2me.mixin.threading.worldgen;

import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;

@Mixin(StructureManager.class)
public class MixinStructureManager {

    @Mutable
    @Shadow
    @Final
    private Map<Identifier, Structure> structures;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onPostInit(CallbackInfo info) {
        this.structures = Collections.synchronizedMap(structures);
    }

}
