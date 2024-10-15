package com.ishland.c2me.base.mixin.access;

import net.minecraft.server.world.ChunkHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public interface IChunkHolder {

    @Invoker
    void invokeCombineSavingFuture(CompletableFuture<?> savingFuture);

}
