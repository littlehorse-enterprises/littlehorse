package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.sdk.wfsdk.LHExpression;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
class WfRunVariableImpl implements WfRunVariable {

    public String name;
    public VariableType type;
    private VariableValue defaultValue;
    private boolean required;
    private boolean searchable;
    private boolean masked;
    private Object typeOrDefaultVal;
    private List<JsonIndex> jsonIndexes = new ArrayList<>();
    private WfRunVariableAccessLevel accessLevel;

    @Getter
    private VariableType typeToCastTo;

    public String jsonPath;

    private final WorkflowThreadImpl parent;

    public WfRunVariableImpl(String name, Object typeOrDefaultVal, WorkflowThreadImpl parent) {
        this.name = name;
        this.typeOrDefaultVal = typeOrDefaultVal;
        this.parent = parent;

        // As per GH Issue #582, the default is now PRIVATE_VAR.
        this.accessLevel = WfRunVariableAccessLevel.PRIVATE_VAR;
        initializeType();
    }

    private void initializeType() {
        if (typeOrDefaultVal instanceof VariableType) {
            this.type = (VariableType) typeOrDefaultVal;
        } else {
            try {
                this.defaultValue = LHLibUtil.objToVarVal(typeOrDefaultVal);
                this.type = LHLibUtil.fromValueCase(defaultValue.getValueCase());
            } catch (LHSerdeError e) {
                throw new IllegalArgumentException(
                        "Was unable to convert provided default value to LH Variable Type", e);
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
        if (!type.equals(VariableType.JSON_OBJ) && !type.equals(VariableType.JSON_ARR)) {
            throw new LHMisconfigurationException(String.format("JsonPath not allowed in a %s variable", type.name()));
        }
        WfRunVariableImpl out = new WfRunVariableImpl(name, typeOrDefaultVal, parent);
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
        this.masked = true;
        return this;
    }

    @Override
    public WfRunVariable required() {
        this.required = true;
        return this;
    }

    @Override
    public WfRunVariable withDefault(Object defaultVal) {
        try {
            VariableValue attempt = LHLibUtil.objToVarVal(defaultVal);
            if (!LHLibUtil.fromValueCase(attempt.getValueCase()).equals(type)) {
                throw new IllegalArgumentException("Default value type does not match variable type");
            }
        } catch (LHSerdeError e) {
            throw new IllegalArgumentException("Was unable to convert provided default value to LH Variable Type", e);
        }
        return this;
    }

    @Override
    public WfRunVariable searchableOn(String fieldPath, VariableType fieldType) {
        if (!fieldPath.startsWith("$.")) {
            throw new LHMisconfigurationException(String.format("Invalid JsonPath: %s", fieldPath));
        }
        if (!type.equals(VariableType.JSON_OBJ) && !type.equals(VariableType.JSON_ARR)) {
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
    public void assignTo(Serializable rhs) {
        parent.mutate(this, VariableMutationType.ASSIGN, rhs);
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

    @Override
    public LHExpression asInt() {
        WfRunVariableImpl result = new WfRunVariableImpl(name, typeOrDefaultVal, parent);
        result.typeToCastTo = VariableType.INT;
        return result;
    }

    @Override
    public LHExpression asStr() {
        WfRunVariableImpl result = new WfRunVariableImpl(name, typeOrDefaultVal, parent);
        result.typeToCastTo = VariableType.STR;
        return result;
    }

    @Override
    public LHExpression asDouble() {
        WfRunVariableImpl result = new WfRunVariableImpl(name, typeOrDefaultVal, parent);
        result.typeToCastTo = VariableType.DOUBLE;
        return result;
    }

    public ThreadVarDef getSpec() {
        VariableDef.Builder varDef = VariableDef.newBuilder()
                .setType(this.getType())
                .setName(this.getName())
                .setMaskedValue(masked);

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
}
