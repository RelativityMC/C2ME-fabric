package com.ishland.c2me.base.common.util;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static String formatElapsedTime(long nanos) {
        if (nanos < 0) {
            return nanos + " ns";
        }
        if (nanos == 0) {
            return "0 ns";
        }

        TimeUnit unit = chooseUnit(nanos);
        double value = (double) nanos / TimeUnit.NANOSECONDS.convert(1, unit);

        return formatCompact4Digits(value) + " " + abbreviate(unit);
    }

    static String formatCompact4Digits(double value) {
        return String.format(Locale.ROOT, "%.4g", value);
    }

    private static TimeUnit chooseUnit(long nanos) {
        if (TimeUnit.DAYS.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.DAYS;
        }
        if (TimeUnit.HOURS.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.HOURS;
        }
        if (TimeUnit.MINUTES.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.MINUTES;
        }
        if (TimeUnit.SECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.SECONDS;
        }
        if (TimeUnit.MILLISECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.MILLISECONDS;
        }
        if (TimeUnit.MICROSECONDS.convert(nanos, TimeUnit.NANOSECONDS) > 0) {
            return TimeUnit.MICROSECONDS;
        }
        return TimeUnit.NANOSECONDS;
    }

    private static String abbreviate(TimeUnit unit) {
        return switch (unit) {
            case NANOSECONDS -> "ns";
            case MICROSECONDS -> "\u03bcs"; // μs
            case MILLISECONDS -> "ms";
            case SECONDS -> "s";
            case MINUTES -> "min";
            case HOURS -> "h";
            case DAYS -> "d";
            default -> throw new AssertionError();
        };
    }

}
