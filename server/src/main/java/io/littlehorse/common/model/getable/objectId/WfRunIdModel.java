package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.sdk.common.proto.WfRunId;
import java.util.Optional;
import lombok.Getter;

@Getter
public class WfRunIdModel extends CoreObjectId<WfRunId, WfRun, WfRunModel> {

    private String id;

    public WfRunIdModel() {}

    public WfRunIdModel(String id) {
        this.id = id;
    }

    @Override
    public Class<WfRunId> getProtoBaseClass() {
        return WfRunId.class;
    }

    @Override
    public void initFrom(Message proto) {
        WfRunId p = (WfRunId) proto;
        id = p.getId();
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(id);
    }

    @Override
    public WfRunId.Builder toProto() {
        WfRunId.Builder out = WfRunId.newBuilder();
        out.setId(id);
        return out;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public void initFromString(String storeKey) {
        id = storeKey;
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.WF_RUN;
    }
}
