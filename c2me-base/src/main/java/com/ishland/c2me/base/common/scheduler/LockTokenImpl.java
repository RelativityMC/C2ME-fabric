package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.LockToken;

public record LockTokenImpl(int ownerTag, long pos) implements LockToken {
}
