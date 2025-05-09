package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.sdk.common.proto.ExternalEventUpdateRecord;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ExternalEventUpdateRecordModel extends LHSerializable<ExternalEventUpdateRecord>
        implements GenericOutputTopicRecordModel {

    private ExternalEventModel getable;

    @Override
    public Class<ExternalEventUpdateRecord> getProtoBaseClass() {
        return ExternalEventUpdateRecord.class;
    }

    @Override
    public ExternalEventUpdateRecord.Builder toProto() {
        ExternalEventUpdateRecord.Builder result = ExternalEventUpdateRecord.newBuilder();

        result.setExternalEvent(getable.toProto());

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        ExternalEventUpdateRecord p = (ExternalEventUpdateRecord) proto;

        this.getable = LHSerializable.fromProto(p.getExternalEvent(), ExternalEventModel.class, ignored);
    }

    @Override
    public String getPartitionKey() {
        return getable.getPartitionKey().get();
    }
}
