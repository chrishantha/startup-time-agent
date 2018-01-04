package com.github.chrishantha.agent.java.startuptime;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.TimeUnit;

public class Agent {

    private static final String NETTY_CLASS = "io/netty/bootstrap/AbstractBootstrap";

    public static void premain(String agentArgs, Instrumentation inst) {
        MeasurementSingleton.getInstance().premainEnter();
//        long mainStartTime = System.currentTimeMillis();
//        long vmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
//        long mainUptime = ManagementFactory.getRuntimeMXBean().getUptime();
//        System.out.println(String.format("Agent - VM Start Time: %d", vmStartTime));
//        System.out.println(String.format("Agent - Main Start Time: %d", mainStartTime));
//        System.out.println(String.format("Agent - Main Uptime (ms): %d", mainUptime));
//        System.out.println(String.format("Agent - VM Startup Time (ms): %d", mainStartTime - vmStartTime));
//        new AgentBuilder.Default()
////                .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
////                .type(ElementMatchers.nameEndsWith("Address"))
////                .type(ElementMatchers.nameStartsWith("java.net"))
//                .disableClassFormatChanges()
////                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
//                .type(ElementMatchers.named("io.netty.bootstrap.AbstractBootstrap"))
////                .transform((builder, typeDescription, classLoader, javaModule) ->
////                        builder.method(ElementMatchers.named("bind").and(ElementMatchers.takesArguments(SocketAddress.class)))
////                                .intercept(MethodDelegation.to(Interceptor.class) //.andThen(SuperMethodCall.INSTANCE)
////                                ))
//
//                .transform((builder, typeDescription, classLoader, javaModule) ->
//                        builder.visit(Advice.to(TimeAdvice.class)
//                                .on(ElementMatchers.named("bind").and(ElementMatchers.takesArguments(SocketAddress.class)))))
//                .installOn(inst);


//        TypePool typePool = TypePool.Default.ofClassPath();
//
//        new ByteBuddy().rebase(typePool.describe("io.netty.bootstrap.AbstractBootstrap").resolve(),
//                ClassFileLocator.ForClassLoader.ofClassPath())
//                .method(ElementMatchers.named("bind").and(ElementMatchers.takesArguments(SocketAddress.class)))
//                .intercept(MethodDelegation.to(Interceptor.class)).make().load(ClassLoader.getSystemClassLoader());


//        LongAdder totalTime = new LongAdder();

        inst.addTransformer((classLoader, s, aClass, protectionDomain, bytes) -> {
            long startTime = System.nanoTime();
            try {
                if (NETTY_CLASS.equals(s)) {
                    long start = System.nanoTime();
//                    // Javassist
                    try {
                        ClassPool cp = ClassPool.getDefault();
                        CtClass cc = cp.get("io.netty.bootstrap.AbstractBootstrap");
                        CtMethod m = cc.getDeclaredMethod("bind", new CtClass[]{cp.get("java.net.SocketAddress")});
//                        CtMethod m = cc.getDeclaredMethod("bind");
//                    m.addLocalVariable("elapsedTime", CtClass.longType);
//                    m.insertBefore("elapsedTime = System.currentTimeMillis();");
//                    m.insertAfter("{elapsedTime = System.currentTimeMillis() - elapsedTime;"
//                            + "System.out.println(\"Method Executed in ms: \" + elapsedTime);}");

//                        m.insertAfter("{ System.out.println(\"Server started. Current Uptime (ms): \" + " +
//                                "java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime());}");

                        m.insertAfter("{com.github.chrishantha.agent.java.startuptime." +
                                "MeasurementSingleton.getInstance().serverStarted();}");
                        byte[] byteCode = cc.toBytecode();
                        cc.detach();
                        return byteCode;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        MeasurementSingleton.getInstance()
                                .setClassTransformationDuration(TimeUnit.NANOSECONDS
                                        .toMillis(System.nanoTime() - start));
//                        System.out.println(String.format("Agent - Transformation Time (ms): %d", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)));
                    }
                }
                return null;
            } finally {
//                totalTime.add(System.nanoTime() - startTime);
            }
        });
//
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            long sum = totalTime.sum();
//            System.out.println(String.format("Total Transformation Time: %d ms (%d ns)", TimeUnit.NANOSECONDS.toMillis(sum), sum));
//        }));
//
//        System.out.println(String.format("Agent - Install Time (ms): %d", System.currentTimeMillis() - mainStartTime));
//
//        System.out.println(String.format("Agent - End Uptime (ms): %d", ManagementFactory.getRuntimeMXBean().getUptime()));
        MeasurementSingleton.getInstance().premainExit();
    }
}
