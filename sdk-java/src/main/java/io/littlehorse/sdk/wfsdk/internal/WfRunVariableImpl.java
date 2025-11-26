package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.LHPath.Selector;
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
import io.littlehorse.sdk.wfsdk.internal.structdefutil.LHStructDefType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PROTECTED)
class WfRunVariableImpl implements WfRunVariable {

    public String name;
    public TypeDefinition typeDef;
    private VariableValue defaultValue;
    private boolean required;
    private boolean searchable;
    private List<JsonIndex> jsonIndexes = new ArrayList<>();
    private WfRunVariableAccessLevel accessLevel;

    private String jsonPath;
    private List<Selector> lhPath;

    private final WorkflowThreadImpl parent;

    private WfRunVariableImpl(String name, WorkflowThreadImpl parent) {
        this.name = Objects.requireNonNull(name, "Name cannot be null.");
        this.parent = Objects.requireNonNull(parent, "Parent thread cannot be null.");

        // As per GH Issue #582, the default is now PRIVATE_VAR.
        this.accessLevel = WfRunVariableAccessLevel.PRIVATE_VAR;

        this.lhPath = new ArrayList<>();
    }

    public static WfRunVariableImpl createPrimitiveVar(
            String name, Object typeOrDefaultVal, WorkflowThreadImpl parent) {
        if (typeOrDefaultVal == null) {
            throw new IllegalArgumentException(
                    "The 'typeOrDefaultVal' argument must be either a VariableType or a default value, but a null value was provided.");
        }
        WfRunVariableImpl wfRunVar = new WfRunVariableImpl(name, parent);
        wfRunVar.initializeAsPrimitive(typeOrDefaultVal);
        return wfRunVar;
    }

    public static WfRunVariableImpl createStructDefVar(String name, LHStructDefType clazz, WorkflowThreadImpl parent) {
        WfRunVariableImpl wfRunVar = new WfRunVariableImpl(name, parent);
        wfRunVar.initializeAsStructDef(clazz);
        return wfRunVar;
    }

    public static WfRunVariableImpl createStructDefVar(String name, String structDefName, WorkflowThreadImpl parent) {
        WfRunVariableImpl wfRunVar = new WfRunVariableImpl(name, parent);
        wfRunVar.initializeAsStructDef(structDefName);
        return wfRunVar;
    }

    private void initializeAsPrimitive(Object typeOrDefaultVal) {
        if (typeOrDefaultVal instanceof VariableType) {
            VariableType variableType = (VariableType) typeOrDefaultVal;
            this.typeDef =
                    TypeDefinition.newBuilder().setPrimitiveType(variableType).build();
        } else {
            setDefaultValue(typeOrDefaultVal);
            this.typeDef = TypeDefinition.newBuilder()
                    .setPrimitiveType(LHLibUtil.fromValueCase(defaultValue.getValueCase()))
                    .build();
        }
    }

    private void initializeAsStructDef(String structDefName) {
        this.typeDef = TypeDefinition.newBuilder()
                .setStructDefId(StructDefId.newBuilder().setName(structDefName))
                .build();
    }

    private void initializeAsStructDef(LHStructDefType structClass) {
        this.typeDef = structClass.getTypeDefinition();
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
        WfRunVariableImpl out = this.clone();
        out.setJsonPath(path);
        return out;
    }

    @Override
    public WfRunVariableImpl get(String field) {
        if (jsonPath != null) {
            throw new LHMisconfigurationException("Cannot use jsonPath() and get() on same var!");
        }
        if (typeDef.getDefinedTypeCase() == DefinedTypeCase.PRIMITIVE_TYPE
                && (typeDef.getPrimitiveType() != VariableType.JSON_OBJ
                        && typeDef.getPrimitiveType() != VariableType.JSON_ARR)) {
            throw new LHMisconfigurationException("Can only use get() on JSON_OBJ, JSON_ARR, or Struct variables");
        }
        WfRunVariableImpl out = this.clone();
        out.getLhPath().add(Selector.newBuilder().setKey(field).build());
        return out;
    }

    @Override
    public WfRunVariableImpl get(int index) {
        if (jsonPath != null) {
            throw new LHMisconfigurationException("Cannot use jsonPath() and get() on same var!");
        }
        if (typeDef.getDefinedTypeCase() == DefinedTypeCase.PRIMITIVE_TYPE
                && (typeDef.getPrimitiveType() != VariableType.JSON_OBJ
                        && typeDef.getPrimitiveType() != VariableType.JSON_ARR)) {
            throw new LHMisconfigurationException("Can only use get() on JSON_OBJ, JSON_ARR, or Struct variables");
        }
        WfRunVariableImpl out = this.clone();
        out.getLhPath().add(Selector.newBuilder().setIndex(index).build());
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

    public WfRunVariableImpl clone() {
        WfRunVariableImpl out = new WfRunVariableImpl(this.getName(), this.getParent());
        out.setDefaultValue(this.getDefaultValue());
        out.setRequired(this.isRequired());
        out.setSearchable(this.isSearchable());
        out.setJsonIndexes(new ArrayList<>(this.getJsonIndexes()));
        out.setAccessLevel(this.getAccessLevel());
        out.setTypeDef(this.getTypeDef());

        if (jsonPath != null) {
            out.setJsonPath(this.getJsonPath());
        } else if (lhPath != null) {
            out.setLhPath(new ArrayList<>(lhPath));
        }

        return out;
    }
}
