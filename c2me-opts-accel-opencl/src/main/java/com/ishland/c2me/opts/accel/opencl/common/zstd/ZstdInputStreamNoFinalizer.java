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

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.zstd.ZSTDInBuffer;
import org.lwjgl.util.zstd.ZSTDOutBuffer;
import org.lwjgl.util.zstd.Zstd;
import org.lwjgl.util.zstd.ZstdErrors;

import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.lang.IndexOutOfBoundsException;
import java.nio.ByteBuffer;

/**
 * InputStream filter that decompresses the data provided
 * by the underlying InputStream using Zstd compression.
 *
 * It does not support mark/reset methods. It also does not have finalizer,
 * so if you rely on finalizers to clean the native memory and release
 * buffers use `ZstdInputStream` instead.
 */

public class ZstdInputStreamNoFinalizer extends FilterInputStream {

    // Opaque pointer to Zstd context object
    private final long stream;
    private long srcPos = 0;
    private long srcSize = 0;
    private boolean needRead = true;
    private final ByteBuffer srcByteBuffer;
    private final byte[] srcTempArray;
    private static final int srcBuffSize = (int) Zstd.ZSTD_DStreamInSize();

    private final ZSTDInBuffer zstdInBuffer;
    private final ZSTDOutBuffer zstdOutBuffer;

    private boolean isContinuous = false;
    private boolean frameFinished = true;
    private boolean isClosed = false;

    /**
     * create a new decompressing InputStream
     * @param inStream the stream to wrap
     */
    public ZstdInputStreamNoFinalizer(InputStream inStream) throws IOException {
        super(inStream);
        this.srcByteBuffer = MemoryUtil.memAlloc(srcBuffSize);
        this.srcTempArray = new byte[srcBuffSize];
        // memory barrier
        synchronized (this) {
            this.stream = Zstd.ZSTD_createDStream();
            ZstdUtil.checkZstdError(Zstd.ZSTD_DCtx_reset(this.stream, Zstd.ZSTD_reset_session_only));
            ZstdUtil.checkZstdError(Zstd.ZSTD_DCtx_loadDictionary(this.stream, null));
            this.zstdInBuffer = ZSTDInBuffer.malloc();
            this.zstdOutBuffer = ZSTDOutBuffer.malloc();
        }
    }

    /**
     * Don't break on unfinished frames
     *
     * Use case: decompressing files that are not yet finished writing and compressing
     */
    public synchronized ZstdInputStreamNoFinalizer setContinuous(boolean b) {
        isContinuous = b;
        return this;
    }

    public synchronized boolean getContinuous() {
        return this.isContinuous;
    }

//    public synchronized ZstdInputStreamNoFinalizer setDict(byte[] dict) throws IOException {
//        int size = Zstd.ZSTD_loadDictDecompress(stream, dict, dict.length);
//        if (Zstd.isError(size)) {
//            throw new ZstdIOException(size);
//        }
//        return this;
//    }
//
//    public synchronized ZstdInputStreamNoFinalizer setDict(ZstdDictDecompress dict) throws IOException {
//        dict.acquireSharedLock();
//        try {
//            int size = Zstd.loadFastDictDecompress(stream, dict);
//            if (Zstd.isError(size)) {
//                throw new ZstdIOException(size);
//            }
//        } finally {
//            dict.releaseSharedLock();
//        }
//        return this;
//    }

    public synchronized ZstdInputStreamNoFinalizer setLongMax(int windowLogMax) throws IOException {
        ZstdUtil.checkZstdError(Zstd.ZSTD_DCtx_setParameter(this.stream, Zstd.ZSTD_d_windowLogMax, windowLogMax));
        return this;
    }

//    public synchronized ZstdInputStreamNoFinalizer setRefMultipleDDicts(boolean useMultiple) throws IOException {
//        int size = Zstd.setRefMultipleDDicts(stream, useMultiple);
//        if (Zstd.isError(size)) {
//            throw new ZstdIOException(size);
//        }
//        return this;
//    }

    public synchronized int read(byte[] dst, int offset, int len) throws IOException {
        // guard agains buffer overflows
        if (offset < 0 || len > dst.length - offset) {
            throw new IndexOutOfBoundsException("Requested length " + len
                    + " from offset " + offset + " in buffer of size " + dst.length);
        }
        if (len == 0) {
            return 0;
        } else {
            int result = 0;
            while (result == 0) {
                result = readInternal(dst, offset, len);
            }
            return result;
        }
    }

    int readInternal(byte[] dst, int offset, int len) throws IOException {

        if (isClosed) {
            throw new IOException("Stream closed");
        }

        // guard against buffer overflows
        if (offset < 0 || len > dst.length - offset) {
            throw new IndexOutOfBoundsException("Requested length " + len
                    + " from offset " + offset + " in buffer of size " + dst.length);
        }
        int dstSize = offset + len;
        long dstPos = offset;
        long lastDstPos = -1;

        while (dstPos < dstSize && lastDstPos < dstPos) {
            // we will read only if data from the upstream is available OR
            // we have not yet produced any output
            if (needRead && (in.available() > 0 || dstPos == offset)) {
                srcSize = in.read(srcTempArray, 0, srcBuffSize);
                srcPos = 0;
                if (srcSize < 0) {
                    srcSize = 0;
                    if (frameFinished) {
                        return -1;
                    } else if (isContinuous) {
                        srcSize = (int)(dstPos - offset);
                        if (srcSize > 0) {
                            return (int) srcSize;
                        }
                        return -1;
                    } else {
                        throw new ZstdIOException(ZstdErrors.ZSTD_error_corruption_detected, "Truncated source");
                    }
                } else if (srcSize == 0) {
                    continue;
                } else {
                    frameFinished = false;
                    srcByteBuffer.put(0, srcTempArray, 0, (int) srcSize).rewind();
                }
            }

            lastDstPos = dstPos;

            int size;

            zstdInBuffer.src(srcByteBuffer.slice(0, (int) srcSize)).pos(srcPos);
            ByteBuffer dstByteBuffer = MemoryUtil.memAlloc((int) (len - dstPos));
            try {
                zstdOutBuffer.dst(dstByteBuffer).pos(0);
//                System.out.print(String.format("decompressStream(stream, size=%d, size=%d): ", srcSize, (int) (len - dstPos)));
                size = (int) ZstdUtil.checkZstdError(Zstd.ZSTD_decompressStream(stream, zstdOutBuffer, zstdInBuffer));
//                System.out.println(String.format("%d", size));
                dstByteBuffer.get(dst, (int) dstPos, (int) zstdOutBuffer.pos());
            } finally {
                MemoryUtil.memFree(dstByteBuffer);
            }
            srcPos = zstdInBuffer.pos();
            dstPos += zstdOutBuffer.pos();

            // we have completed a frame
            if (size == 0) {
                frameFinished = true;
                // we need to read from the upstream only if we have not consumed
                // fully the source buffer
                needRead = srcPos == srcSize;
                return (int)(dstPos - offset);
            } else {
                // size > 0, so more input is required but there is data left in
                // the decompressor buffers if we have not filled the dst buffer
                needRead = dstPos < dstSize;
            }
        }
        return (int)(dstPos - offset);
    }

    public synchronized int read() throws IOException {
        byte[] oneByte = new byte[1];
        int result = 0;
        while (result == 0) {
            result = readInternal(oneByte, 0, 1);
        }
        if (result == 1) {
            return oneByte[0] & 0xff;
        } else {
            return -1;
        }
    }

    public synchronized int available() throws IOException {
        if (isClosed) {
            throw new IOException("Stream closed");
        }
        if (!needRead) {
            return 1;
        } else {
            return in.available();
        }
    }

    /* we don't support mark/reset */
    public boolean markSupported() {
        return false;
    }

    /* we can skip forward */
    public synchronized long skip(long numBytes) throws IOException {
        if (isClosed) {
            throw new IOException("Stream closed");
        }
        if (numBytes <= 0) {
            return 0;
        }
        int bufferLen = (int) Zstd.ZSTD_DStreamOutSize();
        if (bufferLen > numBytes) {
            bufferLen = (int) numBytes;
        }
//        ByteBuffer buf = Zstd.getArrayBackedBuffer(bufferPool, bufferLen);
        byte[] buf = new byte[bufferLen];
        long toSkip = numBytes;
        try {
            byte[] data = buf;
            while (toSkip > 0) {
                int read = read(data, 0, (int) Math.min((long) bufferLen, toSkip));
                if (read < 0) {
                    break;
                }
                toSkip -= read;
            }
        } finally {
//            bufferPool.release(buf);
        }
        return numBytes - toSkip;
    }

    public synchronized void close() throws IOException {
        if (isClosed) {
            return;
        }
        isClosed = true;
//        bufferPool.release(srcByteBuffer);
        MemoryUtil.memFree(this.srcByteBuffer);
        Zstd.ZSTD_freeDStream(this.stream);
        zstdInBuffer.close();
        zstdOutBuffer.close();
        in.close();
    }
}