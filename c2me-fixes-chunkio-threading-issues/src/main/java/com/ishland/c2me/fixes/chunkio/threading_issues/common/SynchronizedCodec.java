package com.ishland.c2me.fixes.chunkio.threading_issues.common;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedCodec<A> implements Codec<A> {

    private final ReentrantLock lock = new ReentrantLock(false);
    private final Codec<A> delegate;

    public SynchronizedCodec(Codec<A> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
        try {
            lock.lockInterruptibly();
            return this.delegate.decode(ops, input);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    @Override
    public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
        try {
            lock.lockInterruptibly();
            return this.delegate.encode(input, ops, prefix);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    class ManagedLocker implements ForkJoinPool.ManagedBlocker {
        boolean hasLock = false;
        public boolean block() {
            if (!hasLock)
                lock.lock();
            return true;
        }
        public boolean isReleasable() {
            return hasLock || (hasLock = lock.tryLock());
        }
    }
}
