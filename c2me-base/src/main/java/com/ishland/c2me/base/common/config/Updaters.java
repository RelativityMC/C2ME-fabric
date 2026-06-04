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

package com.ishland.c2me.base.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class Updaters {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME Config Updater");
    private static final Long2ObjectOpenHashMap<Consumer<Config>> updaters = new Long2ObjectOpenHashMap<>();

    static {
        updaters.put(0, source -> {
            Queue<Runnable> pendingActions = new LinkedList<>();
            Queue<Config> pendingConfigs = new LinkedList<>();
            pendingConfigs.add(source);
            while (!pendingConfigs.isEmpty()) {
                final Config config = pendingConfigs.poll();
                for (Config.Entry entry : config.entrySet()) {
                    if (entry.getValue() instanceof Config) pendingConfigs.add(entry.getValue());
                    else pendingActions.add(() -> entry.setValue("default"));
                }
            }
            pendingActions.forEach(Runnable::run);
        });
        updaters.put(1, source -> {
            final Object _ioSystem = source.get("asyncIO");
            if (_ioSystem instanceof CommentedConfig ioSystem) {
                // rename asyncIO to ioSystem
                source.remove("asyncIO");
                source.add("ioSystem", ioSystem);

                final Object _async = ioSystem.get("enabled");
                if (_async instanceof Boolean async) {
                    // rename enabled to async
                    ioSystem.remove("enabled");
                    ioSystem.add("async", async);
                }
            }
        });
        updaters.put(2, source -> {
            final Object _generalOptimizations = source.get("generalOptimizations");
            final Object _ioSystem = source.get("ioSystem");
            if (_generalOptimizations instanceof CommentedConfig generalOptimizations &&
                    _ioSystem instanceof CommentedConfig ioSystem) {
                final Object _chunkStreamVersion = generalOptimizations.get("chunkStreamVersion");
                if (_chunkStreamVersion instanceof Number chunkStreamVersion) {
                    // move to ioSystem
                    generalOptimizations.remove("chunkStreamVersion");
                    ioSystem.add("chunkStreamVersion", chunkStreamVersion);
                }
            }
        });
    }

    public static void update(Config config) {
        while (true) {
            final long version = config.getLongOrElse("version", 0);
            final Consumer<Config> updater = updaters.get(version);
            if (updater != null) {
                LOGGER.info("Updating config from {} to {}", version, version + 1);
                updater.accept(config);
                config.set("version", version + 1);
            } else {
                break;
            }
        }
    }

}
