package com.ishland.c2me.opts.natives_math;

import com.ishland.c2me.base.common.config.ConfigSystem;
import com.ishland.c2me.opts.natives_math.common.NativeLoader;

public class ModuleEntryPoint {

    public static final boolean AVAILABLE = NativeLoader.lookup != null;
    private static final boolean enabled = new ConfigSystem.ConfigAccessor()
            .key("vanillaWorldGenOptimizations.nativeAcceleration")
            .comment("""
                    Enable the use of bundled native libraries to accelerate world generation
                    Availability on your system in the last game launch: %s
                    
                    The implementation currently have slight relative error from the vanilla implementation
                    (generally within a few ulps)
                    """.formatted(NativeLoader.getAvailabilityString()))
            .getBoolean(true, false) && AVAILABLE;

}
