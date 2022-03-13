package com.ishland.c2me.base.common.util;

import com.google.common.base.Preconditions;
import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

public record AsyncNamedLockDelegateAsyncLock<T>(AsyncNamedLock<T> delegate,
                                                 T name) implements AsyncLock {

    public AsyncNamedLockDelegateAsyncLock {
        Preconditions.checkNotNull(this.delegate());
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
