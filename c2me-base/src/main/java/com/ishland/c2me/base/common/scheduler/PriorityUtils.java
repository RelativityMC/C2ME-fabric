package com.ishland.c2me.base.common.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class PriorityUtils {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME Priority System");

    // int32 S0000000 000MNNNN LLLLLLLL DDDDDDDD
    // S: sign bit always 0
    // M: clear if in sync load range and set if not
    // N: distance to sync load chunk
    // L: load level
    // D: distance to nearest player

    private static final AtomicInteger priorityChanges = new AtomicInteger(0);

    public static void notifyPriorityChange() {
//        priorityChanges.incrementAndGet();
    }

    public static void notifyPriorityChange(int amount) {
//        priorityChanges.addAndGet(amount);
    }

    public static int priorityChangeSerial() {
        return priorityChanges.get();
    }

}
