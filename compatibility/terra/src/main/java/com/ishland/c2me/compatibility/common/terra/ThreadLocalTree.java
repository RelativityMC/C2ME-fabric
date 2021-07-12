package com.ishland.c2me.compatibility.common.terra;

import com.dfsek.terra.api.math.vector.Location;
import com.dfsek.terra.api.platform.world.Tree;
import com.dfsek.terra.api.util.collections.MaterialSet;

import java.util.Random;

public record ThreadLocalTree(ThreadLocal<Tree> delegate) implements Tree {

    @Override
    public boolean plant(Location l, Random r) {
        return delegate.get().plant(l, r);
    }

    @Override
    public MaterialSet getSpawnable() {
        return delegate.get().getSpawnable();
    }
}
