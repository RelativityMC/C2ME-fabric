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

package com.ishland.c2me.fixes.worldgen.threading_issues.common;

import com.ishland.c2me.base.common.config.ConfigSystem;

public class Config {

    public static final boolean enforceSafeWorldRandomAccess = new ConfigSystem.ConfigAccessor()
            .key("fixes.enforceSafeWorldRandomAccess")
            .comment("""
                        Enforces safe world random access. \s
                        This feature detects unsafe off-thread world random access, helping to find the causes \s
                        of mysterious "Accessing LegacyRandomSource from multiple threads" crash. \s
                        The default behavior is to fail hard when such bad things happens. \s
                        Disabling this option will replace this behavior with a warning. \s
                        
                        It is generally not recommended to disable this settings unless you know what you are doing \s
                        
                        """)
            .getBoolean(true,true);

    public static void init() {
    }

}
