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
