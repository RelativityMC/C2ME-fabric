package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkManager.class)
public interface IServerChunkManager {

    @Invoker
    boolean invokeTick();

}
