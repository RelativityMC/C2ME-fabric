package com.ishland.c2me.base.mixin.access;

import net.minecraft.world.gen.chunk.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Blender.class)
public interface IBlender {

    @Accessor
    static int getBLENDING_CHUNK_DISTANCE_THRESHOLD() {
        throw new AbstractMethodError();
    }

}
