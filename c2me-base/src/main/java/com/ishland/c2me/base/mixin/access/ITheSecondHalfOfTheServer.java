package com.ishland.c2me.base.mixin.access;

import net.minecraft.class_10961;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(class_10961.class)
public interface ITheSecondHalfOfTheServer {

    @Accessor("field_58252")
    Map<RegistryKey<World>, ServerWorld> getWorlds();

}
