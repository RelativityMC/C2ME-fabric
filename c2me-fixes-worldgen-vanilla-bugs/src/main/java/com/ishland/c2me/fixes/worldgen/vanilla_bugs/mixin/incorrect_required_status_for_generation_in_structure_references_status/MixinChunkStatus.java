package com.ishland.c2me.fixes.worldgen.vanilla_bugs.mixin.incorrect_required_status_for_generation_in_structure_references_status;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChunkStatus.class)
public class MixinChunkStatus {

    @Mutable
    @Shadow @Final private static List<ChunkStatus> DISTANCE_TO_STATUS;

    @Dynamic
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void appendStructureStarts(CallbackInfo info) {
        final List<ChunkStatus> distanceToStatus = new ArrayList<>(DISTANCE_TO_STATUS);
        distanceToStatus.add(ChunkStatus.STRUCTURE_STARTS);
        DISTANCE_TO_STATUS = ImmutableList.copyOf(distanceToStatus);
    }

}
