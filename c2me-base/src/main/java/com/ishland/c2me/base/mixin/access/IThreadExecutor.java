package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ThreadExecutor.class)
public interface IThreadExecutor {

    @Invoker
    boolean invokeRunTask();

}
