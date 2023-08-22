package io.littlehorse.test.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtil {

    public static <T extends Annotation> List<T> findAnnotatedMethods(Class<?> clazz, Class<T> annotationClass) {
        List<T> annotations = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                T annotation = method.getAnnotation(annotationClass);
                annotations.add(annotation);
            }
        }
        return annotations;
    }
}
