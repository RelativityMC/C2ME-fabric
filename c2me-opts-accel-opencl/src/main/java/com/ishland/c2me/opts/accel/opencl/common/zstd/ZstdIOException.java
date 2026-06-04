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

package com.ishland.c2me.opts.accel.opencl.common.zstd;

import org.lwjgl.util.zstd.Zstd;

import java.io.IOException;

public class ZstdIOException extends IOException {
    private long code;

    /**
     * Construct a ZstdException from the result of a Zstd library call.
     *
     * The error code and message are automatically looked up using
     * Zstd.getErrorCode and Zstd.getErrorName.
     *
     * @param result the return value of a Zstd library call
     */
    public ZstdIOException(long result) {
        this(Zstd.ZSTD_getErrorCode(result), Zstd.ZSTD_getErrorName(result));
    }

    /**
     * Construct a ZstdException with a manually-specified error code and message.
     *
     * No transformation of either the code or message is done. It is advised
     * that one of the Zstd.err*() is used to obtain a stable error code.
     *
     * @param code a Zstd error code
     * @param message the exception's message
     */
    public ZstdIOException(long code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Get the Zstd error code that caused the exception.
     *
     * This will likely correspond to one of the Zstd.err*() methods, but the
     * Zstd library may return error codes that are not yet stable. In such
     * cases, this method will return the code reported by Zstd, but it will
     * not correspond to any of the Zstd.err*() methods.
     *
     * @return a Zstd error code
     */
    public long getErrorCode() {
        return code;
    }
}