package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class WfRunIdModel extends CoreObjectId<WfRunId, WfRun, WfRunModel> {
    private String id;
    private WfRunIdModel parentWfRunId;

    public WfRunIdModel() {}

    public WfRunIdModel(String id) {
        this.id = id;
    }

    public WfRunIdModel(String id, WfRunIdModel parentWfRunId) {
        this.id = id;
        this.parentWfRunId = parentWfRunId;
    }

    public String getId() {
        return id;
    }

    @Override
    public Class<WfRunId> getProtoBaseClass() {
        return WfRunId.class;
    }

    @Override
    public Optional<WfRunIdModel> getGroupingWfRunId() {
        return Optional.of(this);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        WfRunId p = (WfRunId) proto;
        id = p.getId();
        if (p.hasParentWfRunId()) {
            parentWfRunId = LHSerializable.fromProto(p.getParentWfRunId(), WfRunIdModel.class, context);
        }
    }

    @Override
    public Optional<String> getPartitionKey() {
        return parentWfRunId == null ? Optional.of(id) : parentWfRunId.getPartitionKey();
    }

    @Override
    public WfRunId.Builder toProto() {
        WfRunId.Builder out = WfRunId.newBuilder().setId(id);
        if (parentWfRunId != null) out.setParentWfRunId(parentWfRunId.toProto());
        return out;
    }

    @Override
    public String toString() {
        if (parentWfRunId != null) {
            return parentWfRunId + "_" + id;
        }
        return id;
    }

    @Override
    public void initFromString(String storeKey) {
        if (storeKey.contains("_")) {
            // then it's a composite id
            String[] splits = storeKey.split("_");
            this.id = splits[splits.length - 1];
            this.parentWfRunId = (WfRunIdModel)
                    ObjectIdModel.fromString(storeKey.substring(0, storeKey.lastIndexOf("_")), WfRunIdModel.class);
        } else {
            id = storeKey;
        }
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.WF_RUN;
    }

    public WfRunIdModel getParentWfRunId() {
        return this.parentWfRunId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof WfRunIdModel)) return false;
        final WfRunIdModel other = (WfRunIdModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$parentWfRunId = this.getParentWfRunId();
        final Object other$parentWfRunId = other.getParentWfRunId();
        if (this$parentWfRunId == null ? other$parentWfRunId != null : !this$parentWfRunId.equals(other$parentWfRunId))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof WfRunIdModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $parentWfRunId = this.getParentWfRunId();
        result = result * PRIME + ($parentWfRunId == null ? 43 : $parentWfRunId.hashCode());
        return result;
    }

    public void setParentWfRunId(final WfRunIdModel parentWfRunId) {
        this.parentWfRunId = parentWfRunId;
    }
}
