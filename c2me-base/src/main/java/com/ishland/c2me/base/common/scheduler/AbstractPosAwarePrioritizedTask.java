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

import com.ishland.flowsched.executor.Task;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.Objects;

public abstract class AbstractPosAwarePrioritizedTask extends Task {

    protected final ReferenceArrayList<Runnable> postExec = new ReferenceArrayList<>(4);
    private final long pos;

    public AbstractPosAwarePrioritizedTask(long pos) {
        this.pos = pos;
    }

    public long getPos() {
        return this.pos;
    }

    public void addPostExec(Runnable runnable) {
        synchronized (this.postExec) {
            postExec.add(Objects.requireNonNull(runnable));
        }
    }
}
