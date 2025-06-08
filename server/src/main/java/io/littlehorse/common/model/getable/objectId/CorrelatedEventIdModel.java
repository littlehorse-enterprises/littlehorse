package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.externalevent.CorrelatedEventModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.CorrelatedEvent;
import io.littlehorse.sdk.common.proto.CorrelatedEventId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CorrelatedEventIdModel extends CoreObjectId<CorrelatedEventId, CorrelatedEvent, CorrelatedEventModel> {

    private String key;
    private ExternalEventDefIdModel externalEventDefId;

    public CorrelatedEventIdModel() {}

    public CorrelatedEventIdModel(String key, ExternalEventDefIdModel extEvtDefId) {
        this.key = key;
        this.externalEventDefId = extEvtDefId;
    }

    @Override
    public Class<CorrelatedEventId> getProtoBaseClass() {
        return CorrelatedEventId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(key);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        CorrelatedEventId p = (CorrelatedEventId) proto;
        key = p.getKey();
        externalEventDefId =
                LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class, context);
    }

    @Override
    public CorrelatedEventId.Builder toProto() {
        CorrelatedEventId.Builder out =
                CorrelatedEventId.newBuilder().setKey(key).setExternalEventDefId(externalEventDefId.toProto());
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(key, externalEventDefId.toString());
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        key = split[0];
        externalEventDefId =
                (ExternalEventDefIdModel) ObjectIdModel.fromString(split[1], ExternalEventDefIdModel.class);
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.CORRELATED_EVENT;
    }

    public String getExternalEventDefName() {
        return externalEventDefId.getName();
    }
}
