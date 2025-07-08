package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.StructDefCircularDependencyException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StructDefUtil {

    /**
     * Builds an InlineStructDef based on a given Java class
     * @param structClass The Java class you want to convert to an InlineStructDef
     * @return an InlineStructDef
     */
    public static InlineStructDef buildInlineStructDef(Class<?> structClass) {
        InlineStructDef.Builder inlineStructDef = InlineStructDef.newBuilder();

        Field[] fields = structClass.getFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(LHStructIgnore.class)) {
                continue;
            }

            StructFieldDef.Builder fieldDef = StructFieldDef.newBuilder();

            // Assemble Field's TypeDef
            TypeDefinition.Builder typeDef = TypeDefinition.newBuilder();
            String fieldName = field.getName();

            if (field.getType().isAnnotationPresent(LHStructDef.class)) {
                LHStructDef lhStructDefAnnotation = field.getType().getAnnotation(LHStructDef.class);
                typeDef.setStructDefId(StructDefId.newBuilder().setName(lhStructDefAnnotation.name()));
            } else {
                VariableType fieldPrimitiveType = LHLibUtil.javaClassToLHVarType(field.getType());
                typeDef.setPrimitiveType(fieldPrimitiveType);
            }

            if (field.isAnnotationPresent(LHStructField.class)) {
                LHStructField fieldAnnotation = field.getAnnotation(LHStructField.class);

                if (!fieldAnnotation.name().isBlank()) {
                    fieldName = fieldAnnotation.name();
                }

                typeDef.setMasked(fieldAnnotation.masked());
            }

            fieldDef.setFieldType(typeDef);

            // TODO: Process default value, if possible

            inlineStructDef.putFields(fieldName, fieldDef.build());
        }

        return inlineStructDef.build();
    }

    /**
     * Finds the dependencies of a given StructDef class based on its field types and
     * returns them sorted topologically.
     *
     * @param structClass A given StructDef class you want to find the other StructDef dependencies of
     * @return A list of classes depended on by `structClass`, sorted topologically
     */
    public static List<Class<?>> getStructDefDependencies(Class<?> structClass) {
        if (!structClass.isAnnotationPresent(LHStructDef.class)) {
            throw new IllegalArgumentException(
                    "Missing `@LHStructDef` annotation on class: " + structClass.getCanonicalName());
        }

        Set<Class<?>> visited = new HashSet<>();
        List<Class<?>> sortedList = new ArrayList<>();
        Set<Class<?>> tempMarked = new HashSet<>();

        detectCycle(structClass, visited, sortedList, tempMarked, new ArrayList<>());

        return sortedList;
    }

    private static void detectCycle(
            Class<?> clazz,
            Set<Class<?>> visited,
            List<Class<?>> sortedList,
            Set<Class<?>> tempMarked,
            List<Class<?>> currentPath) {

        // If we've already visited this locally in a sibling field
        if (visited.contains(clazz)) {
            return;
        }

        currentPath.add(clazz);

        // If we've already visited this class in an ancestor...
        if (tempMarked.contains(clazz)) {
            throw new StructDefCircularDependencyException(buildCircularDependencyExceptionMessage(currentPath));
        }

        tempMarked.add(clazz);

        // Get fields of the class
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isSynthetic()) continue;

            Class<?> fieldType = getFieldType(field);

            // Check if the field type is a non-primitive class
            if (!isLHPrimitive(fieldType)) {
                if (!fieldType.isAnnotationPresent(LHStructDef.class)) {
                    throw new IllegalArgumentException(
                            "Missing @LHStructDef annotation on non-primitive class used in an LHStructDef: "
                                    + fieldType.getName());
                }
                detectCycle(fieldType, visited, sortedList, tempMarked, currentPath);
            }
        }

        // Add the class to the result in topologically sorted order
        currentPath.remove(currentPath.size() - 1); // Remove from current path
        tempMarked.remove(clazz);
        visited.add(clazz);
        sortedList.add(clazz);
    }

    private static Class<?> getFieldType(Field field) {
        if (field.getType().isArray()) {
            return field.getType().getComponentType();
        }
        return field.getType();
    }

    private static boolean isLHPrimitive(Class<?> clazz) {
        if (clazz.isPrimitive()) return true;
        if (clazz.equals(Byte.class)) return true;
        if (clazz.equals(Short.class)) return true;
        if (clazz.equals(Integer.class)) return true;
        if (clazz.equals(Boolean.class)) return true;
        if (clazz.equals(Long.class)) return true;
        if (clazz.equals(Float.class)) return true;
        if (clazz.equals(Double.class)) return true;
        if (clazz.equals(String.class)) return true;

        return false;
    }

    private static String buildCircularDependencyExceptionMessage(List<Class<?>> classList) {
        if (classList.isEmpty()) {
            throw new IllegalStateException(
                    "Tried to throw Circular Dependency exception but no classes found in class tree.");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Circular dependency found involving class: "
                + classList.get(classList.size() - 1).getCanonicalName() + "\n");

        stringBuilder.append("\nDependency tree:\n");

        for (int i = 0; i < classList.size(); i++) {
            Class<?> visitedClass = classList.get(i);
            stringBuilder
                    .append("  ".repeat(i))
                    .append("- ")
                    .append(visitedClass.getCanonicalName())
                    .append("\n");
        }
        return stringBuilder.toString();
    }
}
