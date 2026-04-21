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

    public VariableDefModel getVarDef() {
        return this.varDef;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isSearchable() {
        return this.searchable;
    }

    public List<JsonIndexModel> getJsonIndexes() {
        return this.jsonIndexes;
    }

    public WfRunVariableAccessLevel getAccessLevel() {
        return this.accessLevel;
    }

    public void setVarDef(final VariableDefModel varDef) {
        this.varDef = varDef;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public void setSearchable(final boolean searchable) {
        this.searchable = searchable;
    }

    public void setJsonIndexes(final List<JsonIndexModel> jsonIndexes) {
        this.jsonIndexes = jsonIndexes;
    }

    public void setAccessLevel(final WfRunVariableAccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ThreadVarDefModel)) return false;
        final ThreadVarDefModel other = (ThreadVarDefModel) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isRequired() != other.isRequired()) return false;
        if (this.isSearchable() != other.isSearchable()) return false;
        final Object this$varDef = this.getVarDef();
        final Object other$varDef = other.getVarDef();
        if (this$varDef == null ? other$varDef != null : !this$varDef.equals(other$varDef)) return false;
        final Object this$jsonIndexes = this.getJsonIndexes();
        final Object other$jsonIndexes = other.getJsonIndexes();
        if (this$jsonIndexes == null ? other$jsonIndexes != null : !this$jsonIndexes.equals(other$jsonIndexes))
            return false;
        final Object this$accessLevel = this.getAccessLevel();
        final Object other$accessLevel = other.getAccessLevel();
        if (this$accessLevel == null ? other$accessLevel != null : !this$accessLevel.equals(other$accessLevel))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ThreadVarDefModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isRequired() ? 79 : 97);
        result = result * PRIME + (this.isSearchable() ? 79 : 97);
        final Object $varDef = this.getVarDef();
        result = result * PRIME + ($varDef == null ? 43 : $varDef.hashCode());
        final Object $jsonIndexes = this.getJsonIndexes();
        result = result * PRIME + ($jsonIndexes == null ? 43 : $jsonIndexes.hashCode());
        final Object $accessLevel = this.getAccessLevel();
        result = result * PRIME + ($accessLevel == null ? 43 : $accessLevel.hashCode());
        return result;
    }
}
