package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.IndexTypePb;
import io.littlehorse.sdk.common.proto.JsonIndexPb;
import io.littlehorse.sdk.common.proto.VariableDefPb;
import io.littlehorse.sdk.common.proto.VariableTypePb;
import io.littlehorse.sdk.common.proto.VariableValuePb;
import io.littlehorse.sdk.wfsdk.WfRunVariable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class WfRunVariableImpl implements WfRunVariable {

    public String name;
    public VariableTypePb type;
    public String jsonPath;
    private VariableValuePb defaultValue;
    private Object typeOrDefaultVal;
    private IndexTypePb indexType;
    private List<JsonIndexPb> jsonIndexes = new ArrayList<>();

    public WfRunVariableImpl(String name, Object typeOrDefaultVal) {
        this.name = name;
        this.typeOrDefaultVal = typeOrDefaultVal;
        initializeType();
    }

    private void initializeType() {
        if (typeOrDefaultVal instanceof VariableTypePb) {
            this.type = (VariableTypePb) typeOrDefaultVal;
        } else {
            try {
                this.defaultValue = LHLibUtil.objToVarVal(typeOrDefaultVal);
                this.type = defaultValue.getType();
            } catch (LHSerdeError e) {
                throw new IllegalArgumentException(
                    "Was unable to convert provided default value to LH Variable Type",
                    e
                );
            }
        }
    }

    public WfRunVariableImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new LHMisconfigurationException(
                "Cannot use jsonpath() twice on same var!"
            );
        }
        WfRunVariableImpl out = new WfRunVariableImpl(name, typeOrDefaultVal);
        out.jsonPath = path;
        return out;
    }

    @Override
    public WfRunVariable withIndex(@NonNull IndexTypePb indexType) {
        this.indexType = indexType;
        return this;
    }

    @Override
    public WfRunVariable withJsonIndex(
        @NonNull String jsonPath,
        @NonNull IndexTypePb indexType
    ) {
        if (!jsonPath.startsWith("$.")) {
            throw new LHMisconfigurationException(
                String.format("Invalid JsonPath: %s", jsonPath)
            );
        }
        if (!type.equals(VariableTypePb.JSON_OBJ)) {
            throw new LHMisconfigurationException(
                String.format("Non-Json %s varibale contains jsonIndex", name)
            );
        }
        this.jsonIndexes.add(
                JsonIndexPb
                    .newBuilder()
                    .setIndexType(indexType)
                    .setPath(jsonPath)
                    .build()
            );
        return this;
    }

    public VariableDefPb getSpec() {
        VariableDefPb.Builder out = VariableDefPb.newBuilder();
        out.setType(this.getType());
        out.setName(this.getName());

        if (this.getIndexType() != null) {
            out.setIndexType(this.getIndexType());
        }

        for (JsonIndexPb jsonIndex : this.getJsonIndexes()) {
            out.addJsonIndexes(jsonIndex);
        }

        if (this.defaultValue != null) {
            out.setDefaultValue(defaultValue);
        }

        return out.build();
    }
}
