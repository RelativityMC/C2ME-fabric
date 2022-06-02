package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import net.minecraft.structure.StructureTemplate;

@Mixin(StructureTemplate.class)
public class MixinStructure {

    @Mutable
    @Shadow
    @Final
    private List<StructureTemplate.PalettedBlockInfoList> blockInfoLists;

    @Mutable
    @Shadow
    @Final
    private List<StructureTemplate.StructureEntityInfo> entities;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.blockInfoLists = Collections.synchronizedList(blockInfoLists);
        this.entities = Collections.synchronizedList(entities);
    }

}
