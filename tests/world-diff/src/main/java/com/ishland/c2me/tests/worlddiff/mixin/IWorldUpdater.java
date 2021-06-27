package com.ishland.c2me.tests.worlddiff.mixin;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.updater.WorldUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(WorldUpdater.class)
public interface IWorldUpdater {

    @Invoker
    List<ChunkPos> invokeGetChunkPositions(RegistryKey<World> world);

}
