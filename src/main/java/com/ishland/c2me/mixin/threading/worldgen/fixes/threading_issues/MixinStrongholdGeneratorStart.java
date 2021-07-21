package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(StrongholdGenerator.Start.class)
public class MixinStrongholdGeneratorStart {

    @Mutable
    @Shadow @Final public List<StructurePiece> pieces;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.pieces = Collections.synchronizedList(pieces);
    }

}
