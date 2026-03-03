package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.worker.LHStructDef;
import io.littlehorse.sdk.worker.adapter.LHTypeAdapterRegistry;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public abstract class LHClassType {
    protected Class<?> clazz;

    public static LHClassType fromJavaClass(Class<?> classType) {
        return fromJavaClass(classType, LHTypeAdapterRegistry.empty());
    }

    public static LHClassType fromJavaClass(Class<?> classType, LHTypeAdapterRegistry typeAdapterRegistry) {
        if (classType == null) {
            throw new IllegalArgumentException("Class type should not be null");
        } else if (LHLibUtil.isJavaClassLHPrimitive(classType)) {
            return new LHPrimitiveType(classType, typeAdapterRegistry);
        } else if (classType.isAnnotationPresent(LHStructDef.class)) {
            return new LHStructDefType(classType, typeAdapterRegistry);
        }
        return new LHPrimitiveType(classType, typeAdapterRegistry);
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
        return getCoreComponentType(LHTypeAdapterRegistry.empty());
    }

    public LHClassType getCoreComponentType(LHTypeAdapterRegistry typeAdapterRegistry) {
        Class<?> coreType = clazz;

        while (coreType.isArray()) {
            coreType = coreType.getComponentType();
        }

        return LHClassType.fromJavaClass(coreType, typeAdapterRegistry);
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
