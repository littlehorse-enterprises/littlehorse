package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.WfRunGroupedObjectId;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(callSuper = false)
public class WfRunIdModel extends WfRunGroupedObjectId<WfRunId, WfRun, WfRunModel> {

    private String id;

    @Setter
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
    public WfRunIdModel getGroupingWfRunId() {
        return this;
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
}
