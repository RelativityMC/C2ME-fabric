package com.ishland.c2me.base.common.scheduler;

import com.ishland.flowsched.executor.LockToken;

import java.util.Objects;

public final class LockTokenImpl implements LockToken {
    private final int ownerTag;
    private final long pos;
    private final Usage usage;

    public LockTokenImpl(int ownerTag, long pos, Usage usage) {
        this.ownerTag = ownerTag;
        this.pos = pos;
        this.usage = usage;
    }

    public int ownerTag() {
        return ownerTag;
    }

    public long pos() {
        return pos;
    }

    public Usage usage() {
        return usage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LockTokenImpl) obj;
        return this.ownerTag == that.ownerTag &&
               this.pos == that.pos &&
               Objects.equals(this.usage, that.usage);
    }

    @Override
    public int hashCode() {
        // inlined Objects.hash(ownerTag, usage, pos)
        int result = 1;

        result = 31 * result + Integer.hashCode(ownerTag);
        result = 31 * result + usage.hashCode();
        result = 31 * result + Long.hashCode(pos);

        return result;
    }

    @Override
    public String toString() {
        return "LockTokenImpl[" +
               "ownerTag=" + ownerTag + ", " +
               "pos=" + pos + ", " +
               "usage=" + usage + ']';
    }


    public enum Usage {
        WORLDGEN,
        LIGHTING,
    }

}
