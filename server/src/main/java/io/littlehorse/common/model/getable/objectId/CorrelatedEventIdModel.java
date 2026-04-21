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

    public String getKey() {
        return this.key;
    }

    public ExternalEventDefIdModel getExternalEventDefId() {
        return this.externalEventDefId;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public void setExternalEventDefId(final ExternalEventDefIdModel externalEventDefId) {
        this.externalEventDefId = externalEventDefId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CorrelatedEventIdModel)) return false;
        final CorrelatedEventIdModel other = (CorrelatedEventIdModel) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$key = this.getKey();
        final Object other$key = other.getKey();
        if (this$key == null ? other$key != null : !this$key.equals(other$key)) return false;
        final Object this$externalEventDefId = this.getExternalEventDefId();
        final Object other$externalEventDefId = other.getExternalEventDefId();
        if (this$externalEventDefId == null
                ? other$externalEventDefId != null
                : !this$externalEventDefId.equals(other$externalEventDefId)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof CorrelatedEventIdModel;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $key = this.getKey();
        result = result * PRIME + ($key == null ? 43 : $key.hashCode());
        final Object $externalEventDefId = this.getExternalEventDefId();
        result = result * PRIME + ($externalEventDefId == null ? 43 : $externalEventDefId.hashCode());
        return result;
    }
}
