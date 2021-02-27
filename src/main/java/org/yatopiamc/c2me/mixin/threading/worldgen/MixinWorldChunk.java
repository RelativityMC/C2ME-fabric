package org.yatopiamc.c2me.mixin.threading.worldgen;

import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.util.Identifier;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.c2me.common.threading.chunkio.ICachedChunkTickScheduler;

import java.util.List;
import java.util.function.Function;

@Mixin(WorldChunk.class)
public class MixinWorldChunk {

    @Shadow @Final private World world;

    @Redirect(method = "enableTickSchedulers", at = @At(value = "NEW", target = "net/minecraft/server/world/SimpleTickScheduler"))
    private <T> SimpleTickScheduler<?> onSimpleTickSchedulerConstruct(Function<T, Identifier> identifierProvider, List<ScheduledTick<T>> scheduledTicks, long startTime) {
        final SimpleTickScheduler<T> scheduler = new SimpleTickScheduler<>(identifierProvider, scheduledTicks, startTime);
        if (!this.world.isClient)
            //noinspection ConstantConditions
            ((ICachedChunkTickScheduler) scheduler).setFallbackExecutor(this.world.getServer());
        return scheduler;
    }

}
