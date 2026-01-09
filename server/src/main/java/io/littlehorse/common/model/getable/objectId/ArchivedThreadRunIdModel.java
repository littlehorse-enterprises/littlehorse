package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.wfrun.ArchivedThreadRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.ArchivedThreadRun;
import io.littlehorse.sdk.common.proto.ArchivedThreadRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class ArchivedThreadRunIdModel
        extends CoreObjectId<ArchivedThreadRunId, ArchivedThreadRun, ArchivedThreadRunModel> {

    private WfRunIdModel wfRunId;
    private int threadRunNumber;

    public ArchivedThreadRunIdModel(WfRunIdModel wfRunId, int threadRunNumber) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return wfRunId.getPartitionKey();
    }

    @Override
    public String toString() {
        return wfRunId + "/" + Integer.toString(threadRunNumber);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = (WfRunIdModel) ObjectIdModel.fromString(split[0], WfRunIdModel.class);
        threadRunNumber = Integer.valueOf(split[1]);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.ARCHIVED_THREAD_RUN;
    }

    @Override
    public ArchivedThreadRunId.Builder toProto() {
        ArchivedThreadRunId.Builder out = ArchivedThreadRunId.newBuilder();
        out.setWfRunId(this.wfRunId.toProto());
        out.setThreadRunNumber(this.threadRunNumber);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        ArchivedThreadRunId p = (ArchivedThreadRunId) proto;
        this.wfRunId = WfRunIdModel.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        this.threadRunNumber = p.getThreadRunNumber();
    }

    @Override
    public Class<ArchivedThreadRunId> getProtoBaseClass() {
        return ArchivedThreadRunId.class;
    }
}
