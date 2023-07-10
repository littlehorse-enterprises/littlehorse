package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.sdk.common.proto.WfRunIdPb;
import io.littlehorse.sdk.common.proto.WfRunPb;

public class WfRunId extends ObjectId<WfRunIdPb, WfRunPb, WfRun> {

    public String id;

    public WfRunId() {}

    public WfRunId(String id) {
        this.id = id;
    }

    public Class<WfRunIdPb> getProtoBaseClass() {
        return WfRunIdPb.class;
    }

    public void initFrom(Message proto) {
        WfRunIdPb p = (WfRunIdPb) proto;
        id = p.getId();
    }

    public String getPartitionKey() {
        return id;
    }

    public WfRunIdPb.Builder toProto() {
        WfRunIdPb.Builder out = WfRunIdPb.newBuilder();
        out.setId(id);
        return out;
    }

    public String getStoreKey() {
        return id;
    }

    public void initFrom(String storeKey) {
        id = storeKey;
    }

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.WF_RUN;
    }
}
