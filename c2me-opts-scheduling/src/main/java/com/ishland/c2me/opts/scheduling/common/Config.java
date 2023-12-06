package com.ishland.c2me.opts.scheduling.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final long midTickChunkTasksInterval = new ConfigSystem.ConfigAccessor()
            .key("generalOptimizations.midTickChunkTasksInterval")
            .comment("""
                    The task interval of mid-tick chunk tasks in nanoseconds (-1 to disable) \s
                    Mid-tick chunk tasks is to execute chunk tasks during server tick loop \s
                    to speed up chunk loading and generation \s
                    This helps chunks loading and generating under high MSPT but may raise \s
                    MSPT when chunks are loading or generating \s
                    \s
                    It is generally not recommended to adjust this value unless you know \s
                    what you are doing \s
                    \s
                    Incompatible with Dimensional Threading (dimthread)
                    """)
            .incompatibleMod("dimthread", "*")
            .getLong(100_000, -1);

    public static final AutoSaveMode autoSaveMode = new ConfigSystem.ConfigAccessor()
            .key("generalOptimizations.autoSave.mode")
            .comment("""
                    Defines how auto save should be handled \s
                    VANILLA: Use vanilla auto-save behavior (auto-save performed every tick during ticking) \s
                    ENHANCED: Use C2ME enhanced auto-save (auto-save performed when the server have spare time after ticking) \s
                    PERIODIC: Use pre-1.18 vanilla auto-save behavior (auto-save performed every 6000 ticks during ticking) \s
                    \s
                    Please preserve quotes so this config don't break
                    """)
            .getEnum(AutoSaveMode.class, AutoSaveMode.ENHANCED, AutoSaveMode.VANILLA);

    public static void init() {
    }


    public enum AutoSaveMode {
        VANILLA(false, false),
        ENHANCED(true, true),
        PERIODIC(true, false);

        public final boolean disableVanillaMidTickAutoSave;
        public final boolean enableEnhancedAutoSave;

        AutoSaveMode(boolean disableVanillaMidTickAutoSave, boolean enableEnhancedAutoSave) {
            this.disableVanillaMidTickAutoSave = disableVanillaMidTickAutoSave;
            this.enableEnhancedAutoSave = enableEnhancedAutoSave;
        }
    }

}
