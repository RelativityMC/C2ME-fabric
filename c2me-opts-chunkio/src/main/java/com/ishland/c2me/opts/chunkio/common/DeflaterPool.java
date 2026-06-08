package com.ishland.c2me.opts.chunkio.common;

import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Reuses a per-thread {@link Deflater} for region file chunk writes.
 *
 * Vanilla wraps every chunk write in a {@code new DeflaterOutputStream(out)}, which allocates a fresh
 * {@link Deflater} (a native zlib stream init) and frees it on close (native end). Under sustained chunk
 * saving this shows up as Deflater.init and Deflater.end self-time on the IO threads.
 *
 * A {@link Deflater} created with the no-arg constructor matches vanilla's compression level and zlib
 * wrapping. {@link Deflater#reset()} clears stream state while keeping those settings, so a reused,
 * reset deflater produces byte-identical output to a freshly allocated one. Saved region files stay
 * readable by vanilla and other tools.
 *
 * The deflater is held in a {@link ThreadLocal} because region IO runs across several worker threads, and
 * each write on a given thread runs to completion before the next, so no locking is needed. When a
 * {@link Deflater} is passed to {@link DeflaterOutputStream#DeflaterOutputStream(OutputStream, Deflater)},
 * the stream's close() finishes but does not end() it, so the same instance survives for the next write.
 */
public final class DeflaterPool {

    private static final ThreadLocal<Deflater> DEFLATER = ThreadLocal.withInitial(Deflater::new);

    private DeflaterPool() {
    }

    /**
     * Wraps the given stream with a {@link DeflaterOutputStream} backed by the calling thread's reused
     * deflater. The deflater is reset before use, so the produced bytes match a fresh deflater exactly.
     * The default 512 byte internal buffer matches vanilla's DeflaterOutputStream, keeping flush
     * boundaries and therefore output identical.
     */
    public static DeflaterOutputStream wrap(OutputStream out) {
        final Deflater deflater = DEFLATER.get();
        deflater.reset();
        return new DeflaterOutputStream(out, deflater);
    }

}
