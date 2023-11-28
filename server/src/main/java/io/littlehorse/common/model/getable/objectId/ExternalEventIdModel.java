package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventId;
import java.util.Optional;
import lombok.Getter;

@Getter
public class ExternalEventIdModel extends CoreObjectId<ExternalEventId, ExternalEvent, ExternalEventModel> {

    private WfRunIdModel wfRunId;
    private ExternalEventDefIdModel externalEventDefId;
    private String guid;

    public ExternalEventIdModel() {}

    public ExternalEventIdModel(WfRunIdModel wfRunId, ExternalEventDefIdModel externalEventDefId, String guid) {
        this.wfRunId = wfRunId;
        this.externalEventDefId = externalEventDefId;
        this.guid = guid;
    }

    public ExternalEventIdModel(String wfRunId, String externalEventDefName, String guid) {
        this(new WfRunIdModel(wfRunId), new ExternalEventDefIdModel(externalEventDefName), guid);
    }

    @Override
    public Class<ExternalEventId> getProtoBaseClass() {
        return ExternalEventId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return wfRunId.getPartitionKey();
    }

    @Override
    public void initFrom(Message proto) {
        ExternalEventId p = (ExternalEventId) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class);
        guid = p.getGuid();
        externalEventDefId = LHSerializable.fromProto(p.getExternalEventDefId(), ExternalEventDefIdModel.class);
    }

    @Override
    public ExternalEventId.Builder toProto() {
        ExternalEventId.Builder out = ExternalEventId.newBuilder()
                .setWfRunId(wfRunId.toProto())
                .setExternalEventDefId(externalEventDefId.toProto())
                .setGuid(guid);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId.toString(), externalEventDefId.toString(), guid);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = (WfRunIdModel) ObjectIdModel.fromString(split[0], WfRunIdModel.class);
        externalEventDefId =
                (ExternalEventDefIdModel) ObjectIdModel.fromString(split[1], ExternalEventDefIdModel.class);
        guid = split[2];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.EXTERNAL_EVENT;
    }

    public String getExternalEventDefName() {
        return externalEventDefId.getName();
    }
}
