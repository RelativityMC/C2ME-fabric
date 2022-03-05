package com.ishland.c2me.base.common.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CMETrackingMap<K, V> implements Map<K, V> {

    private final Map<K, V> wrapped;

    private volatile Throwable throwable = new Throwable();

    public CMETrackingMap(Map<K, V> wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int size() {
        return this.wrapped.size();
    }

    @Override
    public boolean isEmpty() {
        return this.wrapped.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.wrapped.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.wrapped.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.wrapped.get(key);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        this.throwable = new Throwable();
        return this.wrapped.put(key, value);
    }

    @Override
    public V remove(Object key) {
        this.throwable = new Throwable();
        return this.wrapped.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        this.throwable = new Throwable();
        this.wrapped.putAll(m);
    }

    @Override
    public void clear() {
        this.throwable = new Throwable();
        this.wrapped.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return this.wrapped.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return this.wrapped.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.wrapped.entrySet();
    }
}
