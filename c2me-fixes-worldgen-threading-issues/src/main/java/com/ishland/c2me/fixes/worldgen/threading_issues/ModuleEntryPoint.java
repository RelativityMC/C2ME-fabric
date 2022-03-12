package com.ishland.c2me.fixes.worldgen.threading_issues;

import com.ishland.c2me.fixes.worldgen.threading_issues.common.DataFixerUpperClasspathFix;

public class ModuleEntryPoint {

    private static final boolean enabled = true;

    static {
        DataFixerUpperClasspathFix.fix();
    }

}
