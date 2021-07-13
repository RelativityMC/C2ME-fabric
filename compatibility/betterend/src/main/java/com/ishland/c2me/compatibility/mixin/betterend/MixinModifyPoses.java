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
import ru.betterend.world.features.FullHeightScatterFeature;
import ru.betterend.world.features.InvertedScatterFeature;
import ru.betterend.world.features.ScatterFeature;
import ru.betterend.world.features.SilkMothNestFeature;
import ru.betterend.world.features.UnderwaterPlantScatter;
import ru.betterend.world.features.terrain.DesertLakeFeature;
import ru.betterend.world.features.terrain.EndLakeFeature;
import ru.betterend.world.features.terrain.SulphuricLakeFeature;

@Pseudo
@Mixin({DesertLakeFeature.class,
        EndLakeFeature.class,
        FullHeightScatterFeature.class,
        InvertedScatterFeature.class,
        ScatterFeature.class,
        SilkMothNestFeature.class,
        SulphuricLakeFeature.class,
        UnderwaterPlantScatter.class})
public class MixinModifyPoses {

    @Mutable
    @Shadow(remap = false)
    @Final
    private static BlockPos.Mutable POS;

    @Dynamic
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo info) {
        POS = new ThreadLocalMutableBlockPos();
    }

}
