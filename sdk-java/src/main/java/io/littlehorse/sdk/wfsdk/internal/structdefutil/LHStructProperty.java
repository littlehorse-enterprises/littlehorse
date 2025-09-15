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
import java.lang.reflect.Method;
import java.util.Objects;
import lombok.Getter;

public class LHStructProperty {
    private final PropertyDescriptor pd;

    @Getter
    private final String fieldName;

    @Getter
    private final boolean masked;

    @Getter
    private final boolean ignored;

    public LHStructProperty(PropertyDescriptor pd) {
        this.pd = Objects.requireNonNull(pd);

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
            return LHLibUtil.objToVarVal(pd.getReadMethod().invoke(o));
        } catch (LHSerdeException | IllegalAccessException | InvocationTargetException e) {
            throw new LHSerdeException(
                    e, "Failed getting value of property " + this.fieldName + "from object of type: " + o.getClass());
        }
    }

    public void setValueTo(Object o, VariableValue v) throws LHSerdeException {
        if (pd.getWriteMethod() == null) {
            throw new IllegalStateException(
                    "No write method for property [%s] found on object of type [%s]".formatted(this.fieldName, o.getClass()));
        }

        try {
            pd.getWriteMethod().invoke(o, LHLibUtil.varValToObj(v, pd.getPropertyType()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new LHSerdeException(
                    e, "Failed setting value of property [%s] from object of type".formatted(this.fieldName, o.getClass()));
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
        StructFieldDef fieldDef =
                StructFieldDef.newBuilder().setFieldType(typeDef).build();

        return fieldDef;
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
}
