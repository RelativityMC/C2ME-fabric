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
