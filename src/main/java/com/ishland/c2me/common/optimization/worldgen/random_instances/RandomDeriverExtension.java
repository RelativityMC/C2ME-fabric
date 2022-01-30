package com.ishland.c2me.common.optimization.worldgen.random_instances;

import net.minecraft.world.gen.random.AbstractRandom;

public interface RandomDeriverExtension {

    AbstractRandom createRandomTo(int x, int y, int z, AbstractRandom random);

}
