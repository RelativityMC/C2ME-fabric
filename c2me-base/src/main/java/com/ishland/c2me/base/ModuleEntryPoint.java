/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.base;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.base.common.threadpriority.ThreadPriorityPresets;
import io.netty.util.internal.PlatformDependent;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;

public class ModuleEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleEntryPoint.class);

    private static final boolean enabled = true;

    private static final String DEFAULT_EXPRESSION =
            """
                                    
                    max(
                        1,
                        min(
                            if( is_smt_probably_enabled,
                                cores,
                                cores - 1
                            )  - if(is_client, 1, 0),
                            ( ( mem_gb - (if(is_client, 1.0, 0.5)) ) * 3.0 )
                        )
                    )
                \040""";

    public static final String defaultGlobalExecutorParallelismExpression = new ConfigSystem.ConfigAccessor()
            .key("defaultGlobalExecutorParallelismExpression")
            .comment("""

                    The expression for the default value of global executor parallelism.\s
                    This is used when the parallelism isn't overridden.
                    Available variables: is_windows, is_j9vm, is_client, cpus, mem_gb, cores, is_smt_probably_enabled
                    """.indent(1))
            .getString(DEFAULT_EXPRESSION, DEFAULT_EXPRESSION);

    public static final ThreadPriorityPresets threadPoolPriorityPreset = new ConfigSystem.ConfigAccessor()
            .key("threadPoolPriorityPreset")
            .comment("""
                    Sets the thread priority preset for worker threads
                    
                    Available presets:
                    - UNSET: do not touch any thread priority settings
                    - BELOW_NORMAL:
                      Linux: Use SCHED_BATCH
                      Windows: Use Below Normal priority
                    - LOW:
                      Linux: Use SCHED_BATCH, nice value to 5
                      Windows: Use Below Normal priority, enable power throttling
                    - LOWER
                      Linux: Use SCHED_BATCH, nice value to 10
                      Windows: Use Lowest priority, enable power throttling
                    - IDLE
                      Linux: Use SCHED_IDLE
                      Windows: Use Idle priority, enable power throttling
                    
                    Defaults to LOW on clients and BELOW_NORMAL for dedicated servers
                    
                    Please preserve quotes so the config don't break
                    """)
            .getEnum(
                    ThreadPriorityPresets.class,
                    FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? ThreadPriorityPresets.LOW : ThreadPriorityPresets.BELOW_NORMAL,
                    ThreadPriorityPresets.LOW
            );

    public static final boolean disableLoggingShutdownHook = new ConfigSystem.ConfigAccessor()
            .key("fixes.disableLoggingShutdownHook")
            .comment("""
                    
                    Whether to disable the shutdown hook of log4j2 on dedicated servers.
                    Enabling this also makes the JVM exit when the dedicated server is considered fully shut down.
                    This option have no effect on client-side.
                    We has historically been doing this, and this config option allows you to disable this behavior.
                    """.indent(1))
            .incompatibleMod("textile_backup", "*")
            .getBoolean(true, false);

    public static final int defaultParallelism;

    private static int tryFetchCoreCount() {
        int logicalProcessorsCount = Runtime.getRuntime().availableProcessors();
        try {
            int coreCount = new SystemInfo().getHardware().getProcessor().getPhysicalProcessors().size();
            if (logicalProcessorsCount < coreCount) {
                LOGGER.warn("There's fewer logical processors than enumerated core count. Returning available logical processor count");
                coreCount = logicalProcessorsCount;
            }
            return coreCount;
        } catch (Throwable t) {
            LOGGER.error("Failed to fetch system core count, returning available logical processor count");
            return logicalProcessorsCount;
        }
    }

    private static int tryEvaluateExpression(String expression) {
        int coreCount = tryFetchCoreCount();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        LOGGER.info("Detected {} available physical CPUs, {} available logical CPUs", coreCount, availableProcessors);
        return (int) Math.max(1,
                new ExpressionBuilder(expression)
                        .variables("is_windows", "is_j9vm", "is_client", "cpus", "mem_gb", "cpus", "cores", "is_smt_probably_enabled")
                        .function(new Function("max", 2) {
                            @Override
                            public double apply(double... args) {
                                return Math.max(args[0], args[1]);
                            }
                        })
                        .function(new Function("min", 2) {
                            @Override
                            public double apply(double... args) {
                                return Math.min(args[0], args[1]);
                            }
                        })
                        .function(new Function("if", 3) {
                            @Override
                            public double apply(double... args) {
                                return args[0] != 0 ? args[1] : args[2];
                            }
                        })
                        .build()
                        .setVariable("is_windows", PlatformDependent.isWindows() ? 1 : 0)
                        .setVariable("is_j9vm", PlatformDependent.isJ9Jvm() ? 1 : 0)
                        .setVariable("is_client", FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? 1 : 0)
                        .setVariable("cpus", availableProcessors)
                        .setVariable("cores", coreCount)
                        .setVariable("is_smt_probably_enabled", availableProcessors > coreCount ? 1 : 0)
                        .setVariable("mem_gb", Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0)
                        .evaluate()
        );
    }

    public static final long globalExecutorParallelism;

    static {
        final int defaultEval = tryEvaluateExpression(DEFAULT_EXPRESSION);
        int value;
        try {
            value = tryEvaluateExpression(defaultGlobalExecutorParallelismExpression);
        } catch (Throwable t) {
            ConfigSystem.LOGGER.error("Failed to evaluate defaultGlobalExecutorParallelismExpression, falling back to default value", t);
            value = defaultEval;
        }

        defaultParallelism = value;
        globalExecutorParallelism = new ConfigSystem.ConfigAccessor()
                .key("globalExecutorParallelism")
                .comment("Configures the parallelism of global executor")
                .getLong(value, value, ConfigSystem.LongChecks.THREAD_COUNT);

        ConfigSystem.LOGGER.info("Global Executor Parallelism: {} configured, {} evaluated, {} default evaluated", globalExecutorParallelism, defaultParallelism, defaultEval);
    }

//    public static int getDefaultGlobalExecutorParallelism() {
//        return Math.max(1, Math.min(getDefaultParallelismCPU(), getDefaultParallelismHeap()));
//    }
//
//    private static int getDefaultParallelismCPU() {
//        if (PlatformDependent.isWindows()) {
//            return Math.max(1, (int) (Runtime.getRuntime().availableProcessors() / 1.6 - 2)) + defaultParallelismEnvTypeOffset();
//        } else {
//            return Math.max(1, (int) (Runtime.getRuntime().availableProcessors() / 1.2 - 2)) + defaultParallelismEnvTypeOffset();
//        }
//    }
//
//    private static int defaultParallelismEnvTypeOffset() {
//        return isClientSide() ? -2 : 0;
//    }
//
//    private static boolean isClientSide() {
//        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
//    }
//
//    private static int getDefaultParallelismHeap() {
//        if (PlatformDependent.isJ9Jvm()) {
//            return (int) ((memoryInGiB() + (isClientSide() ? -0.6 : -0.2)) / 0.5) + defaultParallelismEnvTypeOffset();
//        } else {
//            return (int) ((memoryInGiB() + (isClientSide() ? -1.2 : -0.6)) / 1.2) + defaultParallelismEnvTypeOffset();
//        }
//    }
//
//    private static double memoryInGiB() {
//        return Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 / 1024.0;
//    }


}
