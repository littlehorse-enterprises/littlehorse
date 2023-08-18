package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.wfrun.ExternalEventModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventId;

public class ExternalEventIdModel extends ObjectId<ExternalEventId, ExternalEvent, ExternalEventModel> {

    public String wfRunId;
    public String externalEventDefName;
    public String guid;

    public ExternalEventIdModel() {}

    public ExternalEventIdModel(String wfRunId, String externalEventDefName, String guid) {
        this.wfRunId = wfRunId;
        this.externalEventDefName = externalEventDefName;
        this.guid = guid;
    }

    public Class<ExternalEventId> getProtoBaseClass() {
        return ExternalEventId.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        ExternalEventId p = (ExternalEventId) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
    }

    public ExternalEventId.Builder toProto() {
        ExternalEventId.Builder out = ExternalEventId.newBuilder()
                .setWfRunId(wfRunId)
                .setExternalEventDefName(externalEventDefName)
                .setGuid(guid);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(wfRunId, externalEventDefName, guid);
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        externalEventDefName = split[1];
        guid = split[2];
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.EXTERNAL_EVENT;
    }
}
