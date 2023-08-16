package io.littlehorse.test;

import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class LHExtension implements BeforeAllCallback, TestInstancePostProcessor {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getStore(context).put("worker", new Object());
        Optional<Class<?>> testClass = context.getTestClass();
        System.out.println("extension");
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create("EXECUTION"));
    }

    @Override
    public void postProcessTestInstance(
        Object testInstance,
        ExtensionContext context
    ) throws Exception {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            boolean annotationPresent = field.isAnnotationPresent(LHTestClient.class);
            if (annotationPresent) {}
        }
    }
}
