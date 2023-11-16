package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;

public class ExternalEventIdModel extends CoreObjectId<ExternalEventId, ExternalEvent, ExternalEventModel> {

    public String wfRunId;
    public String externalEventDefName;
    public String guid;

    public ExternalEventIdModel() {}

    public ExternalEventIdModel(String wfRunId, String externalEventDefName, String guid) {
        this.wfRunId = wfRunId;
        this.externalEventDefName = externalEventDefName;
        this.guid = guid;
    }

    @Override
    public Class<ExternalEventId> getProtoBaseClass() {
        return ExternalEventId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(wfRunId);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ExternalEventId p = (ExternalEventId) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
    }

    @Override
    public ExternalEventId.Builder toProto() {
        ExternalEventId.Builder out = ExternalEventId.newBuilder()
                .setWfRunId(wfRunId)
                .setExternalEventDefName(externalEventDefName)
                .setGuid(guid);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId, externalEventDefName, guid);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        externalEventDefName = split[1];
        guid = split[2];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.EXTERNAL_EVENT;
    }
}
