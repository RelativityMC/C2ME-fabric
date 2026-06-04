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

package com.ishland.c2me.opts.natives_math;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class ModuleEntryPoint {

    public static final boolean allowAVX512 = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.nativeAcceleration.allowAVX512")
            .comment("""
                    Enable the use of AVX512 for native acceleration
                    
                    Currently, AVX512 implementation is generally slower than AVX2 implementations.
                    If you ever decide to enable this, make sure you have verified that
                    AVX512 implementations are faster on your machine.
                    """)
            .getBoolean(false, false);
    private static final boolean enabled;

    static {
        System.setProperty("com.ishland.c2me.opts.natives_math.duringGameInit", "true");
        boolean configured = new ConfigSystem.ConfigAccessor()
                .key("vanillaWorldGenOptimizations.nativeAcceleration.enabled")
                .comment("""
                        Enable the use of bundled native libraries to accelerate world generation
                        """)
                .getBoolean(true, false);
        boolean actuallyEnabled = false;
        if (configured) {
            try {
                actuallyEnabled = Class.forName("com.ishland.c2me.opts.natives_math.common.NativeLoader").getField("lookup").get(null) != null;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        enabled = actuallyEnabled;
    }

}
