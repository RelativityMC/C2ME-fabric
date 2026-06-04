/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl.common.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

public class CLEventList extends LongArrayList {

    public PointerBuffer getEventWaitList(MemoryStack stack) {
        int size = this.size();
        PointerBuffer buffer = stack.mallocPointer(size);
        for (int i = 0; i < size; ++i) {
            buffer.put(i, this.getLong(i));
        }
        return buffer.rewind();
    }

}
