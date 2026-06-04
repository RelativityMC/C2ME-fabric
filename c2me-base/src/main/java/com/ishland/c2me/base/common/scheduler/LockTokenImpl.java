/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
