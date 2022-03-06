package com.ishland.c2me.threading.chunkio.common;

public class TaskCancellationException extends RuntimeException {

    public TaskCancellationException() {
        super();
    }

    public TaskCancellationException(String message) {
        super(message);
    }

    public TaskCancellationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskCancellationException(Throwable cause) {
        super(cause);
    }

    protected TaskCancellationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
