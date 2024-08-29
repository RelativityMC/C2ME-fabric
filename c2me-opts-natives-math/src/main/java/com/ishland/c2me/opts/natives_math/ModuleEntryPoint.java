package com.ishland.c2me.opts.natives_math;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.opts.natives_math.common.NativeLoader;

public class ModuleEntryPoint {

    private static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.nativeAcceleration")
            .comment("""
                    Enable the use of bundled native libraries to accelerate world generation
                    Availability on your system in the last game launch: %s
                    """.formatted(NativeLoader.getAvailabilityString()))
            .getBoolean(false, false) && NativeLoader.lookup != null;

}
