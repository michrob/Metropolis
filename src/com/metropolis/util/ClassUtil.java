package com.metropolis.util;


import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ClassUtil {

    public static Map<String, MethodHandle> getConsumers(final Class consumersClass, final Class parameterType) {
        final Map<String, MethodHandle> declaredMethods = new HashMap<>();

        for (final Method method : consumersClass.getDeclaredMethods()) {
            final String methodName = method.getName();
            if (methodName.contains("lambda")) {
                continue;
            }
            if (method.getParameterCount() == 2 &&
                method.getParameterTypes()[0].equals(parameterType)) {
                try {

                    final MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
                    declaredMethods.put(consumersClass.getSimpleName() + "::" + methodName, methodHandle);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return declaredMethods;
    }


}
