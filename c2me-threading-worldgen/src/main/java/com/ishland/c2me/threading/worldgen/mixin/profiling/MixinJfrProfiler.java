package com.ishland.c2me.threading.worldgen.mixin.profiling;

import com.ishland.c2me.threading.worldgen.common.profiling.ChunkLoadScheduleEvent;
import com.ishland.c2me.threading.worldgen.common.profiling.IVanillaJfrProfiler;
import jdk.jfr.Event;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiling.jfr.Finishable;
import net.minecraft.util.profiling.jfr.JfrProfiler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(JfrProfiler.class)
public class MixinJfrProfiler implements IVanillaJfrProfiler {

    @Mutable
    @Shadow @Final private static List<Class<? extends Event>> EVENTS;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Object;<init>()V", shift = At.Shift.AFTER))
    private void preInit(CallbackInfo ci) {
        ArrayList<Class<? extends Event>> copy = new ArrayList<>(EVENTS);
        copy.add(ChunkLoadScheduleEvent.class);
        EVENTS = List.copyOf(copy);
    }


    @Override
    public Finishable startChunkLoadSchedule(ChunkPos chunkPos, RegistryKey<World> world, String targetStatus) {
        if (!ChunkLoadScheduleEvent.TYPE.isEnabled()) {
            return null;
        } else {
            ChunkLoadScheduleEvent event = new ChunkLoadScheduleEvent(chunkPos, world, targetStatus);
            event.begin();
            return event::commit;
        }
    }
}
