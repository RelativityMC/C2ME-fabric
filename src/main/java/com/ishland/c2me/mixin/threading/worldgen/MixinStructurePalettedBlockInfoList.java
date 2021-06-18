package com.ishland.c2me.mixin.threading.worldgen;

import net.minecraft.block.Block;
import net.minecraft.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Mixin(Structure.PalettedBlockInfoList.class)
public class MixinStructurePalettedBlockInfoList {

    @Mutable
    @Shadow @Final private Map<Block, List<Structure.StructureBlockInfo>> blockToInfos;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.blockToInfos = Collections.synchronizedMap(blockToInfos);
    }

}
