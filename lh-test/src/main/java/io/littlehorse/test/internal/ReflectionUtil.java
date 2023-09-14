package io.littlehorse.test.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

    public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotationClass)) {
                fields.add(field);
            }
        }
        return fields;
    }
}
