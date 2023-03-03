package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading_detections.random_instances;

import com.ishland.c2me.fixes.worldgen.threading_issues.common.CheckedThreadLocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public class MixinWorld {

    @Shadow @Final private Thread thread;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/random/Random;create()Lnet/minecraft/util/math/random/Random;"))
    private Random redirectWorldRandomInit() {
//        new CheckedThreadLocalRandom(RandomSeed.getSeed(), () -> new Thread()).nextInt();
        return new CheckedThreadLocalRandom(RandomSeed.getSeed(), () -> this.thread);
    }

}
