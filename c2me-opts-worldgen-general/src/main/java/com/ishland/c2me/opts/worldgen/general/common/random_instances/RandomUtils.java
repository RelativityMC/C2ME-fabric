package com.ishland.c2me.opts.worldgen.general.common.random_instances;

import com.ishland.c2me.base.mixin.access.IAtomicSimpleRandomDeriver;
import com.ishland.c2me.base.mixin.access.ISimpleRandom;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandom;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandomDeriver;
import com.ishland.c2me.base.mixin.access.IXoroshiro128PlusPlusRandomImpl;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.random.AbstractRandom;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.RandomDeriver;
import net.minecraft.world.gen.random.SimpleRandom;
import net.minecraft.world.gen.random.Xoroshiro128PlusPlusRandom;

public class RandomUtils {

    private static final ThreadLocal<Xoroshiro128PlusPlusRandom> xoroshiro = ThreadLocal.withInitial(() -> new Xoroshiro128PlusPlusRandom(0L, 0L));
    private static final ThreadLocal<SimpleRandom> simple = ThreadLocal.withInitial(() -> new SimpleRandom(0L));

    public static void derive(RandomDeriver deriver, AbstractRandom random, int x, int y, int z) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.RandomDeriver) {
            final IXoroshiro128PlusPlusRandomImpl implementation = (IXoroshiro128PlusPlusRandomImpl) ((IXoroshiro128PlusPlusRandom) random).getImplementation();
            final IXoroshiro128PlusPlusRandomDeriver deriver1 = (IXoroshiro128PlusPlusRandomDeriver) deriver;
            implementation.setSeedLo(MathHelper.hashCode(x, y, z) ^ deriver1.getSeedLo());
            implementation.setSeedHi(deriver1.getSeedHi());
            return;
        }
        if (deriver instanceof AtomicSimpleRandom.RandomDeriver) {
            final ISimpleRandom random1 = (ISimpleRandom) random;
            final IAtomicSimpleRandomDeriver deriver1 = (IAtomicSimpleRandomDeriver) deriver;
            random1.setSeed(MathHelper.hashCode(x, y, z) ^ deriver1.getSeed());
            return;
        }
        throw new IllegalArgumentException();
    }

    public static AbstractRandom getThreadLocalRandom(RandomDeriver deriver) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.RandomDeriver) {
            return xoroshiro.get();
        }
        if (deriver instanceof AtomicSimpleRandom.RandomDeriver) {
            return simple.get();
        }
        throw new IllegalArgumentException();
    }

    public static AbstractRandom getRandom(RandomDeriver deriver) {
        if (deriver instanceof Xoroshiro128PlusPlusRandom.RandomDeriver) {
            return new Xoroshiro128PlusPlusRandom(0L, 0L);
        }
        if (deriver instanceof AtomicSimpleRandom.RandomDeriver) {
            return new SimpleRandom(0L);
        }
        throw new IllegalArgumentException();
    }

}
