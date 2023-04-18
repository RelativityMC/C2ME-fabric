package com.ishland.c2me.opts.worldgen.general.common.random_instances;

import com.ishland.c2me.base.mixin.access.IAtomicSimpleRandomDeriver;
import com.ishland.c2me.base.mixin.access.ISimpleRandom;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandom;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandomDeriver;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandomImpl;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

public class RandomUtils {

    private static final ThreadLocal<Xoroshiro128PlusPlusRandom> xoroshiro = ThreadLocal.withInitial(() -> new Xoroshiro128PlusPlusRandom(0L, 0L));
    private static final ThreadLocal<LocalRandom> simple = ThreadLocal.withInitial(() -> new LocalRandom(0L));

    public static void derive(RandomSplitter deriver, Random random, int x, int y, int z) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.Splitter) {
            final IXoroshiro128PlusPlusRandomImpl implementation = (IXoroshiro128PlusPlusRandomImpl) ((IXoroshiro128PlusPlusRandom) random).getImplementation();
            final IXoroshiro128PlusPlusRandomDeriver deriver1 = (IXoroshiro128PlusPlusRandomDeriver) deriver;
            implementation.setSeedLo(MathHelper.hashCode(x, y, z) ^ deriver1.getSeedLo());
            implementation.setSeedHi(deriver1.getSeedHi());
            return;
        }
        if (deriver instanceof CheckedRandom.Splitter) {
            final ISimpleRandom random1 = (ISimpleRandom) random;
            final IAtomicSimpleRandomDeriver deriver1 = (IAtomicSimpleRandomDeriver) deriver;
            random1.invokeSetSeed(MathHelper.hashCode(x, y, z) ^ deriver1.getSeed());
            return;
        }
        throw new IllegalArgumentException();
    }

    public static Random getThreadLocalRandom(RandomSplitter deriver) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.Splitter) {
            return xoroshiro.get();
        }
        if (deriver instanceof CheckedRandom.Splitter) {
            return simple.get();
        }
        throw new IllegalArgumentException();
    }

    public static Random getRandom(RandomSplitter deriver) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.Splitter) {
            return new Xoroshiro128PlusPlusRandom(0L, 0L);
        }
        if (deriver instanceof CheckedRandom.Splitter) {
            return new LocalRandom(0L);
        }
        throw new IllegalArgumentException();
    }

}
