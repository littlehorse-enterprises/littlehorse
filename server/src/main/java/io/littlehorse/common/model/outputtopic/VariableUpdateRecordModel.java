package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.sdk.common.proto.VariableUpdateRecord;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class VariableUpdateRecordModel extends LHSerializable<VariableUpdateRecord>
        implements GenericOutputTopicRecordModel {

    private VariableModel getable;

    @Override
    public Class<VariableUpdateRecord> getProtoBaseClass() {
        return VariableUpdateRecord.class;
    }

    @Override
    public VariableUpdateRecord.Builder toProto() {
        VariableUpdateRecord.Builder result = VariableUpdateRecord.newBuilder();

        result.setVariable(getable.toProto());

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        VariableUpdateRecord p = (VariableUpdateRecord) proto;

        this.getable = LHSerializable.fromProto(p.getVariable(), VariableModel.class, ignored);
    }

    @Override
    public String getPartitionKey() {
        return getable.getPartitionKey().get();
    }
}
