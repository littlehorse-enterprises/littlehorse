package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.worker.LHStructField;
import io.littlehorse.sdk.worker.LHStructIgnore;
import java.beans.PropertyDescriptor;

public class LHStructProperty {
    private final PropertyDescriptor pd;

    public LHStructProperty(PropertyDescriptor pd) {
        if (pd == null) {
            throw new IllegalArgumentException("");
        }
        this.pd = pd;
    }

    public boolean isIgnored() {
        if ((hasReadMethod() && pd.getReadMethod().isAnnotationPresent(LHStructIgnore.class))
                || (pd.getWriteMethod() != null && pd.getWriteMethod().isAnnotationPresent(LHStructIgnore.class))) {
            return true;
        }
        return false;
    }

    /**
     * Gets the LHClassType for a given PropertyDescriptor
     * @param pd A PropertyDescriptor representing your Java Beans property
     * @return An optional LHClassType, which will be empty if the property contains the LHStructIgnore annotation on its getter or setters
     */
    public LHClassType getPropertyType() {
        return new LHClassType(pd.getPropertyType());
    }

    public boolean isMasked() {
        if (hasReadMethod() && pd.getReadMethod().isAnnotationPresent(LHStructField.class)) {
            return pd.getReadMethod().getAnnotation(LHStructField.class).masked();
        }
        if (hasWriteMethod() && pd.getWriteMethod().isAnnotationPresent(LHStructField.class)) {
            return pd.getWriteMethod().getAnnotation(LHStructField.class).masked();
        }
        return false;
    }

    public String getFieldName() {
        if (hasReadMethod() && pd.getReadMethod().isAnnotationPresent(LHStructField.class)) {
            String fieldName = pd.getReadMethod().getAnnotation(LHStructField.class).name();
            if (!fieldName.isBlank()) {
                return fieldName;
            }
        }
        if (hasWriteMethod() && pd.getWriteMethod().isAnnotationPresent(LHStructField.class)) {
            String fieldName = pd.getWriteMethod().getAnnotation(LHStructField.class).name();
            if (!fieldName.isBlank()) {
                return fieldName;
            }
        }

        return pd.getName();
    }

    private boolean hasReadMethod() {
        return pd.getReadMethod() != null;
    }

    private boolean hasWriteMethod() {
        return pd.getWriteMethod() != null;
    }
}
