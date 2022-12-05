package com.ishland.c2me.opts.scheduling.mixin.idle_tasks.autosave.enhanced_autosave;

import com.ishland.c2me.opts.scheduling.common.idle_tasks.IThreadedAnvilChunkStorage;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk extends Chunk {

    public MixinWorldChunk(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biome, long inhabitedTime, @Nullable ChunkSection[] sectionArrayInitializer, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biome, inhabitedTime, sectionArrayInitializer, blendingData);
    }

    @Inject(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/WorldChunk;needsSaving:Z"))
    private void onSetShouldSave(CallbackInfo ci) {
        //noinspection ConstantConditions
        if (this.needsSaving && (Object) this instanceof WorldChunk worldChunk) {
            if (worldChunk.getWorld() instanceof ServerWorld serverWorld) {
                ((IThreadedAnvilChunkStorage) serverWorld.getChunkManager().threadedAnvilChunkStorage).enqueueDirtyChunkPosForAutoSave(this.getPos());
            }
        }
    }

}
