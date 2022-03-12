package com.ishland.c2me.base.common.util;

public class BooleanUtils {

    public static boolean parseBoolean(String value) {
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new BooleanFormatException(value);
    }

    public static class BooleanFormatException extends RuntimeException {
        public BooleanFormatException() {
            super();
        }

        public BooleanFormatException(String message) {
            super(message);
        }

        public BooleanFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        public BooleanFormatException(Throwable cause) {
            super(cause);
        }

        protected BooleanFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
