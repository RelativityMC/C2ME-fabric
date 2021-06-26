package com.ishland.c2me.gradle;

import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class RedirectingOutputStream extends OutputStream {

    private final ProjectInternal project;
    private final ProgressLogger progressLogger;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final AtomicBoolean closed = new AtomicBoolean();

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
                final String s = buffer.toString();
                progressLogger.progress(buffer.toString());
                project.getLogger().info(buffer.toString());
                if (s.contains("PreGen completed")) project.getLogger().lifecycle(s);
                buffer.reset();
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
