package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
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
    private Object typeOrDefaultVal;
    private List<JsonIndex> jsonIndexes = new ArrayList<>();

    public String jsonPath;

    public WfRunVariableImpl(String name, Object typeOrDefaultVal) {
        this.name = name;
        this.typeOrDefaultVal = typeOrDefaultVal;
        initializeType();
    }

    private void initializeType() {
        if (typeOrDefaultVal instanceof VariableType) {
            this.type = (VariableType) typeOrDefaultVal;
        } else {
            try {
                this.defaultValue = LHLibUtil.objToVarVal(typeOrDefaultVal);
                this.type = defaultValue.getType();
            } catch (LHSerdeError e) {
                throw new IllegalArgumentException(
                        "Was unable to convert provided default value to LH Variable Type", e);
            }
        }
    }

    @Override
    public WfRunVariableImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new LHMisconfigurationException("Cannot use jsonpath() twice on same var!");
        }
        if (!type.equals(VariableType.JSON_OBJ) && !type.equals(VariableType.JSON_ARR)) {
            throw new LHMisconfigurationException(String.format("JsonPath not allowed in a %s variable", type.name()));
        }
        WfRunVariableImpl out = new WfRunVariableImpl(name, typeOrDefaultVal);
        out.jsonPath = path;
        return out;
    }

    @Override
    public WfRunVariable searchable() {
        this.searchable = true;
        return this;
    }

    @Override
    public WfRunVariable required() {
        this.required = true;
        return this;
    }

    @Override
    public WfRunVariable searchableOn(String fieldPath, VariableType fieldType) {
        if (!fieldPath.startsWith("$.")) {
            throw new LHMisconfigurationException(String.format("Invalid JsonPath: %s", fieldPath));
        }
        if (!type.equals(VariableType.JSON_OBJ)) {
            throw new LHMisconfigurationException(String.format("Non-Json %s variable contains jsonIndex", name));
        }
        this.jsonIndexes.add(JsonIndex.newBuilder()
                .setFieldPath(fieldPath)
                .setFieldType(fieldType)
                .build());
        return this;
    }

    public ThreadVarDef getSpec() {
        VariableDef.Builder varDef =
                VariableDef.newBuilder().setType(this.getType()).setName(this.getName());

        if (this.defaultValue != null) {
            varDef.setDefaultValue(defaultValue);
        }

        return ThreadVarDef.newBuilder()
                .setVarDef(varDef)
                .setRequired(required)
                .setSearchable(searchable)
                .addAllJsonIndexes(jsonIndexes)
                .build();
    }
}
