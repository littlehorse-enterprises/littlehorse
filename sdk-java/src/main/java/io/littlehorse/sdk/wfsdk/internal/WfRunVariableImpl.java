package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.IndexType;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.VariableDef;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;

@Getter
class WfRunVariableImpl implements WfRunVariable {

    public String name;
    public VariableType type;
    public String jsonPath;
    private VariableValue defaultValue;
    private Object typeOrDefaultVal;
    private IndexType indexType;
    private List<JsonIndex> jsonIndexes = new ArrayList<>();

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
    public WfRunVariable withIndex(@NonNull IndexType indexType) {
        this.indexType = indexType;
        return this;
    }

    @Override
    public WfRunVariable withJsonIndex(@NonNull String jsonPath, @NonNull IndexType indexType) {
        if (!jsonPath.startsWith("$.")) {
            throw new LHMisconfigurationException(String.format("Invalid JsonPath: %s", jsonPath));
        }
        if (!type.equals(VariableType.JSON_OBJ)) {
            throw new LHMisconfigurationException(String.format("Non-Json %s variable contains jsonIndex", name));
        }
        this.jsonIndexes.add(
                JsonIndex.newBuilder().setIndexType(indexType).setPath(jsonPath).build());
        return this;
    }

    public VariableDef getSpec() {
        VariableDef.Builder out = VariableDef.newBuilder();
        out.setType(this.getType());
        out.setName(this.getName());

        if (this.getIndexType() != null) {
            out.setIndexType(this.getIndexType());
        }

        for (JsonIndex jsonIndex : this.getJsonIndexes()) {
            out.addJsonIndexes(jsonIndex);
        }

        if (this.defaultValue != null) {
            out.setDefaultValue(defaultValue);
        }

        return out.build();
    }
}
