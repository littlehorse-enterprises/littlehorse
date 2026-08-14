package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.proto.InlineStructDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LHInlineStructDefType represents a struct type whose schema is emitted inline (as an
 * {@code InlineStructDef}) rather than as a reference to a named {@code StructDefId}.
 *
 * <p>Use this when you want to express the complete structure of a POJO directly inside a
 * {@link TypeDefinition} without first registering a named {@code StructDef} with the server.
 * The resulting {@link TypeDefinition} will have its {@code defined_type} oneof set to
 * {@code inline_struct_def}.
 *
 * <p>Contrast with {@link LHStructDefType}, which requires the class to carry an
 * {@code @LHStructDef} annotation and emits a {@code struct_def_id} reference instead.
 *
 * <p>The Java class provided to this constructor does <em>not</em> need to be annotated with
 * {@code @LHStructDef}; any POJO whose properties can be introspected via {@link Introspector}
 * is accepted.
 */
public class LHInlineStructDefType extends LHClassType {

    private final InlineStructDef inlineStructDef;
    private List<LHStructProperty> structProperties;

    /**
     * Creates an {@code LHInlineStructDefType} for the given class, using an empty type-adapter
     * registry.
     *
     * @param clazz the Java class whose bean properties define the inline struct schema
     */
    public LHInlineStructDefType(Class<?> clazz) {
        this(clazz, LHTypeAdapterRegistry.empty());
    }

    /**
     * Creates an {@code LHInlineStructDefType} for the given class.
     *
     * @param clazz              the Java class whose bean properties define the inline struct schema
     * @param typeAdapterRegistry the registry used when resolving property types
     */
    public LHInlineStructDefType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        this(clazz, typeAdapterRegistry, Map.of());
    }

    /**
     * Creates an {@code LHInlineStructDefType} for the given class, with placeholder resolution
     * support for any nested {@code @LHStructDef}-annotated property types.
     *
     * @param clazz               the Java class whose bean properties define the inline struct schema
     * @param typeAdapterRegistry the registry used when resolving property types
     * @param placeholderValues   placeholder values used to resolve {@code ${...}} tokens in any
     *                            nested {@code @LHStructDef} names
     */
    public LHInlineStructDefType(
            Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry, Map<String, String> placeholderValues) {
        super(clazz, typeAdapterRegistry, placeholderValues);
        this.inlineStructDef = buildInlineStructDef();
    }

    @Override
    public DefinedTypeCase getDefinedTypeCase() {
        return DefinedTypeCase.INLINE_STRUCT_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return TypeDefinition.newBuilder().setInlineStructDef(inlineStructDef).build();
    }

    /**
     * Returns the {@link InlineStructDef} derived from this class's bean properties.
     *
     * @return the inline struct definition
     */
    public InlineStructDef getInlineStructDef() {
        return inlineStructDef;
    }

    /**
     * Returns the list of {@link LHStructProperty}s for this class (lazily built once).
     *
     * @return an unmodifiable list of struct properties
     * @throws IntrospectionException if bean introspection fails
     */
    public List<LHStructProperty> getStructProperties() throws IntrospectionException {
        if (this.structProperties == null) {
            this.structProperties = buildStructProperties();
        }
        return this.structProperties;
    }

    private List<LHStructProperty> buildStructProperties() throws IntrospectionException {
        return List.of(Introspector.getBeanInfo(this.clazz).getPropertyDescriptors()).stream()
                .filter((PropertyDescriptor pd) -> !"class".equals(pd.getName()))
                .map((PropertyDescriptor pd) -> new LHStructProperty(pd, this))
                .filter((LHStructProperty property) -> !property.isIgnored())
                .collect(Collectors.toUnmodifiableList());
    }

    private InlineStructDef buildInlineStructDef() {
        InlineStructDef.Builder builder = InlineStructDef.newBuilder();
        try {
            for (LHStructProperty property : buildStructProperties()) {
                builder.putFields(property.getFieldName(), property.toStructFieldDef(typeAdapterRegistry));
            }
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Cannot build InlineStructDef for type: " + this.clazz.getName(), e);
        }
        return builder.build();
    }
}
