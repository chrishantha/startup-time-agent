package com.github.chrishantha.agent.java.startuptime;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.SocketAddress;

public class Agent {

    public static void premain(String arg, Instrumentation instrumentation) {
        long mainStartTime = System.currentTimeMillis();
        long vmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        long mainUptime = ManagementFactory.getRuntimeMXBean().getUptime();
        System.out.println(String.format("Agent - VM Start Time: %d", vmStartTime));
        System.out.println(String.format("Agent - Main Start Time: %d", mainStartTime));
        System.out.println(String.format("Agent - Main Uptime (ms): %d", mainUptime));
        System.out.println(String.format("Agent - VM Startup Time (ms): %d", mainStartTime - vmStartTime));
        new AgentBuilder.Default()
//                .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
//                .type(ElementMatchers.nameEndsWith("Address"))
//                .type(ElementMatchers.nameStartsWith("java.net"))
                .type(ElementMatchers.named("io.netty.bootstrap.AbstractBootstrap"))
                .transform((builder, typeDescription, classLoader, javaModule) ->
                        builder.method(ElementMatchers.named("bind").and(ElementMatchers.takesArguments(SocketAddress.class)))
//                        builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(Interceptor.class) //.andThen(SuperMethodCall.INSTANCE)
                                ))
                .installOn(instrumentation);
    }
}
