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

package com.ishland.c2me.opts.chunkio.common;

import com.ishland.c2me.base.common.config.ConfigSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME Opts/ChunkIO Config");

    public static final long chunkDataCacheSoftLimit = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.chunkDataCacheSoftLimit")
            .comment("Soft limit for io worker nbt cache")
            .getLong(8192, 8192, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static final long chunkDataCacheLimit = new ConfigSystem.ConfigAccessor()
            .key("ioSystem.chunkDataCacheLimit")
            .comment("Hard limit for io worker nbt cache")
            .getLong(32678, 32678, ConfigSystem.LongChecks.POSITIVE_VALUES_ONLY);

    public static void init() {
    }

}
