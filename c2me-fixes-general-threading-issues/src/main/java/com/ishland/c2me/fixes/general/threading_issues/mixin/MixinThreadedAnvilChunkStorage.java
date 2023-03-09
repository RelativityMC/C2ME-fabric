package com.ishland.c2me.fixes.general.threading_issues.mixin;

import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow @Final private ServerWorld world;

    @Redirect(method = "getChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage$TicketManager;addTicketWithLevel(Lnet/minecraft/server/world/ChunkTicketType;Lnet/minecraft/util/math/ChunkPos;ILjava/lang/Object;)V"))
    private <T> void redirectAddLightTicket(ThreadedAnvilChunkStorage.TicketManager ticketManager, ChunkTicketType<T> type, ChunkPos pos, int level, T argument) {
        if (this.world.getServer().getThread() != Thread.currentThread()) {
            this.mainThreadExecutor.execute(() -> ticketManager.addTicketWithLevel(type, pos, level, argument));
        } else {
            ticketManager.addTicketWithLevel(type, pos, level, argument);
        }
    }

    @Redirect(method = "upgradeChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getProfiler()Lnet/minecraft/util/profiler/Profiler;"))
    private Profiler removeProfilerUsage(ServerWorld instance) {
        return DummyProfiler.INSTANCE;
    }

}
