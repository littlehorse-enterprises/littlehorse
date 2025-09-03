package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.exception.StructDefCircularDependencyException;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.StructDef;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.worker.LHStructDef;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LHStructDefType extends LHClassType {
    private List<LHStructDefType> dependencyClasses;
    private InlineStructDef inlineStructDef;

    private List<LHStructProperty> structProperties;

    public LHStructDefType(Class<?> clazz) {
        super(clazz);

        if (!clazz.isAnnotationPresent(LHStructDef.class)) {
            throw new IllegalArgumentException(
                    "Cannot create LHStructDefType, missing `@LHStructDef` annotation on provided class: "
                            + this.clazz);
        }

        this.inlineStructDef = this.buildInlineStructDef();
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.STRUCT_DEF_ID;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder()
                .setStructDefId(StructDefId.newBuilder()
                        .setName(this.getStructDefAnnotation().name())
                        .build())
                .build();
    }

    public StructDefId getStructDefId() {
        String structName = this.getStructDefAnnotation().name();

        return StructDefId.newBuilder().setName(structName).build();
    }

    private LHStructDef getStructDefAnnotation() {
        return this.clazz.getAnnotation(LHStructDef.class);
    }

    public PutStructDefRequest toPutStructDefRequest() {
        return PutStructDefRequest.newBuilder()
                .setName(this.getStructDefAnnotation().name())
                .setDescription(this.getStructDefAnnotation().description())
                .setStructDef(this.getInlineStructDef())
                .build();
    }

    public StructDef toStructDef() {
        LHStructDef annotation = this.getStructDefAnnotation();

        StructDef.Builder structDef = StructDef.newBuilder();
        structDef.setId(StructDefId.newBuilder().setName(annotation.name()));
        structDef.setDescription(annotation.description());
        structDef.setStructDef(this.getInlineStructDef());

        return structDef.build();
    }

    public List<LHStructDefType> getDependencyClasses() {
        if (this.dependencyClasses == null) {
            this.dependencyClasses = collectDependencyClasses();
        }
        return Collections.unmodifiableList(this.dependencyClasses);
    }

    private List<LHStructDefType> collectDependencyClasses() {
        Set<LHClassType> visited = new HashSet<>();
        List<LHStructDefType> sortedList = new ArrayList<>();
        Set<LHClassType> tempMarked = new HashSet<>();

        try {
            detectCycle(visited, sortedList, tempMarked, new ArrayList<>());
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Error ");
        }

        return sortedList;
    }

    private void detectCycle(
            Set<LHClassType> visited,
            List<LHStructDefType> sortedList,
            Set<LHClassType> tempMarked,
            List<LHClassType> currentPath)
            throws IntrospectionException {

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

        for (LHStructProperty property : this.getStructProperties()) {
            LHClassType coreType = property.getPropertyType().getCoreComponentType();

            if (coreType instanceof LHPrimitiveType) {
                continue;
            }
            if (coreType instanceof LHStructDefType) {
                LHStructDefType propertyCoreType = (LHStructDefType) coreType;
                propertyCoreType.detectCycle(visited, sortedList, tempMarked, currentPath);
            } else {
                throw new IllegalArgumentException(
                        "Missing @LHStructDef annotation on non-primitive class used in an LHStructDef getter or setter: "
                                + coreType.getClassType().getCanonicalName());
            }
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

    /**
     * Gets the properties defined by a given StructDef class
     *
     * @return a list of {@LHStructProperty}s describing the properties of your
     *         StructDef class
     * @throws IntrospectionException if an exception occurs during the
     *                                introspection of your class
     */
    public List<LHStructProperty> getStructProperties() throws IntrospectionException {
        if (this.structProperties == null) {
            this.structProperties = this.buildStructProperties();
        }
        return this.structProperties;
    }

    private List<LHStructProperty> buildStructProperties() throws IntrospectionException {
        return List.of(Introspector.getBeanInfo(this.clazz).getPropertyDescriptors()).stream()
                // Default property descriptor provided by Java, should be filtered out
                .filter((PropertyDescriptor pd) -> !"class".equals(pd.getName()))

                // Convert to our domain LHStructProperty class
                .map((PropertyDescriptor pd) -> new LHStructProperty(pd))

                // Property descriptors with the LHStructIgnore annotation should be skipped
                .filter((LHStructProperty property) -> !property.isIgnored())
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Gets an InlineStructDef based on a given Java class
     *
     * @return an InlineStructDef representing the class stored in this LHClassType
     */
    public InlineStructDef getInlineStructDef() {
        return this.inlineStructDef;
    }

    private InlineStructDef buildInlineStructDef() {
        InlineStructDef.Builder inlineStructDef = InlineStructDef.newBuilder();

        try {
            for (LHStructProperty property : this.getStructProperties()) {
                inlineStructDef.putFields(property.getFieldName(), property.toStructFieldDef());
            }
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot build InlineStructDef for type: " + this.clazz.getName(), e);
        }

        return inlineStructDef.build();
    }
}
