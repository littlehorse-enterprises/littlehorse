package io.littlehorse.common.model.getable.global.wfspec.thread;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.variable.JsonIndexModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableDefModel;
import io.littlehorse.sdk.common.proto.JsonIndex;
import io.littlehorse.sdk.common.proto.ThreadVarDef;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ThreadVarDefModel extends LHSerializable<ThreadVarDef> {

    private VariableDefModel varDef;
    private boolean required;
    private boolean searchable;
    private List<JsonIndexModel> jsonIndexes;
    private WfRunVariableAccessLevel accessLevel;

    public ThreadVarDefModel() {
        this.jsonIndexes = new ArrayList<>();
    }

    // For unit testing
    public ThreadVarDefModel(
            VariableDefModel varDef, boolean searchable, boolean required, WfRunVariableAccessLevel accessLevel) {
        this();
        this.varDef = varDef;
        this.searchable = searchable;
        this.required = required;
        this.accessLevel = accessLevel;
    }

    // For unit testing
    public ThreadVarDefModel(
            VariableDefModel varDef,
            List<JsonIndexModel> jsonIndexes,
            boolean required,
            WfRunVariableAccessLevel accessLevel) {
        this.varDef = varDef;
        this.required = required;
        this.searchable = true;
        this.jsonIndexes = jsonIndexes;
        this.accessLevel = accessLevel;
    }

    @Override
    public Class<ThreadVarDef> getProtoBaseClass() {
        return ThreadVarDef.class;
    }

    @Override
    public ThreadVarDef.Builder toProto() {
        ThreadVarDef.Builder out = ThreadVarDef.newBuilder()
                .setRequired(required)
                .setSearchable(searchable)
                .setVarDef(varDef.toProto())
                .setAccessLevel(accessLevel);

        for (JsonIndexModel jim : jsonIndexes) {
            out.addJsonIndexes(jim.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext executionContext) {
        ThreadVarDef p = (ThreadVarDef) proto;
        varDef = LHSerializable.fromProto(p.getVarDef(), VariableDefModel.class, executionContext);
        searchable = p.getSearchable();
        required = p.getRequired();
        accessLevel = p.getAccessLevel();
        for (JsonIndex ji : p.getJsonIndexesList()) {
            jsonIndexes.add(LHSerializable.fromProto(ji, JsonIndexModel.class, executionContext));
        }
    }

    /**
     * Returns true if the ThreadVarDef has an index on the provided specific JsonPath key.
     * @param jsonFieldKey is the specific json path.
     * @return true if an index is specified on the jsonFieldKey.
     */
    public boolean isSearchableOn(String jsonFieldKey) {
        return jsonIndexes.stream().anyMatch(jsonIdx -> jsonIdx.getFieldPath().equals(jsonFieldKey));
    }
}
