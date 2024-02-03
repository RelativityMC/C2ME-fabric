package com.ishland.c2me.tests.worlddiff.mixin;

import net.minecraft.world.updater.WorldUpdater;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldUpdater.class)
public interface IWorldUpdater {

//    @Invoker
//    List<ChunkPos> invokeGetChunkPositions(RegistryKey<World> world);

}
