package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.worker.LHStructDef;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

/**
 * LHClassType is an abstract class that represents a Java class type in the context of LittleHorse's workflow SDK.
 *
 * It provides methods for creating instances of the class, retrieving type definitions, and handling type adapters.
 */
public abstract class LHClassType {
    protected Class<?> clazz;
    protected LHTypeAdapterRegistry typeAdapterRegistry;
    protected Map<String, String> placeholderValues = Map.of();

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
        return fromJavaClass(classType, typeAdapterRegistry, Map.of());
    }

    /**
     * Creates an LHClassType from a given Java class, resolving any placeholders in the referenced
     * {@code @LHStructDef} name(s) using the provided placeholder values.
     *
     * @param classType the Java class to convert to an LHClassType
     * @param typeAdapterRegistry the LHTypeAdapterRegistry to use for handling type adapters
     * @param placeholderValues placeholder values used to resolve {@code ${...}} placeholders in StructDef names
     * @return an LHClassType representing the provided Java class
     */
    public static LHClassType fromJavaClass(
            Class<?> classType, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        if (classType == null) {
            throw new IllegalArgumentException("Class type should not be null");
        } else if (void.class.equals(classType) || Void.class.equals(classType)) {
            throw new IllegalArgumentException(
                    "Void type is not supported as a variable type in LittleHorse. Void cases should be handled before creating LHClassTypes.");
        } else if (LHLibUtil.isJavaClassLHPrimitive(classType)) {
            return new LHPrimitiveType(classType, typeAdapterRegistry);
        } else if (classType.isAnnotationPresent(LHStructDef.class)) {
            return new LHStructDefType(classType, typeAdapterRegistry, placeholderValues);
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

    protected LHClassType() {}

    protected LHClassType(Class<?> clazz, LHTypeAdapterRegistry lhTypeAdapterRegistry) {
        this(clazz, lhTypeAdapterRegistry, Map.of());
    }

    protected LHClassType(
            Class<?> clazz, LHTypeAdapterRegistry lhTypeAdapterRegistry, Map<String, String> placeholderValues) {
        this.clazz = Objects.requireNonNull(clazz);
        this.typeAdapterRegistry = Objects.requireNonNull(lhTypeAdapterRegistry);
        this.placeholderValues = placeholderValues == null ? Map.of() : Map.copyOf(placeholderValues);
    }

    public Class<?> getClassType() {
        return this.clazz;
    }

    /**
     * Returns the placeholder values used to resolve {@code ${...}} placeholders in StructDef names.
     *
     * @return the placeholder values (never null)
     */
    public Map<String, String> getPlaceholderValues() {
        return this.placeholderValues;
    }

    /**
     * Within a series of nested Arrays, grabs the root component type. For example, if the class is String[][][], this method will return the LHClassType for String.
     * @param typeAdapterRegistry the LHTypeAdapterRegistry to use for handling type adapters when determining the core component type
     * @return the LHClassType representing the core component type of the array
     */
    public LHClassType getCoreComponentType(LHTypeAdapterRegistry typeAdapterRegistry) {
        Class<?> coreType = clazz;

        while (coreType.isArray()) {
            coreType = coreType.getComponentType();
        }

        return LHClassType.fromJavaClass(coreType, typeAdapterRegistry, placeholderValues);
    }

    public LHClassType getComponentType(LHTypeAdapterRegistry typeAdapterRegistry) {
        if (!clazz.isArray()) {
            throw new IllegalStateException("getComponentType can only be called on array types, but class "
                    + clazz.getName() + " is not an array.");
        }
        Class<?> componentType = clazz.getComponentType();
        return LHClassType.fromJavaClass(componentType, typeAdapterRegistry, placeholderValues);
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
