package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.VariableIdPb;
import io.littlehorse.jlib.common.proto.VariablePb;

public class VariableId extends ObjectId<VariableIdPb, VariablePb, Variable> {

    public String wfRunId;
    public int threadRunNumber;
    public String name;

    public Class<VariableIdPb> getProtoBaseClass() {
        return VariableIdPb.class;
    }

    public VariableId() {}

    public VariableId(String wfRunId, int threadRunNumber, String name) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.name = name;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        VariableIdPb p = (VariableIdPb) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        name = p.getName();
    }

    public VariableIdPb.Builder toProto() {
        VariableIdPb.Builder out = VariableIdPb
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

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.VARIABLE;
    }
}
