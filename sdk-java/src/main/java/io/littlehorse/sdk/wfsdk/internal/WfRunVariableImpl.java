package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.StructDefId;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.TypeDefinition;
import io.littlehorse.sdk.common.proto.TypeDefinition.DefinedTypeCase;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.wfsdk.LHExpression;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHClassType;
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
class WfRunVariableImpl implements WfRunVariable {

    public String name;
    public DefinedTypeCase definedType;
    public TypeDefinition typeDef;
    private VariableValue defaultValue;
    private boolean required;
    private boolean searchable;
    private Object typeOrDefaultVal;
    private List<JsonIndex> jsonIndexes = new ArrayList<>();
    private WfRunVariableAccessLevel accessLevel;

    public String jsonPath;
    public List<String> structPath;

    private final WorkflowThreadImpl parent;

    public static WfRunVariableImpl createPrimitiveVar(
            String name, Object typeOrDefaultVal, WorkflowThreadImpl parent) {
        if (typeOrDefaultVal == null) {
            throw new IllegalArgumentException(
                    "The 'typeOrDefaultVal' argument must be either a VariableType or a default value, but a null value was provided.");
        }
        return new WfRunVariableImpl(name, typeOrDefaultVal, null, null, null, parent);
    }

    public static WfRunVariableImpl createStructDefVar(String name, LHStructDefType clazz, WorkflowThreadImpl parent) {
        return new WfRunVariableImpl(name, null, clazz, null, null, parent);
    }

    public static WfRunVariableImpl createStructDefVar(String name, String structDefName, WorkflowThreadImpl parent) {
        return new WfRunVariableImpl(name, null, null, structDefName, null, parent);
    }

    private WfRunVariableImpl(
            String name,
            Object typeOrDefaultVal,
            LHClassType structClass,
            String structDefName,
            LHClassType arrayElementType,
            WorkflowThreadImpl parent) {
        this.name = name;
        this.parent = Objects.requireNonNull(parent, "Parent thread cannot be null.");
        this.structPath = new ArrayList<>();

        // As per GH Issue #582, the default is now PRIVATE_VAR.
        this.accessLevel = WfRunVariableAccessLevel.PRIVATE_VAR;

        if (typeOrDefaultVal != null) {
            this.definedType = DefinedTypeCase.PRIMITIVE_TYPE;
            this.typeOrDefaultVal = typeOrDefaultVal;

            if (typeOrDefaultVal instanceof VariableType) {
                this.typeDef = TypeDefinition.newBuilder()
                        .setPrimitiveType((VariableType) typeOrDefaultVal)
                        .build();
            } else {
                setDefaultValue(typeOrDefaultVal);
                this.typeDef = TypeDefinition.newBuilder()
                        .setPrimitiveType(LHLibUtil.fromValueCase(defaultValue.getValueCase()))
                        .build();
            }
        } else if (structClass != null || structDefName != null) {
            this.definedType = DefinedTypeCase.STRUCT_DEF_ID;

            if (structClass != null) {
                this.typeDef = structClass.getTypeDefinition();
            } else {
                this.typeDef = TypeDefinition.newBuilder()
                        .setStructDefId(StructDefId.newBuilder().setName(structDefName))
                        .build();
            }
        }
    }

    @Override
    public WfRunVariableImpl withAccessLevel(WfRunVariableAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
        return this;
    }

    @Override
    public WfRunVariableImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new LHMisconfigurationException("Cannot use jsonpath() twice on same var!");
        }
        if (typeDef.getDefinedTypeCase() != DefinedTypeCase.PRIMITIVE_TYPE) {
            throw new LHMisconfigurationException(
                    String.format("JsonPath not allowed in a %s variable", typeDef.getDefinedTypeCase()));
        }
        if (!typeDef.getPrimitiveType().equals(VariableType.JSON_OBJ)
                && !typeDef.getPrimitiveType().equals(VariableType.JSON_ARR)) {
            throw new LHMisconfigurationException(String.format(
                    "JsonPath not allowed in a %s variable",
                    typeDef.getPrimitiveType().name()));
        }
        WfRunVariableImpl out = WfRunVariableImpl.createPrimitiveVar(name, typeOrDefaultVal, parent);
        out.jsonPath = path;
        return out;
    }

    @Override
    public WfRunVariable searchable() {
        this.searchable = true;
        return this;
    }

    @Override
    public WfRunVariable masked() {
        this.typeDef = typeDef.toBuilder().setMasked(true).build();
        return this;
    }

    @Override
    public WfRunVariable required() {
        this.required = true;
        return this;
    }

    @Override
    public WfRunVariable withDefault(Object defaultVal) {
        setDefaultValue(defaultVal);

        if (!LHLibUtil.fromValueCase(defaultValue.getValueCase()).equals(typeDef.getPrimitiveType())) {
            throw new IllegalArgumentException("Default value type does not match LH variable type " + typeDef);
        }

        return this;
    }

    private void setDefaultValue(Object defaultVal) {
        try {
            this.defaultValue = LHLibUtil.objToVarVal(defaultVal);
        } catch (LHSerdeException e) {
            throw new IllegalArgumentException("Was unable to convert provided default value to LH Variable Type", e);
        }
    }

    @Override
    public WfRunVariable searchableOn(String fieldPath, VariableType fieldType) {
        if (!fieldPath.startsWith("$.")) {
            throw new LHMisconfigurationException(String.format("Invalid JsonPath: %s", fieldPath));
        }
        if (!typeDef.getPrimitiveType().equals(VariableType.JSON_OBJ)
                && !typeDef.getPrimitiveType().equals(VariableType.JSON_ARR)) {
            throw new LHMisconfigurationException(String.format("Non-Json %s variable contains jsonIndex", name));
        }
        this.jsonIndexes.add(JsonIndex.newBuilder()
                .setFieldPath(fieldPath)
                .setFieldType(fieldType)
                .build());
        return this;
    }

    @Override
    public WorkflowConditionImpl isEqualTo(Serializable rhs) {
        return parent.condition(this, Comparator.EQUALS, rhs);
    }

    @Override
    public WorkflowConditionImpl isNotEqualTo(Serializable rhs) {
        return parent.condition(this, Comparator.NOT_EQUALS, rhs);
    }

    @Override
    public WorkflowConditionImpl isGreaterThan(Serializable rhs) {
        return parent.condition(this, Comparator.GREATER_THAN, rhs);
    }

    @Override
    public WorkflowConditionImpl isGreaterThanEq(Serializable rhs) {
        return parent.condition(this, Comparator.GREATER_THAN_EQ, rhs);
    }

    @Override
    public WorkflowConditionImpl isLessThanEq(Serializable rhs) {
        return parent.condition(this, Comparator.LESS_THAN_EQ, rhs);
    }

    @Override
    public WorkflowConditionImpl isLessThan(Serializable rhs) {
        return parent.condition(this, Comparator.LESS_THAN, rhs);
    }

    @Override
    public WorkflowConditionImpl doesContain(Serializable rhs) {
        return parent.condition(rhs, Comparator.IN, this);
    }

    @Override
    public WorkflowConditionImpl doesNotContain(Serializable rhs) {
        return parent.condition(rhs, Comparator.NOT_IN, this);
    }

    @Override
    public WorkflowConditionImpl isIn(Serializable rhs) {
        return parent.condition(this, Comparator.IN, rhs);
    }

    @Override
    public WorkflowConditionImpl isNotIn(Serializable rhs) {
        return parent.condition(this, Comparator.NOT_IN, rhs);
    }

    @Override
    public void assign(Serializable rhs) {
        WorkflowThreadImpl activeThread = parent;

        WorkflowThreadImpl lastThread = parent.getParent().getThreads().peek();

        if (lastThread.isActive()) {
            activeThread = lastThread;
        }

        activeThread.mutate(this, VariableMutationType.ASSIGN, rhs);
    }

    @Override
    public LHExpression add(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.ADD, other);
    }

    @Override
    public LHExpression subtract(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.SUBTRACT, other);
    }

    @Override
    public LHExpression multiply(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.MULTIPLY, other);
    }

    @Override
    public LHExpression divide(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.DIVIDE, other);
    }

    @Override
    public LHExpression extend(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.EXTEND, other);
    }

    @Override
    public LHExpression removeIfPresent(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_IF_PRESENT, other);
    }

    @Override
    public LHExpression removeIndex(int index) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeIndex(LHExpression index) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_INDEX, index);
    }

    @Override
    public LHExpression removeKey(Serializable key) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_KEY, key);
    }

    public ThreadVarDef getSpec() {
        VariableDef.Builder varDef =
                VariableDef.newBuilder().setTypeDef(typeDef).setName(this.getName());

        if (this.defaultValue != null) {
            varDef.setDefaultValue(defaultValue);
        }

        return ThreadVarDef.newBuilder()
                .setVarDef(varDef)
                .setRequired(required)
                .setSearchable(searchable)
                .addAllJsonIndexes(jsonIndexes)
                .setAccessLevel(accessLevel)
                .build();
    }

    @Override
    public WfRunVariableImpl asPublic() {
        return this.withAccessLevel(WfRunVariableAccessLevel.PUBLIC_VAR);
    }

    @Override
    public WfRunVariable asInherited() {
        return this.withAccessLevel(WfRunVariableAccessLevel.INHERITED_VAR);
    }

    @Override
    public LHExpression castTo(VariableType targetType) {
        return new CastExpressionImpl(this, targetType);
    }
}
