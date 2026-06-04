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
