package com.ishland.c2me.opts.worldgen.general.mixin.random_instances;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CheckedRandom.Splitter.class)
public abstract class MixinAtomicSimpleRandomFactory implements RandomSplitter {

    @Shadow @Final private long seed;

    /**
     * @author ishland
     * @reason non-atomic
     */
    @Overwrite
    @Override
    public Random split(int x, int y, int z) { // TODO [VanillaCopy]
        long l = MathHelper.hashCode(x, y, z);
        long m = l ^ this.seed;
        return new LocalRandom(m);
    }

    /**
     * @author ishland
     * @reason non-atomic
     */
    @Overwrite
    @Override
    public Random split(String string) { // TODO [VanillaCopy]
        int i = string.hashCode();
        return new LocalRandom((long)i ^ this.seed);
    }

}
