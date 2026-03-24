package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.worker.LHStructDef;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * LHClassType is an abstract class that represents a Java class type in the context of LittleHorse's workflow SDK.
 *
 * It provides methods for creating instances of the class, retrieving type definitions, and handling type adapters.
 */
public abstract class LHClassType {
    protected Class<?> clazz;
    protected LHTypeAdapterRegistry typeAdapterRegistry;

    /**
     * @deprecated Use {@link #fromJavaClass(Class, LHTypeAdapterRegistry)} instead, which allows for proper handling of type adapters.
     * @param classType the Java class to convert to an LHClassType
     * @return an LHClassType representing the provided Java class
     */
    @Deprecated(since = "0.16.0", forRemoval = true)
    public static LHClassType fromJavaClass(Class<?> classType) {
        return fromJavaClass(classType, LHTypeAdapterRegistry.empty());
    }

    /**
     * Creates an LHClassType from a given Java class, using the provided LHTypeAdapterRegistry to handle any type adapters.
     *
     * @param classType the Java class to convert to an LHClassType
     * @param typeAdapterRegistry the LHTypeAdapterRegistry to use for handling type adapters
     * @return an LHClassType representing the provided Java class
     */
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

    protected LHClassType(Class<?> clazz, LHTypeAdapterRegistry lhTypeAdapterRegistry) {
        this.clazz = Objects.requireNonNull(clazz);
        this.typeAdapterRegistry = Objects.requireNonNull(lhTypeAdapterRegistry);
    }

    public Class<?> getClassType() {
        return this.clazz;
    }

    public LHClassType getCoreComponentType() {
        return getCoreComponentType(this.typeAdapterRegistry);
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
