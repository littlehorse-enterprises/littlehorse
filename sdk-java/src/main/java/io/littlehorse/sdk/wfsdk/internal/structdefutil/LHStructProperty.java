package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.StructFieldDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LHStructProperty {
    private final PropertyDescriptor pd;

    @Getter
    private final String fieldName;

    @Getter
    private final boolean masked;

    @Getter
    private final boolean ignored;

    private final LHStructDefType parentStructDef;

    public LHStructProperty(PropertyDescriptor pd, LHStructDefType parentStructDef) {
        this.pd = Objects.requireNonNull(pd);
        this.parentStructDef = parentStructDef;

        this.fieldName = findFieldName();
        this.masked = findIsMasked();
        this.ignored = findIsIgnored();
    }

    public VariableValue getValueFrom(Object o) throws LHSerdeException {
        if (pd.getReadMethod() == null) {
            throw new IllegalStateException(
                    "No read method for property " + this.fieldName + " found on object of type: " + o.getClass());
        }

        try {
            Object val = pd.getReadMethod().invoke(o);
            if (val == null) return null;
            return LHLibUtil.objToVarVal(val);
        } catch (LHSerdeException | IllegalAccessException | InvocationTargetException e) {
            throw new LHSerdeException(
                    e, "Failed getting value of property " + this.fieldName + "from object of type: " + o.getClass());
        }
    }

    public void setValueTo(Object o, VariableValue v) throws LHSerdeException {
        if (pd.getWriteMethod() == null) {
            throw new IllegalStateException(String.format(
                    "No write method for property [%s] found on object of type [%s]", this.fieldName, o.getClass()));
        }

        try {
            pd.getWriteMethod().invoke(o, LHLibUtil.varValToObj(v, pd.getPropertyType()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new LHSerdeException(
                    e,
                    String.format(
                            "Failed setting value of property [%s] from object of type", this.fieldName, o.getClass()));
        }
    }

    /**
     * Converts the LHStructProperty to a StructFieldDef
     * @return a StructFieldDef representing this property
     */
    public StructFieldDef toStructFieldDef() {
        LHClassType propertyClass = this.getPropertyType();
        TypeDefinition typeDef = propertyClass.getTypeDefinition().toBuilder()
                .setMasked(this.isMasked())
                .build();

        StructFieldDef.Builder fieldDef = StructFieldDef.newBuilder().setFieldType(typeDef);

        Optional<VariableValue> defaultValue = this.getDefaultValue();
        if (defaultValue.isPresent()) {
            fieldDef.setDefaultValue(defaultValue.get());
        }

        return fieldDef.build();
    }

    public Optional<VariableValue> getDefaultValue() {
        try {
            Object defaultInstance = parentStructDef.createInstance();

            return Optional.ofNullable(getValueFrom(defaultInstance));
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
        return LHClassType.fromJavaClass(pd.getPropertyType());
    }

    private <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if ((hasReadMethod() && pd.getReadMethod().isAnnotationPresent(annotationClass))) {
            return pd.getReadMethod().getAnnotation(annotationClass);
        }
        if ((hasWriteMethod() && pd.getWriteMethod().isAnnotationPresent(annotationClass))) {
            return pd.getWriteMethod().getAnnotation(annotationClass);
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

        if (lhStructField == null || lhStructField.name().isBlank()) return pd.getName();

        return lhStructField.name();
    }

    private boolean hasReadMethod() {
        return pd.getReadMethod() != null;
    }

    private boolean hasWriteMethod() {
        return pd.getWriteMethod() != null;
    }

    @Override
    public String toString() {
        return this.parentStructDef.toString() + ": " + this.fieldName;
    }
}
