package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.wfrun.VariableModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;

public class VariableIdModel extends ObjectId<VariableId, Variable, VariableModel> {

    public String wfRunId;
    public int threadRunNumber;
    public String name;

    public Class<VariableId> getProtoBaseClass() {
        return VariableId.class;
    }

    public VariableIdModel() {}

    public VariableIdModel(String wfRunId, int threadRunNumber, String name) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.name = name;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        VariableId p = (VariableId) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        name = p.getName();
    }

    public VariableId.Builder toProto() {
        VariableId.Builder out = VariableId
            .newBuilder()
            .setWfRunId(wfRunId)
            .setThreadRunNumber(threadRunNumber)
            .setName(name);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(wfRunId, String.valueOf(threadRunNumber), name);
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        threadRunNumber = Integer.valueOf(split[1]);
        name = split[2];
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.VARIABLE;
    }
}
