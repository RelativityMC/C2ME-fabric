package com.ishland.c2me.common.util;

import net.minecraft.util.collection.IndexedIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public record ListIndexedIterable<T>(List<T> delegate) implements IndexedIterable<T> {

    @Override
    public int getRawId(T entry) {
        return delegate.indexOf(entry);
    }

    @Nullable
    @Override
    public T get(int index) {
        return delegate.get(index);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }
}
