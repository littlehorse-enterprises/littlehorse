package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.wfrun.ScheduledWfRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ScheduledWfRun;
import io.littlehorse.sdk.common.proto.ScheduledWfRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class ScheduledWfRunIdModel extends CoreObjectId<ScheduledWfRunId, ScheduledWfRun, ScheduledWfRunModel> {

    private String id;

    public ScheduledWfRunIdModel() {}

    public ScheduledWfRunIdModel(String id) {
        this.id = id;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ScheduledWfRunId p = (ScheduledWfRunId) proto;
        this.id = p.getId();
    }

    @Override
    public ScheduledWfRunId.Builder toProto() {
        return ScheduledWfRunId.newBuilder().setId(id);
    }

    @Override
    public Class<ScheduledWfRunId> getProtoBaseClass() {
        return ScheduledWfRunId.class;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void initFromString(String storeKey) {
        this.id = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.SCHEDULED_WF_RUN;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(id);
    }
}
