package com.ishland.c2me.base.common.logging;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Custom C2ME logger that writes to a dedicated log file
 */
public class C2MELogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("C2ME Logger");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static C2MELogger INSTANCE;
    private final Path logFile;
    private final PrintWriter writer;
    private final ExecutorService logExecutor;
    private final ConcurrentLinkedQueue<String> logQueue;

    private C2MELogger() throws IOException {
        Path logDir = FabricLoader.getInstance().getConfigDir().resolve("c2me");
        Files.createDirectories(logDir);

        this.logFile = logDir.resolve("c2me.log");
        this.writer = new PrintWriter(Files.newBufferedWriter(logFile,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND), true);

        this.logQueue = new ConcurrentLinkedQueue<>();
        this.logExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "C2ME-Logger");
            t.setDaemon(true);
            return t;
        });

        // Start log processing thread
        logExecutor.submit(this::processLogQueue);

        LOGGER.info("C2ME logger initialized, logging to: {}", logFile);
    }

    public static C2MELogger getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = new C2MELogger();
            } catch (IOException e) {
                LOGGER.error("Failed to initialize C2ME logger", e);
                throw new RuntimeException("Failed to initialize C2ME logger", e);
            }
        }
        return INSTANCE;
    }

    public void log(String level, String loggerName, String message) {
        logQueue.offer(String.format("[%s] [%s] [%s]: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                Thread.currentThread().getName(),
                loggerName,
                message));
    }

    public void log(String level, String loggerName, String message, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] [%s] [%s]: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                Thread.currentThread().getName(),
                loggerName,
                message));

        if (throwable != null) {
            sb.append("\n").append(throwable.toString());
            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("\n    at ").append(element.toString());
            }
        }

        logQueue.offer(sb.toString());
    }

    private void processLogQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String logEntry = logQueue.poll();
                if (logEntry != null) {
                    writer.println(logEntry);
                    writer.flush();
                } else {
                    // Sleep a bit if queue is empty
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error writing to C2ME log file: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        logExecutor.shutdown();
        if (writer != null) {
            writer.close();
        }
    }

    // Static convenience methods
    public static void info(String loggerName, String message) {
        getInstance().log("INFO", loggerName, message);
    }

    public static void warn(String loggerName, String message) {
        getInstance().log("WARN", loggerName, message);
    }

    public static void error(String loggerName, String message) {
        getInstance().log("ERROR", loggerName, message);
    }

    public static void error(String loggerName, String message, Throwable throwable) {
        getInstance().log("ERROR", loggerName, message, throwable);
    }

    public static void debug(String loggerName, String message) {
        getInstance().log("DEBUG", loggerName, message);
    }
}