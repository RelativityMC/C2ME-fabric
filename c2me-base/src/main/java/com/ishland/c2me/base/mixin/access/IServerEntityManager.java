package com.ishland.c2me.base.mixin.access;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerEntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerEntityManager.class)
public interface IServerEntityManager {

    @Invoker
    LongSet invokeGetLoadedChunks();

}
