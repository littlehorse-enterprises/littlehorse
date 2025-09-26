package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.worker.LHStructDef;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public abstract class LHClassType {
    protected Class<?> clazz;

    public static LHClassType fromJavaClass(Class<?> classType) {
        if (classType == null) {
            throw new IllegalArgumentException("Class type should not be null");
        } else if (LHLibUtil.isJavaClassLHPrimitive(classType)) {
            return new LHPrimitiveType(classType);
        } else if (classType.isAnnotationPresent(LHStructDef.class)) {
            return new LHStructDefType(classType);
        } else if (classType.isArray()) {
            return new LHArrayDefType(classType);
        }
        return new LHPrimitiveType(classType);
    }

    public Object createInstance()
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
                    NoSuchMethodException, SecurityException {
        return clazz.getDeclaredConstructor().newInstance();
    }

    public abstract TypeDefinition.DefinedTypeCase getDefinedTypeCase();

    public abstract TypeDefinition getTypeDefinition();

    protected LHClassType(Class<?> clazz) {
        this.clazz = Objects.requireNonNull(clazz);
    }

    public Class<?> getClassType() {
        return this.clazz;
    }

    public LHClassType getCoreComponentType() {
        Class<?> coreType = clazz;

        while (coreType.isArray()) {
            coreType = coreType.getComponentType();
        }

        return LHClassType.fromJavaClass(coreType);
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
