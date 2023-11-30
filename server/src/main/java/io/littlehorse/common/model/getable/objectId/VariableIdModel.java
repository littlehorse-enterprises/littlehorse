package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
public class VariableIdModel extends CoreObjectId<VariableId, Variable, VariableModel> {

    private WfRunIdModel wfRunId;
    private int threadRunNumber;

    @Setter // for unit test
    private String name;

    public Class<VariableId> getProtoBaseClass() {
        return VariableId.class;
    }

    public VariableIdModel() {}

    public VariableIdModel(WfRunIdModel wfRunId, int threadRunNumber, String name) {
        this.wfRunId = wfRunId;
        this.threadRunNumber = threadRunNumber;
        this.name = name;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return wfRunId.getPartitionKey();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        VariableId p = (VariableId) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        threadRunNumber = p.getThreadRunNumber();
        name = p.getName();
    }

    @Override
    public VariableId.Builder toProto() {
        VariableId.Builder out = VariableId.newBuilder()
                .setWfRunId(wfRunId.toProto())
                .setThreadRunNumber(threadRunNumber)
                .setName(name);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId.toString(), String.valueOf(threadRunNumber), name);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = (WfRunIdModel) ObjectIdModel.fromString(split[0], WfRunIdModel.class);
        threadRunNumber = Integer.valueOf(split[1]);
        name = split[2];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.VARIABLE;
    }
}
