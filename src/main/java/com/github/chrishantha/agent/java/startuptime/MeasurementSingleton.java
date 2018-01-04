package com.github.chrishantha.agent.java.startuptime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class MeasurementSingleton {

    private static final MeasurementSingleton INSTANCE = new MeasurementSingleton();

    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    private final long VM_START_TIME = runtimeMXBean.getStartTime();

    private long agentStartNanoTime;

    private long agentStartupTime;

    private long agentEntryUptime;

    private long agentDuration;

    private long classTransformationDuration;

    private long serverStartNanoTime;

    private long serverStartupDuration;

    private long serverStartupTime;

    private long serverStartUptime;

    private MeasurementSingleton() {
        System.out.println(runtimeMXBean.getUptime());
        System.out.println(System.currentTimeMillis() - VM_START_TIME);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Path p = Paths.get("log.csv");
            File file = p.toFile();
            if (!file.exists()) {
                try (BufferedWriter writer = Files.newBufferedWriter(p)) {
                    writer.write(getHeader());
                    writer.newLine();
                    writer.write(getValues());
                    writer.newLine();
                } catch (IOException e) {
                    System.err.format("IOException: %s%n", e);
                }
            } else {
                try (BufferedWriter writer = Files.newBufferedWriter(p,
                        StandardOpenOption.APPEND)) {
                    writer.write(getValues());
                    writer.newLine();
                } catch (IOException e) {
                    System.err.format("IOException: %s%n", e);
                }
            }
        }));
    }

    private String getHeader() {
        return String.join(",", "Agent Startup Time (ms)", "Agent Entry Uptime (ms)",
                "Agent Duration (ms)", "Class Transformation Duration (ms)", "Server Startup Time (ms)",
                "Server Start Uptime (ms)", "Server Startup Duration (ms)");
    }

    private String getValues() {
        Long values[] = {agentStartupTime, agentEntryUptime, agentDuration, classTransformationDuration,
                serverStartupTime, serverStartUptime, serverStartupDuration};
        return Arrays.stream(values).map(i -> String.valueOf(i)).collect(Collectors.joining(","));
    }

    public static void main(String[] args) {
        System.out.println(MeasurementSingleton.getInstance().getValues());
    }

    public static MeasurementSingleton getInstance() {
        return INSTANCE;
    }

    public void premainEnter() {
        agentStartNanoTime = System.nanoTime();
        agentEntryUptime = runtimeMXBean.getUptime();
        agentStartupTime = System.currentTimeMillis() - VM_START_TIME;
    }

    public void premainExit() {
        agentDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - agentStartNanoTime);
        serverStartNanoTime = System.nanoTime();
    }

    public void serverStarted() {
        serverStartupDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - serverStartNanoTime)
                - classTransformationDuration;
        serverStartupTime = System.currentTimeMillis() - VM_START_TIME;
        serverStartUptime = runtimeMXBean.getUptime();
    }

    public void setClassTransformationDuration(long classTransformationDuration) {
        this.classTransformationDuration = classTransformationDuration;
    }
}