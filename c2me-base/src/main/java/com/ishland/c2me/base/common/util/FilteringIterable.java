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
