package com.ishland.c2me.base.common.util;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Function;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RxJavaUtils {

    public static @NonNull Function<? super Flowable<Throwable>, @NonNull ? extends Publisher<@NonNull ?>> retryWithExponentialBackoff(int maxRetries, long initialDelayMillis) {
        List<Throwable> throwableList = Collections.synchronizedList(new ReferenceArrayList<>());
        return errors -> errors.zipWith(Flowable.range(1, maxRetries), (error, retryCount) -> {
            if (retryCount > maxRetries) {
                final RuntimeException exception = new RuntimeException("Max retries reached", error);
                throwableList.forEach(exception::addSuppressed);
                throw exception;
            }
            throwableList.add(error);
            return retryCount;
        }).flatMap(retryCount -> Flowable.timer((long) Math.pow(2, retryCount - 1) * initialDelayMillis, TimeUnit.MILLISECONDS));
    }

}
