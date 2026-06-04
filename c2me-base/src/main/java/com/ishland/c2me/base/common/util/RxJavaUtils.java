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

    public static @NonNull Function<? super Flowable<Throwable>, @NonNull ? extends Publisher<@NonNull ?>> retryWithExponentialBackoff(int maxRetries, long initialDelayMillis, Throwable... existingErrors) {
        List<Throwable> throwableList = Collections.synchronizedList(new ReferenceArrayList<>());
        throwableList.addAll(List.of(existingErrors));
        return errors -> errors.zipWith(Flowable.range(1, maxRetries + 1), (error, retryCount) -> {
            throwableList.add(error);
            if (retryCount > maxRetries) {
                final RuntimeException exception = new StacklessRuntimeException("Max retries reached");
                throwableList.forEach(exception::addSuppressed);
                throw exception;
            }
            return retryCount;
        }).flatMap(retryCount -> Flowable.timer((long) Math.pow(2, retryCount - 1) * initialDelayMillis, TimeUnit.MILLISECONDS));
    }

    public static class StacklessRuntimeException extends RuntimeException {
        public StacklessRuntimeException(String message) {
            super(message, null, true, false);
        }
    }

}
