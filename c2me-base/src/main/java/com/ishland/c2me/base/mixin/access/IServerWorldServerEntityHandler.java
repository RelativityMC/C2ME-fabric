package com.ishland.c2me.base.mixin.access;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.server.world.ServerWorld$ServerEntityHandler")
public interface IServerWorldServerEntityHandler {

    @Accessor("field_26936")
    ServerWorld getParentInstance();

}
