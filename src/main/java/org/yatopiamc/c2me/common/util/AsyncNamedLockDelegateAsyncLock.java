package org.yatopiamc.c2me.common.util;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class AsyncNamedLockDelegateAsyncLock<T> implements AsyncLock {

    private final AsyncNamedLock<T> delegate;
    private final T name;

    public AsyncNamedLockDelegateAsyncLock(AsyncNamedLock<T> delegate, T name) {
        this.delegate = Objects.requireNonNull(delegate);
        this.name = name;
    }

    @Override
    public CompletionStage<LockToken> acquireLock() {
        return delegate.acquireLock(name);
    }

    @Override
    public Optional<LockToken> tryLock() {
        return delegate.tryLock(name);
    }
}
