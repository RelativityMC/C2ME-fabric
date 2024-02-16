package com.ishland.c2me.base.mixin.access;

import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SyncedClientOptions.class)
public interface ISyncedClientOptions {

    @Mutable
    @Accessor
    void setViewDistance(int viewDistance);

}
