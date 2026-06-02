package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHStructProperty {
    private final PropertyDescriptor pd;
    private final RecordComponent recordComponent;

    @Getter
    private final String propertyName;

    @Getter
    private final String fieldName;

    @Getter
    private final boolean masked;

    @Getter
    private final boolean ignored;

    @Getter
    private final boolean isNullable;

    private final LHStructDefType parentStructDef;

    public LHStructProperty(PropertyDescriptor pd, LHStructDefType parentStructDef) {
        this.pd = Objects.requireNonNull(pd);
        this.recordComponent = null;
        this.parentStructDef = parentStructDef;
        this.propertyName = pd.getName();

        this.fieldName = findFieldName();
        this.masked = findIsMasked();
        this.ignored = findIsIgnored();
        this.isNullable = findIsNullable();
    }

    public LHStructProperty(RecordComponent recordComponent, LHStructDefType parentStructDef) {
        this.pd = null;
        this.recordComponent = Objects.requireNonNull(recordComponent);
        this.parentStructDef = parentStructDef;
        this.propertyName = recordComponent.getName();

        this.fieldName = findFieldName();
        this.masked = findIsMasked();
        this.ignored = findIsIgnored();
        this.isNullable = findIsNullable();
    }

    public VariableValue getValueFrom(Object o) throws LHSerdeException {
        return getValueFrom(o, LHTypeAdapterRegistry.empty());
    }

    public VariableValue getValueFrom(Object o, LHTypeAdapterRegistry typeAdapterRegistry) throws LHSerdeException {
        Method readMethod = getReadMethod();
        if (readMethod == null) {
            throw new IllegalStateException(
                    "No read method for property " + this.fieldName + " found on object of type: " + o.getClass());
        }

        try {
            Object val = readMethod.invoke(o);
            if (val == null) return null;

            if (isNativeArray() && val.getClass().isArray()) {
                return LHLibUtil.objToVarValAsNativeArray(val, getPropertyTypeClass(), typeAdapterRegistry);
            }

            return LHLibUtil.objToVarVal(val, getPropertyTypeClass(), typeAdapterRegistry);
        } catch (LHSerdeException | IllegalAccessException | InvocationTargetException e) {
            throw new LHSerdeException(
                    e, "Failed getting value of property " + this.fieldName + "from object of type: " + o.getClass());
        }
    }

    public void setValueTo(Object o, VariableValue v) throws LHSerdeException {
        setValueTo(o, v, LHTypeAdapterRegistry.empty());
    }

    public void setValueTo(Object o, VariableValue v, LHTypeAdapterRegistry typeAdapterRegistry)
            throws LHSerdeException {
        Method writeMethod = getWriteMethod();
        if (writeMethod == null) {
            throw new IllegalStateException(String.format(
                    "No write method for property [%s] found on object of type [%s]", this.fieldName, o.getClass()));
        }

        try {
            writeMethod.invoke(o, deserializeValue(v, typeAdapterRegistry));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new LHSerdeException(
                    e,
                    String.format(
                            "Failed setting value of property [%s] from object of type", this.fieldName, o.getClass()));
        }
    }

    public Object deserializeValue(VariableValue value, LHTypeAdapterRegistry typeAdapterRegistry) {
        return LHLibUtil.varValToObj(value, getPropertyTypeClass(), typeAdapterRegistry);
    }

    /**
     * Converts the LHStructProperty to a StructFieldDef
     * @return a StructFieldDef representing this property
     */
    public StructFieldDef toStructFieldDef() {
        return toStructFieldDef(LHTypeAdapterRegistry.empty());
    }

    public StructFieldDef toStructFieldDef(LHTypeAdapterRegistry typeAdapterRegistry) {
        TypeDefinition typeDef = resolveValidatedFieldType(typeAdapterRegistry);

        StructFieldDef.Builder fieldDef =
                StructFieldDef.newBuilder().setFieldType(typeDef).setIsNullable(isNullable);

        Optional<VariableValue> defaultValue = this.getDefaultValue();
        if (defaultValue.isPresent()) {
            fieldDef.setDefaultValue(defaultValue.get());
        }

        return fieldDef.build();
    }

    private TypeDefinition resolveValidatedFieldType(LHTypeAdapterRegistry typeAdapterRegistry) {
        LHClassType propertyClass;
        try {
            propertyClass = this.getPropertyType(typeAdapterRegistry);

            LHTypeConstraintValidator.ensureNoJsonPrimitiveTypes(propertyClass.getTypeDefinition());
        } catch (IllegalArgumentException | ForbiddenJsonTypeException ex) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid StructDef field [%s] on class %s: %s",
                            this.fieldName, this.parentStructDef.getClassType().getCanonicalName(), ex.getMessage()),
                    ex);
        }

        TypeDefinition typeDef = propertyClass.getTypeDefinition().toBuilder()
                .setMasked(this.isMasked())
                .build();

        return typeDef;
    }

    public Optional<VariableValue> getDefaultValue() {
        return getDefaultValue(LHTypeAdapterRegistry.empty());
    }

    public Optional<VariableValue> getDefaultValue(LHTypeAdapterRegistry typeAdapterRegistry) {
        try {
            Object defaultInstance = parentStructDef.createInstance();

            return Optional.ofNullable(getValueFrom(defaultInstance, typeAdapterRegistry));
        } catch (Exception e) {
            log.warn(String.format(
                    "Unable to retrieve default value for Struct Property %s. Blank constructor may not be visible.",
                    this));
            return Optional.empty();
        }
    }

    /**
     * Gets the LHClassType for a given PropertyDescriptor
     * @param pd A PropertyDescriptor representing your Java Beans property
     * @return An optional LHClassType, which will be empty if the property contains the LHStructIgnore annotation on its getter or setters
     */
    public LHClassType getPropertyType() {
        return getPropertyType(LHTypeAdapterRegistry.empty());
    }

    public LHClassType getPropertyType(LHTypeAdapterRegistry typeAdapterRegistry) {
        if (isNativeArray()) {
            return new LHArrayType(getPropertyTypeClass(), typeAdapterRegistry);
        }

        return LHClassType.fromJavaClass(getPropertyTypeClass(), typeAdapterRegistry);
    }

    private boolean isNativeArray() {
        return getPropertyTypeClass().isArray() && !byte[].class.equals(getPropertyTypeClass());
    }

    /// The following methods are used to find annotations on the property, whether they are on the getter, setter, or
    // field itself.
    private <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (this.recordComponent != null && this.recordComponent.isAnnotationPresent(annotationClass)) {
            return this.recordComponent.getAnnotation(annotationClass);
        }

        if ((hasReadMethod() && getReadMethod().isAnnotationPresent(annotationClass))) {
            return getReadMethod().getAnnotation(annotationClass);
        }
        if ((hasWriteMethod() && getWriteMethod().isAnnotationPresent(annotationClass))) {
            return getWriteMethod().getAnnotation(annotationClass);
        }

        return getAnnotationFromField(annotationClass);
    }

    private <T extends Annotation> T getAnnotationFromField(Class<T> annotationClass) {
        for (String fieldName : getCandidateFieldNames()) {
            Field field = getFieldFromClassHierarchy(fieldName);

            if (field != null && field.isAnnotationPresent(annotationClass)) {
                return field.getAnnotation(annotationClass);
            }
        }

        return null;
    }

    private List<String> getCandidateFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add(propertyName);

        if (getPropertyTypeClass() == boolean.class || getPropertyTypeClass() == Boolean.class) {
            fieldNames.add("is" + Character.toUpperCase(propertyName.charAt(0))
                    + propertyName.substring(1));
        }

        return fieldNames;
    }

    private Field getFieldFromClassHierarchy(String fieldName) {
        Class<?> currentClass = this.parentStructDef.getClassType();

        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                currentClass = currentClass.getSuperclass();
            }
        }

        return null;
    }

    private boolean findIsIgnored() {
        return getAnnotation(LHStructIgnore.class) != null;
    }

    private boolean findIsMasked() {
        LHStructField lhStructField = getAnnotation(LHStructField.class);

        if (lhStructField == null) return false;

        return lhStructField.masked();
    }

    private String findFieldName() {
        LHStructField lhStructField = getAnnotation(LHStructField.class);

        if (lhStructField == null || lhStructField.name().isBlank()) return propertyName;

        return lhStructField.name();
    }

    private boolean findIsNullable() {
        LHStructField lhStructField = getAnnotation(LHStructField.class);

        if (lhStructField == null) return false;

        return lhStructField.isNullable();
    }

    private boolean hasReadMethod() {
        return getReadMethod() != null;
    }

    private boolean hasWriteMethod() {
        return getWriteMethod() != null;
    }

    private Method getReadMethod() {
        if (this.recordComponent != null) {
            return this.recordComponent.getAccessor();
        }

        return this.pd.getReadMethod();
    }

    private Method getWriteMethod() {
        if (this.recordComponent != null) {
            return null;
        }

        return this.pd.getWriteMethod();
    }

    private Class<?> getPropertyTypeClass() {
        if (this.recordComponent != null) {
            return this.recordComponent.getType();
        }

        return this.pd.getPropertyType();
    }

    @Override
    public String toString() {
        return this.parentStructDef.toString() + ": " + this.fieldName;
    }
}
