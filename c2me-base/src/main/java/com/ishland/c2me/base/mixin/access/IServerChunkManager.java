package com.ishland.c2me.base.mixin.access;

import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkManager.class)
public interface IServerChunkManager {

    @Accessor
    ChunkTicketManager getTicketManager();

    @Accessor
    ServerChunkManager.MainThreadExecutor getMainThreadExecutor();

    @Invoker
    boolean invokeUpdateChunks();

}
