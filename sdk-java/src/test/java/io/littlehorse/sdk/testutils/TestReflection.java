package io.littlehorse.sdk.testutils;

import io.littlehorse.sdk.worker.LHTaskMethod;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class TestReflection {

    private TestReflection() {}

    public static Method getTaskMethodByName(Class<?> cls, String taskName) {
        for (Method m : cls.getMethods()) {
            LHTaskMethod ann = m.getAnnotation(LHTaskMethod.class);
            if (ann != null && ann.value().equals(taskName)) {
                return m;
            }
        }
        throw new RuntimeException(
                new NoSuchMethodException("No @LHTaskMethod with value='" + taskName + "' found on " + cls.getName()));
    }

    public static Parameter getParameter(Method method, int paramIndex) {
        Parameter param = method.getParameters()[paramIndex];
        return param;
    }
}
