package com.ishland.c2me.opts.scheduling.common;

import net.minecraft.server.world.ServerWorld;

public interface ServerMidTickTask {

    void executeTasksMidTick(ServerWorld world);

}
