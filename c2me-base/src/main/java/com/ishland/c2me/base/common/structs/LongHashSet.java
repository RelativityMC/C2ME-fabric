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
