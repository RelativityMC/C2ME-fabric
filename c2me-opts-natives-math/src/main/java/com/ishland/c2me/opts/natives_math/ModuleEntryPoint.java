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
