package com.github.chrishantha.agent.java.startuptime;


import net.bytebuddy.asm.Advice;

import java.lang.management.ManagementFactory;

public class TimeAdvice {

    @Advice.OnMethodEnter
    static void enter() {
        System.out.println("Binding");
    }
//
//    @Advice.OnMethodExit
//    static void exit(@Advice.Origin String method, @Advice.Enter long start) {
//        System.out.println(String.format("Server started. Current Time (ms): %d", System.currentTimeMillis()));
//        System.out.println(String.format("Server started. Current Uptime (ms): %d",
//                ManagementFactory.getRuntimeMXBean().getUptime()));
//        System.out.println(method + " took " + (System.currentTimeMillis() - start));
//    }


    @Advice.OnMethodExit
    static void exit() {
        System.out.println(String.format("Server started. Current Time (ms): %d", System.currentTimeMillis()));
        System.out.println(String.format("Server started. Current Uptime (ms): %d",
                ManagementFactory.getRuntimeMXBean().getUptime()));
    }
}
