package com.ishland.c2me.common.threading.worldgen.debug;


import com.google.common.collect.Sets;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.structure.processor.RuleStructureProcessor;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.WorldView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;

// Used to examine getChunk calls with reduced lock radius
public class StacktraceRecorder {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean doRecord = Boolean.getBoolean("com.ishland.c2me.common.threading.worldgen.debug.DebugReducedLockRadius");

    private static final Set<StacktraceHolder> recordedStacktraces = Sets.newConcurrentHashSet();

    public static void record() {
        if (!doRecord) return;
        final StacktraceHolder stacktraceHolder = new StacktraceHolder();
        if (recordedStacktraces.add(stacktraceHolder)) {
            if (stacktraceHolder.needPrint()) {
                LOGGER.warn("Recorded new stacktrace", stacktraceHolder.throwable);
            } else {
                LOGGER.info("Ignoring safe call");
            }
        }
    }


    public static class StacktraceHolder {

        private static final String StructureProcessor$process = FabricLoader.getInstance().getMappingResolver()
                .mapMethodName("intermediary", "net.minecraft.class_3491", "method_15110", "(Lnet/minecraft/class_4538;Lnet/minecraft/class_2338;Lnet/minecraft/class_2338;Lnet/minecraft/class_3499$class_3501;Lnet/minecraft/class_3499$class_3501;Lnet/minecraft/class_3492;)Lnet/minecraft/class_3499$class_3501;");
        private static final String BlockCollisionSpliterator$offerBlockShape = FabricLoader.getInstance().getMappingResolver()
                .mapMethodName("intermediary", "net.minecraft.class_5329", "method_29285", "(Ljava/util/function/Consumer;)Z");
        private static final String BiomeAccess$Storage$getBiomeForNoiseGen = FabricLoader.getInstance().getMappingResolver()
                .mapMethodName("intermediary", "net.minecraft.class_4543$class_4544", "method_16359", "(III)Lnet/minecraft/class_1959;");

        @NotNull
        private final StackTraceElement[] stackTrace;
        private final Throwable throwable;

        public StacktraceHolder() {
            this.throwable = new Throwable();
            this.stackTrace = this.throwable.getStackTrace();
        }

        public boolean needPrint() {
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (stackTraceElement.getMethodName().equals("method_26971"))
                    return false;
                if (stackTraceElement.getClassName().equals(RuleStructureProcessor.class.getName()) &&
                        stackTraceElement.getMethodName().equals(StructureProcessor$process))
                    return false;
                if (stackTraceElement.getClassName().equals(BlockCollisionSpliterator.class.getName()) &&
                        stackTraceElement.getMethodName().equals(BlockCollisionSpliterator$offerBlockShape))
                    return false;
                if (stackTraceElement.getClassName().equals(WorldView.class.getName()) &&
                        stackTraceElement.getMethodName().equals(BiomeAccess$Storage$getBiomeForNoiseGen))
                    return false;

                // lithium
                if (stackTraceElement.getClassName().equals("me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper"))
                    return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "StacktraceHolder{" +
                    "stackTrace=" + Arrays.toString(stackTrace) +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StacktraceHolder that = (StacktraceHolder) o;
            return Arrays.equals(stackTrace, that.stackTrace);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(stackTrace);
        }
    }

}
