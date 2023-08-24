package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;
import java.util.Optional;

public class VariableIdModel extends CoreObjectId<VariableId, Variable, VariableModel> {

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

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(wfRunId);
    }

    @Override
    public void initFrom(Message proto) {
        VariableId p = (VariableId) proto;
        wfRunId = p.getWfRunId();
        threadRunNumber = p.getThreadRunNumber();
        name = p.getName();
    }

    @Override
    public VariableId.Builder toProto() {
        VariableId.Builder out = VariableId.newBuilder()
                .setWfRunId(wfRunId)
                .setThreadRunNumber(threadRunNumber)
                .setName(name);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId, String.valueOf(threadRunNumber), name);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        threadRunNumber = Integer.valueOf(split[1]);
        name = split[2];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.VARIABLE;
    }
}
