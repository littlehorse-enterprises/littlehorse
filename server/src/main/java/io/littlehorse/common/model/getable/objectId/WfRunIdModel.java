package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import lombok.Getter;

@Getter
public class WfRunIdModel extends ObjectIdModel<WfRunId, WfRun, WfRunModel> {

    private String id;

    public WfRunIdModel() {}

    public WfRunIdModel(String id) {
        this.id = id;
    }

    public Class<WfRunId> getProtoBaseClass() {
        return WfRunId.class;
    }

    public void initFrom(Message proto) {
        WfRunId p = (WfRunId) proto;
        id = p.getId();
    }

    public String getPartitionKey() {
        return id;
    }

    public WfRunId.Builder toProto() {
        WfRunId.Builder out = WfRunId.newBuilder();
        out.setId(id);
        return out;
    }

    public String getStoreKey() {
        return id;
    }

    public void initFrom(String storeKey) {
        id = storeKey;
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.WF_RUN;
    }
}
