package com.ishland.c2me.mixin.optimization.chunkscheduling.idle_tasks.autosave.disable_vanilla_mid_tick_autosave;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Unique
    private static final Long2ObjectLinkedOpenHashMap<ChunkHolder> anEmptyChunkHoldersMap = new Long2ObjectLinkedOpenHashMap<>();

    @Redirect(method = "unloadChunks", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;chunkHolders:Lit/unimi/dsi/fastutil/longs/Long2ObjectLinkedOpenHashMap;"))
    private Long2ObjectLinkedOpenHashMap<ChunkHolder> stopAutoSaveInUnloading(ThreadedAnvilChunkStorage instance) {
        return anEmptyChunkHoldersMap; // prevent autosave from happening in unloading stage
    }

}
