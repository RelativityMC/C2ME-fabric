package com.ishland.c2me.gradle;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RedirectingOutputStream extends OutputStream {

    private final ProjectInternal project;
    private final ProgressLogger progressLogger;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicLong lastError = new AtomicLong();

    public RedirectingOutputStream(Project project, String description) {
        this.project = (ProjectInternal) project;
        progressLogger = this.project.getServices().get(ProgressLoggerFactory.class).newOperation(getClass());
        progressLogger.setDescription(description);
        progressLogger.started();
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (buffer) {
            if (closed.get()) throw new IOException("Attempted to write to a closed resource");
            if (b == '\n') {
                String s = buffer.toString();
                try {
                    boolean doProgress = true;
                    boolean doPrint = false;
                    if (s.contains("[noprogress]")) {
                        doProgress = false;
                        s = s.replace("[noprogress]", "");
                    }
                    if (s.contains("[print]")) {
                        doPrint = true;
                        s = s.replace("[print]", "");
                    }
                    if (doProgress) {
                        progressLogger.progress(s);
                    }
                    if (doPrint) {
                        project.getLogger().lifecycle(s);
                    }
                    if (s.contains("ERROR")) {
                        lastError.set(System.currentTimeMillis());
                        project.getLogger().error(s);
                        return;
                    }
                    if (s.contains("WARN") || s.contains("[FabricLoader]")) {
                        lastError.set(System.currentTimeMillis());
                        project.getLogger().warn(s);
                        return;
                    }
                    if (System.currentTimeMillis() - lastError.get() < 500) {
                        project.getLogger().lifecycle(s);
                    } else {
                        project.getLogger().info(s);
                    }
                } finally {
                    buffer.reset();
                }
            } else {
                buffer.write(b);
            }
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (closed.compareAndSet(false, true)) {
            synchronized (buffer) {
                progressLogger.completed();
            }
        }
    }
}
