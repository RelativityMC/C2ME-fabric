package com.ishland.c2me.base.mixin.access;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerWorld.class)
public interface IServerWorld {

    @Accessor
    ServerEntityManager<Entity> getEntityManager();

}
