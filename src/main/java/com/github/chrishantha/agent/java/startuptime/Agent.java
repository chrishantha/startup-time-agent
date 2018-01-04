package com.github.chrishantha.agent.java.startuptime;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.TimeUnit;

public class Agent {

    private static final String CODE_STRING = "{com.github.chrishantha.agent.java.startuptime." +
            "MeasurementSingleton.getInstance().serverStarted();}";

    private static final String NETTY_CLASS_STRING = "io/netty/bootstrap/AbstractBootstrap";

    private static final String SPRING_TOMCAT_CLASS_STRING = "org/springframework/boot/context/embedded/tomcat/TomcatEmbeddedServletContainer";

    private enum ServerType {
        NETTY,
        SPRING_BOOT
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        ServerType serverType = ServerType.NETTY;
        if (agentArgs != null) {
            serverType = ServerType.valueOf(agentArgs.toUpperCase());
        }

        final String CLASS_STRING;

        switch (serverType) {
            case NETTY:
                CLASS_STRING = NETTY_CLASS_STRING;
                break;
            case SPRING_BOOT:
                CLASS_STRING = SPRING_TOMCAT_CLASS_STRING;
                break;
            default:
                CLASS_STRING = NETTY_CLASS_STRING;
        }


        MeasurementSingleton.getInstance().premainEnter();
        // Javassist
        ServerType finalServerType = serverType;
        inst.addTransformer((classLoader, s, aClass, protectionDomain, bytes) -> {
            if (CLASS_STRING.equals(s)) {
                long start = System.nanoTime();
                try {
                    switch (finalServerType) {
                        case NETTY:
                            return getNettyClassByteCode();
                        case SPRING_BOOT:
                            return getSpringTomcatClassByteCode();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    MeasurementSingleton.getInstance()
                            .setClassTransformationDuration(TimeUnit.NANOSECONDS
                                    .toMillis(System.nanoTime() - start));
                }
            }
            return null;
        });
        MeasurementSingleton.getInstance().premainExit();
    }

    private static byte[] getNettyClassByteCode() throws NotFoundException, CannotCompileException, IOException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get("io.netty.bootstrap.AbstractBootstrap");
        CtMethod m = cc.getDeclaredMethod("bind", new CtClass[]{cp.get("java.net.SocketAddress")});
        m.insertAfter(CODE_STRING);
        byte[] byteCode = cc.toBytecode();
        cc.detach();
        return byteCode;
    }

    private static byte[] getSpringTomcatClassByteCode() throws NotFoundException, CannotCompileException, IOException {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get("org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer");
        CtMethod m = cc.getDeclaredMethod("start");
        m.insertAfter(CODE_STRING);
        byte[] byteCode = cc.toBytecode();
        cc.detach();
        return byteCode;
    }
}
