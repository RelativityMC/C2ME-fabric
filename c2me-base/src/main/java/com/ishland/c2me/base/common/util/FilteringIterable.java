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

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilteringIterable<T> implements Iterable<T> {

    private final Iterable<T> delegate;
    private final Predicate<T> predicate;

    public FilteringIterable(Iterable<T> delegate, Predicate<T> predicate) {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new FilteringIterator();
    }

    private class FilteringIterator implements Iterator<T> {

        private final Iterator<T> iterator = delegate.iterator();
        private T next;

        @Override
        public boolean hasNext() {
            return this.calculateNext();
        }

        @Override
        public T next() {
            if (calculateNext()) {
                final T object = this.next;
                this.next = null;
                return object;
            }
            throw new NoSuchElementException();
        }

        private boolean calculateNext() {
            if (this.next != null) return true;
            while (iterator.hasNext()) {
                final T object = iterator.next();
                if (predicate.test(object)) {
                    this.next = object;
                    return true;
                }
            }
            return false;
        }

    }
}
