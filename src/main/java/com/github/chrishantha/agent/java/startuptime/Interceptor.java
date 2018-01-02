package com.github.chrishantha.agent.java.startuptime;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class Interceptor {

    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
        try {
            return callable.call();
        } finally {
            System.out.println(String.format("Server started. Current Time (ms): %d", System.currentTimeMillis()));
            System.out.println(String.format("Server started. Current Uptime (ms): %d",
                    ManagementFactory.getRuntimeMXBean().getUptime()));
        }
    }
}