package com.ishland.c2me.opts.natives_math;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.opts.natives_math.common.NativeLoader;

public class ModuleEntryPoint {

    private static final boolean enabled;

    static {
        boolean configured = new ConfigSystem.ConfigAccessor()
                .key("vanillaWorldGenOptimizations.nativeAcceleration")
                .comment("""
                        Enable the use of bundled native libraries to accelerate world generation
                        """)
                .getBoolean(false, false);
        boolean actuallyEnabled = false;
        if (configured) {
            try {
                actuallyEnabled = Class.forName("com.ishland.c2me.opts.natives_math.common.NativeLoader").getField("linker").get(null) != null;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        enabled = false;
    }

}
