package com.ishland.c2me.notickvd.common;

import net.minecraft.server.network.ServerPlayerEntity;

public class NoTickChunkSendingInterceptor {

    public static boolean onChunkSending(ServerPlayerEntity player, long pos) {
        return true;
    }

}
