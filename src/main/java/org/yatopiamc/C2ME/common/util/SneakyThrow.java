package org.yatopiamc.C2ME.common.util;

public class SneakyThrow {

    public static void sneaky(Throwable throwable) {
        throw0(throwable);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throw0(Throwable throwable) throws T {
        throw (T) throwable;
    }

}
