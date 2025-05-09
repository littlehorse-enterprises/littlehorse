package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.WfRunUpdateRecord;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class WfRunUpdateRecordModel extends LHSerializable<WfRunUpdateRecord> implements GenericOutputTopicRecordModel {

    private WfRunModel getable;

    @Override
    public Class<WfRunUpdateRecord> getProtoBaseClass() {
        return WfRunUpdateRecord.class;
    }

    @Override
    public WfRunUpdateRecord.Builder toProto() {
        WfRunUpdateRecord.Builder result = WfRunUpdateRecord.newBuilder();

        result.setWfRun(getable.toProto());

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        WfRunUpdateRecord p = (WfRunUpdateRecord) proto;

        this.getable = LHSerializable.fromProto(p.getWfRun(), WfRunModel.class, ignored);
    }

    @Override
    public String getPartitionKey() {
        return getable.getPartitionKey().get();
    }
}
