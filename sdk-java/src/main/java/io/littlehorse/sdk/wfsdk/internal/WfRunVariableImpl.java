package io.littlehorse.sdk.wfsdk.internal;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.exception.LHMisconfigurationException;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.IndexTypePb;
import io.littlehorse.sdk.common.proto.JsonIndexPb;
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
    private Object typeOrDefaultVal;
    private IndexTypePb indexTypePb;
    private List<JsonIndexPb> jsonIndexPbs = new ArrayList<>();

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
                VariableValuePb defaultVal = LHLibUtil.objToVarVal(typeOrDefaultVal);
                this.type = defaultVal.getType();
            } catch (LHSerdeError e) {
                throw new RuntimeException(e); //TODO ???
            }
        }
    }

    public WfRunVariableImpl jsonPath(String path) {
        if (jsonPath != null) {
            throw new RuntimeException("Cannot use jsonpath() twice on same var!");
        }
        WfRunVariableImpl out = new WfRunVariableImpl(name, typeOrDefaultVal);
        out.jsonPath = path;
        return out;
    }

    @Override
    public WfRunVariable withIndex(@NonNull IndexTypePb indexType) {
        this.indexTypePb = indexType;
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
        this.jsonIndexPbs.add(
                JsonIndexPb
                    .newBuilder()
                    .setIndexType(indexType)
                    .setPath(jsonPath)
                    .build()
            );
        return this;
    }
}
