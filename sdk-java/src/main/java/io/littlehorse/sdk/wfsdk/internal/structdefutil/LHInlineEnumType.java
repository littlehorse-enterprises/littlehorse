package io.littlehorse.sdk.wfsdk.internal.structdefutil;

import io.littlehorse.sdk.common.adapter.LHTypeAdapterRegistry;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.EnumMemberDef;
import io.littlehorse.sdk.common.proto.InlineEnumDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.worker.LHEnumValueDef;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LHInlineEnumType extends LHClassType {
    private final TypeDefinition typeDefinition;
    private final Map<String, Enum<?>> members = new HashMap<>();

    public LHInlineEnumType(Class<?> clazz, LHTypeAdapterRegistry typeAdapterRegistry) {
        super(clazz, typeAdapterRegistry);
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException("Expected a Java enum class: " + clazz.getName());
        }
        Object[] constants = clazz.getEnumConstants();
        if (constants.length == 0) {
            throw new IllegalArgumentException("An inline enum must have at least one member");
        }

        InlineEnumDef.Builder definition = InlineEnumDef.newBuilder();
        Set<Integer> backingValues = new HashSet<>();
        long nextBackingValue = 0;
        for (Object constant : constants) {
            Enum<?> member = (Enum<?>) constant;
            LHEnumValueDef annotation;
            // Extract optional user-supplied backing value
            try {
                annotation = clazz.getField(member.name()).getAnnotation(LHEnumValueDef.class);
            } catch (NoSuchFieldException ex) {
                throw new IllegalArgumentException("Cannot find enum member field: " + member.name(), ex);
            }
            long backingValue = annotation == null ? nextBackingValue : annotation.value();
            if (backingValue < 0 || backingValue > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Enum member " + member.name() + " must have a non-negative int32 backing value");
            }
            if (!backingValues.add((int) backingValue)) {
                throw new IllegalArgumentException("Duplicate enum backing value: " + backingValue);
            }
            members.put(member.name(), member);
            definition.addMembers(
                    EnumMemberDef.newBuilder().setName(member.name()).setBackingValue((int) backingValue));
            nextBackingValue = backingValue + 1;
        }
        typeDefinition =
                TypeDefinition.newBuilder().setInlineEnumDef(definition).build();
    }

    @Override
    public TypeDefinition.DefinedTypeCase getDefinedTypeCase() {
        return TypeDefinition.DefinedTypeCase.INLINE_ENUM_DEF;
    }

    @Override
    public TypeDefinition getTypeDefinition() {
        return typeDefinition;
    }

    public VariableValue toVariableValue(Object value) {
        if (value == null) {
            return VariableValue.getDefaultInstance();
        }
        // Compare members by name. A member of a different enum class is valid
        // if this enum has a member with that name.
        if (!(value instanceof Enum<?> member)) {
            throw new LHSerdeException("Expected an enum member of " + clazz.getName());
        }
        return memberNameToVariableValue(member.name());
    }

    public VariableValue memberNameToVariableValue(String name) {
        if (!members.containsKey(name)) {
            throw new LHSerdeException("Unknown enum member " + name + " for " + clazz.getName());
        }
        return VariableValue.newBuilder()
                .setEnum(io.littlehorse.sdk.common.proto.Enum.newBuilder().setName(name))
                .build();
    }

    public Object fromVariableValue(VariableValue value) {
        if (value.getValueCase() == VariableValue.ValueCase.VALUE_NOT_SET) {
            return null;
        }
        if (!value.hasEnum()) {
            throw new LHSerdeException("Expected an ENUM for " + clazz.getName() + ", got " + value.getValueCase());
        }
        String name = value.getEnum().getName();
        Enum<?> member = members.get(name);
        if (member == null) {
            throw new LHSerdeException("Unknown enum member " + name + " for " + clazz.getName());
        }
        return member;
    }
}
