package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.LockToken;

public record LockTokenImpl(int ownerTag, long pos, Usage usage) implements LockToken {

    public enum Usage {
        WORLDGEN,
        LIGHTING,
    }

}
