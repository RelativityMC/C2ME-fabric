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

package com.ishland.c2me.base.common.structs;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class LongHashSet implements LongSet {

    private final HashSet<Long> delegate = new HashSet<>();

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public @NotNull LongIterator iterator() {
        final Iterator<Long> iterator = delegate.iterator();
        return new LongIterator() {
            @Override
            public long nextLong() {
                return iterator.next();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return delegate.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T @NotNull [] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Long> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean add(long key) {
        return delegate.add(key);
    }

    @Override
    public boolean contains(long key) {
        return delegate.contains(key);
    }

    @Override
    public long[] toLongArray() {
        return delegate.stream().mapToLong(value -> value).toArray();
    }

    @Override
    public long[] toLongArray(long[] a) {
        final long[] longs = toLongArray();
        for (int i = 0; i < longs.length && i < a.length; i++) {
            a[i] = longs[i];
        }
        return a;
    }

    @Override
    public long[] toArray(long[] a) {
        return toLongArray(a);
    }

    @Override
    public boolean addAll(LongCollection c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean containsAll(LongCollection c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean removeAll(LongCollection c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(LongCollection c) {
        return delegate.retainAll(c);
    }

    @Override
    public boolean remove(long k) {
        return delegate.remove(k);
    }
}
