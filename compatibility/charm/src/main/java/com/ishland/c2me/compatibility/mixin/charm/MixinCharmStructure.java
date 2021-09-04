package com.ishland.c2me.compatibility.mixin.charm;

import com.mojang.datafixers.util.Pair;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import svenhjol.charm.world.CharmStructure;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Mixin(CharmStructure.class)
public class MixinCharmStructure {

    @Mutable
    @Shadow @Final private List<Pair<Function<StructurePool.Projection, ? extends StructurePoolElement>, Integer>> starts;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.starts = Collections.synchronizedList(this.starts);
    }

}
