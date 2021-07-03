package com.ishland.c2me.tests.testmod;

import java.util.function.BooleanSupplier;

public class ShouldKeepTickingUtils {

    public static BooleanSupplier maxTime(int timeMillis) {
        long startTime = System.currentTimeMillis();
        return () -> System.currentTimeMillis() - startTime <= timeMillis;
    }

}
