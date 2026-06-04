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

package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean asyncSerialization = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.asyncSerialization")
            .comment("""
                    Whether to enable async serialization
                    """)
            .getBoolean(true, false);

    public static final boolean recoverFromErrors = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.recoverFromErrors")
            .comment("""
                    Whether to recover from errors when loading chunks\s
                     This will cause errored chunk to be regenerated entirely, which may cause data loss\s
                     Only applies when async chunk loading is enabled
                     """)
            .getBoolean(false, false);

    public static final boolean allowPOIUnloading = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.allowPOIUnloading")
            .comment("""
                    Whether to allow POIs (Point of Interest) to be unloaded
                    Unloaded POIs are reloaded on-demand or when the corresponding chunks are loaded again,
                    which should not cause any behavior change
                    \s
                    Note:
                    Vanilla never unloads POIs when chunks unload, causing small memory leaks
                    These leaks adds up and eventually cause issues after generating millions of chunks
                    in a single world instance
                    """)
            .getBoolean(true, false);

    public static final boolean suppressGhostMushrooms = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.suppressGhostMushrooms")
            .comment("""
                    This option workarounds MC-276863, a bug that makes mushrooms appear in non-postprocessed chunks
                    This bug is amplified with notickvd as it exposes non-postprocessed chunks to players
                    
                    This should not affect other worldgen behavior and game mechanics in general
                    """)
            .getBoolean(true, false);

    public static final boolean syncPlayerTickets = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.syncPlayerTickets")
            .comment("""
                    Whether to synchronize the management of player tickets
                    
                    In vanilla Minecraft, player tickets are not always removed immediately when players leave an area.
                    The delay in removal increases with the chunk system’s throughput, but due to vanilla’s typically
                    slow chunk loading, tickets are almost always removed immediately. However, some contraptions rely
                    on this immediate removal behavior and tend to be broken with the increased chunk throughput.
                    Enabling this option synchronizes player ticket handling, making it more predictable and
                    thus improving compatibility with these contraptions.
                    """)
            .getBoolean(true, false);

    public static final boolean fluidPostProcessingToScheduledTick = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.fluidPostProcessingToScheduledTick")
            .comment("""
                    Whether to turn fluid postprocessing into scheduled tick
                    
                    Fluid post-processing is very expensive when loading in new chunks, and this can affect
                    MSPT significantly. This option delays fluid post-processing to scheduled tick to hopefully
                    mitigate this issue.
                    """)
            .getBoolean(true, false);

    public static final boolean filterFluidPostProcessing = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.filterFluidPostProcessing")
            .comment("""
                    Whether to filter fluid post-processing on worldgen threads
                    
                    The worldgen processes creates a lot of unnecessary fluid post-processing tasks,
                    which can overload the server thread and cause stutters.
                    This applies a rough filter to filter out fluids that are definitely not going to flow
                    """)
            .getBoolean(true, false);

    public static final boolean useLegacyScheduling = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.useLegacyScheduling")
            .comment("""
                    Whether to use legacy scheduling for neighbor chunks
                    
                    Enabling this restores the behavior of always loading in neighbor chunks when a chunk is loaded.
                    
                    This is currently deprecated and will be removed in the future.
                    """)
            .getBoolean(false, false);

    public static final boolean lowMemoryMode = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.lowMemoryMode")
            .comment("""
                    Whether to enable low memory mode
                    
                    This option will attempt to aggressively unload unused chunks.
                    Only applies when useLegacyScheduling is disabled.
                    """)
            .getBoolean(false, false);

    public static final boolean delayFullChunkEvents = new ConfigSystem.ConfigAccessor()
            .key("chunkSystem.delayFullChunkEvents")
            .comment("""
                    Whether to delay full chunk events to after full chunk future have been completed
                    This restores the buggy behavior in NeoForge that the ecosystem have been relying on
                    
                    Enabling this will almost certainly break vanilla behavior, disabled by default on fabric
                    Do not change this unless you know what you are doing
                    """)
            .getBoolean(false, false);

    public static void init() {
        // intentionally empty
    }

}
