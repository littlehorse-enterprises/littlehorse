package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.StructDefCircularDependencyException;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.worker.LHStructDef;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * LHClassType is an abstract class that represents a Java class type in the context of LittleHorse's workflow SDK.
 *
 * It provides methods for creating instances of the class, retrieving type definitions, and handling type adapters.
 */
public abstract class LHClassType {
    public enum ResolutionContext {
        /** Preserve unannotated Java objects as JSON values. */
        VALUE,

        /** Resolve unannotated concrete POJOs as embedded Struct schemas. */
        STRUCT_MEMBER
    }

    private static final ThreadLocal<HashSet<Class<?>>> inlineStructBuildPath = new ThreadLocal<>();
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
        return resolve(classType, typeAdapterRegistry, placeholderValues, ResolutionContext.VALUE);
    }

    public static LHClassType resolve(
            Class<?> classType,
            LHTypeAdapterRegistry typeAdapterRegistry,
            Map<String, String> placeholderValues,
            ResolutionContext context) {
        Objects.requireNonNull(context, "Resolution context should not be null");
        if (classType == null) {
            throw new IllegalArgumentException("Class type should not be null");
        } else if (void.class.equals(classType) || Void.class.equals(classType)) {
            throw new IllegalArgumentException(
                    "Void type is not supported as a variable type in LittleHorse. Void cases should be handled before creating LHClassTypes.");
        } else if (LHLibUtil.getTypeAdapterForClass(classType, typeAdapterRegistry)
                        .isPresent()
                || LHLibUtil.isJavaClassLHPrimitive(classType)) {
            return new LHPrimitiveType(classType, typeAdapterRegistry);
        } else if (classType.isAnnotationPresent(LHStructDef.class)) {
            return new LHStructDefType(classType, typeAdapterRegistry, placeholderValues);
        }
        if (context == ResolutionContext.VALUE
                || classType == Object.class
                || classType.isInterface()
                || classType.isEnum()) {
            return new LHPrimitiveType(classType, typeAdapterRegistry);
        }

        HashSet<Class<?>> buildPath = inlineStructBuildPath.get();
        if (buildPath == null) {
            buildPath = new HashSet<>();
            inlineStructBuildPath.set(buildPath);
        }
        if (!buildPath.add(classType)) {
            throw new StructDefCircularDependencyException(
                    "Circular inline StructDef dependency involving class: " + classType.getCanonicalName());
        }
        try {
            return new LHInlineStructDefType(classType, typeAdapterRegistry, placeholderValues);
        } finally {
            buildPath.remove(classType);
            if (buildPath.isEmpty()) {
                inlineStructBuildPath.remove();
            }
        }
    }

    public static LHClassType fromStructFieldJavaClass(
            Class<?> classType, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        return resolve(classType, typeAdapterRegistry, placeholderValues, ResolutionContext.STRUCT_MEMBER);
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
