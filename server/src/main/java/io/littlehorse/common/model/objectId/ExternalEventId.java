package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.ExternalEventIdPb;
import io.littlehorse.jlib.common.proto.ExternalEventPb;

public class ExternalEventId
    extends ObjectId<ExternalEventIdPb, ExternalEventPb, ExternalEvent> {

    public String wfRunId;
    public String externalEventDefName;
    public String guid;

    public ExternalEventId() {}

    public ExternalEventId(String wfRunId, String externalEventDefName, String guid) {
        this.wfRunId = wfRunId;
        this.externalEventDefName = externalEventDefName;
        this.guid = guid;
    }

    public Class<ExternalEventIdPb> getProtoBaseClass() {
        return ExternalEventIdPb.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        ExternalEventIdPb p = (ExternalEventIdPb) proto;
        wfRunId = p.getWfRunId();
        externalEventDefName = p.getExternalEventDefName();
        guid = p.getGuid();
    }

    public ExternalEventIdPb.Builder toProto() {
        ExternalEventIdPb.Builder out = ExternalEventIdPb
            .newBuilder()
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

    public GETableClassEnumPb getType() {
        return GETableClassEnumPb.EXTERNAL_EVENT;
    }
}
