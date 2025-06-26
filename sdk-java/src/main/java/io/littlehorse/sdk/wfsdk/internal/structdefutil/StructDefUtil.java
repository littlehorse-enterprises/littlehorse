package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StructDefUtil {
   public static void dfsFindCycle(Class<?> structClass) {
        Deque<Class<?>> classStack = new ArrayDeque<>();
        classStack.push(structClass);
        detectCycle(classStack, structClass);
    }

    public static void detectCycle(Deque<Class<?>> visitedAncestors, Class<?> structClass) {
        if (structClass.getFields().length == 0) return;

        // Field types within the current class that have already been visited;
        Set<Class<?>> visitedSiblings = new HashSet<>();

        for (Field field : structClass.getFields()) {
            Class<?> fieldType;
            if (field.getType().isArray()) {
                fieldType = field.getType().componentType();
            } else {
                fieldType = field.getType();
            } 

            if (fieldType.isPrimitive()) continue;
            if (visitedSiblings.contains(fieldType)) continue;

            visitedSiblings.add(fieldType);

            // If we've already visited this fieldType in an ancestor
            if (visitedAncestors.contains(fieldType)) {
                visitedAncestors.push(fieldType);
                throw new CircularDependencyException(buildCircularDependencyExceptionMessage(visitedAncestors));
            } else {
                visitedAncestors.push(fieldType);
                detectCycle(visitedAncestors, fieldType);
                visitedAncestors.pop();
            }
        }
    }

    public static String buildCircularDependencyExceptionMessage(Deque<Class<?>> visited) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Circular dependency found involving class: " + visited.peek().getCanonicalName() + "\n");

        List<Class<?>> classList = visited.stream().collect(Collectors.toList()).reversed();

        stringBuilder.append("\nDependency tree:\n");
        
        for (int i = 0; i < classList.size(); i++) {
            Class<?> visitedClass = classList.get(i);
            for (int j = 0; j < i; j++) {
                stringBuilder.append("  ");
            }
            stringBuilder.append("- ");
            stringBuilder.append(visitedClass.getCanonicalName() + "\n");
        }
        return stringBuilder.toString();
    }
}
