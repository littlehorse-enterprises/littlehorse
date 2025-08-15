package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.StructDefCircularDependencyException;
import io.littlehorse.sdk.common.proto.InlineArrayDef;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.worker.LHStructDef;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LHClassType {
    private Class<?> clazz;
    private List<LHClassType> dependencyClasses;
    private InlineStructDef inlineStructDef;

    public LHClassType(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Argument 'clazz' cannot be null");
        }
        this.clazz = clazz;
    }

    public Class<?> getClassType() {
        return this.clazz;
    }

    private boolean isLHPrimitive() {
        return LHLibUtil.isJavaClassLHPrimitive(this.clazz);
    }

    private boolean isStructDef() {
        return this.clazz.isAnnotationPresent(LHStructDef.class);
    }

    private boolean isArray() {
        return this.clazz.isArray();
    }

    private TypeDefinition.DefinedTypeCase getDefinedTypeCase() {
        if (this.isLHPrimitive()) {
            return TypeDefinition.DefinedTypeCase.PRIMITIVE_TYPE;
        } else if (this.isStructDef()) {
            return TypeDefinition.DefinedTypeCase.STRUCT_DEF_ID;
        } else if (this.isArray()) {
            return TypeDefinition.DefinedTypeCase.INLINE_ARRAY_DEF;
        }
        return TypeDefinition.DefinedTypeCase.DEFINEDTYPE_NOT_SET;
    }

    public TypeDefinition getTypeDefinition() {
        TypeDefinition.Builder typeDef = TypeDefinition.newBuilder();

        switch (this.getDefinedTypeCase()) {
            case PRIMITIVE_TYPE:
                typeDef.setPrimitiveType(LHLibUtil.javaClassToLHVarType(this.clazz));
                break;
            case STRUCT_DEF_ID:
                typeDef.setStructDefId(StructDefId.newBuilder()
                        .setName(this.getStructDefAnnotation().name())
                        .build());
                break;
            case INLINE_ARRAY_DEF:
                Class<?> componentType = this.clazz.getComponentType();
                LHClassType lhClassType = new LHClassType(componentType);
                typeDef.setInlineArrayDef(InlineArrayDef.newBuilder().setElementType(lhClassType.getTypeDefinition()));
                break;
            case DEFINEDTYPE_NOT_SET:
            case INLINE_STRUCT_DEF:
            default:
                break;
        }

        return typeDef.build();
    }

    public LHClassType getCoreType() {
        Class<?> coreType = clazz;

        while (coreType.isArray()) {
            coreType = coreType.getComponentType();
        }

        return new LHClassType(coreType);
    }

    public LHStructDef getStructDefAnnotation() {
        if (!this.isStructDef()) {
            throw new IllegalStateException(
                    "Cannot get StructDef annotation: Missing `@LHStructDef` annotation on class: "
                            + this.clazz.getCanonicalName());
        }

        return this.clazz.getAnnotation(LHStructDef.class);
    }

    public StructDef toStructDef() {
        if (!this.isStructDef()) {
            throw new IllegalStateException("Cannot convert class to StructDef: " + this.clazz);
        }

        LHStructDef annotation = this.getStructDefAnnotation();

        StructDef.Builder structDef = StructDef.newBuilder();
        structDef.setId(StructDefId.newBuilder().setName(annotation.name()));
        structDef.setDescription(annotation.description());
        structDef.setStructDef(this.getInlineStructDef());

        return structDef.build();
    }

    public List<LHClassType> getDependencyClasses() {
        if (this.dependencyClasses == null) {
            this.dependencyClasses = collectDependencyClasses();
        }
        return Collections.unmodifiableList(this.dependencyClasses);
    }

    private List<LHClassType> collectDependencyClasses() {
        if (!this.isStructDef()) {
            throw new IllegalArgumentException(
                    "Cannot collect dependencies: Missing `@LHStructDef` annotation on class: "
                            + this.clazz.getCanonicalName());
        }

        Set<LHClassType> visited = new HashSet<>();
        List<LHClassType> sortedList = new ArrayList<>();
        Set<LHClassType> tempMarked = new HashSet<>();

        detectCycle(visited, sortedList, tempMarked, new ArrayList<>());

        return sortedList;
    }

    private void detectCycle(
            Set<LHClassType> visited,
            List<LHClassType> sortedList,
            Set<LHClassType> tempMarked,
            List<LHClassType> currentPath) {

        // If we've already visited this locally in a sibling field
        if (visited.contains(this)) {
            return;
        }

        currentPath.add(this);

        // If we've already visited this class in an ancestor...
        if (tempMarked.contains(this)) {
            throw new StructDefCircularDependencyException(buildCircularDependencyExceptionMessage(currentPath));
        }

        tempMarked.add(this);

        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if ("class".equals(pd.getName())) continue;
                
                LHStructProperty property = new LHStructProperty(pd);

                if (property.isIgnored()) continue;

                // For detecting cycles, we don't care if the property is an Array or not. We just need the core
                // component type.
                LHClassType propertyClass = property.getPropertyType().getCoreType();

                if (!propertyClass.isLHPrimitive()) {
                    if (!propertyClass.isStructDef()) {
                        throw new IllegalArgumentException(
                                "Missing @LHStructDef annotation on non-primitive class used in an LHStructDef getter or setter: "
                                        + propertyClass.getClassType().getCanonicalName());
                    }
                    propertyClass.detectCycle(visited, sortedList, tempMarked, currentPath);
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
            throw new RuntimeException("Blahh");
        }

        // Add the class to the result in topologically sorted order
        currentPath.remove(currentPath.size() - 1); // Remove from current path
        tempMarked.remove(this);
        visited.add(this);
        sortedList.add(this);
    }

    private static String buildCircularDependencyExceptionMessage(List<LHClassType> classList) {
        if (classList.isEmpty()) {
            throw new IllegalStateException(
                    "Tried to throw Circular Dependency exception but no classes found in class tree.");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Circular dependency found involving class: "
                + classList.get(classList.size() - 1).getClassType().getCanonicalName() + "\n");

        stringBuilder.append("\nDependency tree:\n");

        for (int i = 0; i < classList.size(); i++) {
            Class<?> visitedClass = classList.get(i).getClassType();
            stringBuilder
                    .append("  ".repeat(i))
                    .append("- ")
                    .append(visitedClass.getCanonicalName())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    private StructFieldDef getStructFieldDef() {
        StructFieldDef.Builder fieldDef = StructFieldDef.newBuilder();
        TypeDefinition typeDef = this.getTypeDefinition();
        fieldDef.setFieldType(typeDef);
        return fieldDef.build();
    }

    /**
     * Gets an InlineStructDef based on a given Java class
     *
     * @return an InlineStructDef representing the class stored in this LHClassType
     */
    public InlineStructDef getInlineStructDef() {
        if (this.inlineStructDef == null) {
            this.inlineStructDef = this.buildInlineStructDef();
        }
        return this.inlineStructDef;
    }

    private InlineStructDef buildInlineStructDef() {
        if (this.isLHPrimitive()) {
            throw new IllegalStateException("Cannot build InlineStructDef for primitive type");
        }

        InlineStructDef.Builder inlineStructDef = InlineStructDef.newBuilder();

        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                // Default property descriptor provided by Java
                if ("class".equals(pd.getName())) continue;

                LHStructProperty property = new LHStructProperty(pd);

                // Property descriptors with the LHStructIgnore annotation should be skipped
                if (property.isIgnored()) continue;

                LHClassType propertyClass = property.getPropertyType();
                String fieldName = property.getFieldName();
                TypeDefinition typeDef = propertyClass.getTypeDefinition().toBuilder()
                        .setMasked(property.isMasked())
                        .build();
                StructFieldDef fieldDef =
                        StructFieldDef.newBuilder().setFieldType(typeDef).build();

                inlineStructDef.putFields(fieldName, fieldDef);
            }
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot build InlineStructDef for class: " + this.clazz.getCanonicalName());
        }

        return inlineStructDef.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LHClassType other = (LHClassType) obj;
        return clazz.equals(other.clazz);
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }
}
