package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.base.common.util.FilteringIterable;
import com.ishland.c2me.base.mixin.access.IChunkLevelManager;
import com.ishland.c2me.base.mixin.access.ISimulationDistanceLevelPropagator;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpawnHelper.class)
public class MixinSpawnHelper {

    @WrapOperation(method = "setupSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;iterateEntities()Ljava/lang/Iterable;"))
    private static Iterable<Entity> redirectIterateEntities(ServerWorld serverWorld, Operation<Iterable<Entity>> op) {
        Long2ByteMap trackedChunks = ((ISimulationDistanceLevelPropagator) ((IChunkLevelManager) ((IThreadedAnvilChunkStorage) serverWorld.getChunkManager().chunkLoadingManager).getLevelManager()).getSimulationDistanceLevelPropagator()).getLevels();
        return new FilteringIterable<>(op.call(serverWorld), entity -> trackedChunks.containsKey(entity.getChunkPos().toLong()));
    }

}
