package com.ishland.c2me.compatibility.mixin.terra;

import com.dfsek.terra.api.TerraPlugin;
import com.dfsek.terra.api.platform.world.Tree;
import com.dfsek.terra.config.factories.TreeFactory;
import com.dfsek.terra.config.templates.TreeTemplate;
import com.dfsek.terra.world.population.items.tree.TerraTree;
import com.ishland.c2me.compatibility.common.terra.ThreadLocalTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TreeFactory.class)
public class MixinTreeFactory {

    /**
     * @author ishland
     * @reason thread-local tree
     */
    @Overwrite(remap = false)
    public Tree build(TreeTemplate config, TerraPlugin main) {
        return new ThreadLocalTree(ThreadLocal.withInitial(() -> new TerraTree(config.getSpawnable(), config.getyOffset(), config.getStructures())));
    }

}
