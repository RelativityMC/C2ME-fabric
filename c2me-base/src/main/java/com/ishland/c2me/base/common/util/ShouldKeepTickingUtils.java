package com.ishland.c2me.base.common.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public class ShouldKeepTickingUtils {

    public static BooleanSupplier minimumTicks(BooleanSupplier delegate, int minimum) {
        AtomicInteger ticks = new AtomicInteger(0);
        return () -> ticks.getAndIncrement() < minimum || delegate.getAsBoolean();
    }

}
