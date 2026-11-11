/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ishland.c2me.notickvd.common;

import com.ishland.c2me.base.common.C2MEConstants;
import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.base.common.config.ModStatuses;

public class Config {

    public static final int maxConcurrentChunkLoads = (int) new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.maxConcurrentChunkLoads")
            .comment("No-tick view distance max concurrent chunk loads \n" +
                    " Lower this for a better latency and higher this for a faster loading")
            .getLong(GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM * 3L, GlobalExecutors.GLOBAL_EXECUTOR_PARALLELISM * 3L, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static final boolean enableExtRenderDistanceProtocol = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.enableExtRenderDistanceProtocol")
            .comment("""
                    Enable server-side support for extended render distance protocol (c2me:%s)
                    This allows requesting render distances higher than 127 chunks from the server
                    
                    Requires Fabric API (currently %s)
                    """.formatted(C2MEConstants.EXT_RENDER_DISTANCE_ID, ModStatuses.fabric_networking_api_v1 ? "available" : "unavailable"))
            .getBoolean(true, false);

    public static final boolean ensureChunkCorrectness = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.ensureChunkCorrectness")
            .comment("Whether to ensure correct chunks within normal render distance \n" +
                    " This will send chunks twice increasing network load")
            .getBoolean(false, true);

    public static final long chunkSendingSpeedMultiplierPercentage = new ConfigSystem.ConfigAccessor()
            .key("noTickViewDistance.chunkSendingSpeedMultiplierPercentage")
            .comment("""
                    Applies a multiplier *in percentage* to the target chunk sending rate from vanilla
                    Setting this to 0 disables rate limiting
                    
                    Defaults to 200, which is 200%
                    """)
            .getLong(200L, 100L, ConfigSystem.LongChecks.NON_NEGATIVE_VALUE_ONLY);

    public static final int maxViewDistance = 1 << 16;

    public static void init() {
    }

}
