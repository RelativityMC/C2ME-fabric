/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
