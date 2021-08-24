package com.ishland.c2me.mixin.access;

import net.minecraft.server.world.ChunkTicket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkTicket.class)
public interface IChunkTicket {

    @Invoker
    boolean invokeIsExpired(long currentTick);

}
