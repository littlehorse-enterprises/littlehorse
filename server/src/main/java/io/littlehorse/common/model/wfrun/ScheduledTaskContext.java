package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ScheduledTaskContextPb;
import io.littlehorse.sdk.common.proto.VarNameAndValPb;
import java.util.ArrayList;
import java.util.List;

public class ScheduledTaskContext extends LHSerializable<ScheduledTaskContextPb> {

    private List<VarNameAndVal> contextVariables = new ArrayList<>();

    @Override
    public ScheduledTaskContextPb.Builder toProto() {
        List<VarNameAndValPb> variableValuePbs = contextVariables
            .stream()
            .map(VarNameAndVal::toProto)
            .map(VarNameAndValPb.Builder::build)
            .toList();
        return ScheduledTaskContextPb.newBuilder().addAllVariables(variableValuePbs);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        ScheduledTaskContextPb taskContext = (ScheduledTaskContextPb) proto;
        contextVariables = new ArrayList<>();
        for (VarNameAndValPb varNameAndValPb : taskContext.getVariablesList()) {
            contextVariables.add(
                LHSerializable.fromProto(varNameAndValPb, VarNameAndVal.class)
            );
        }
    }

    public void addVariable(VarNameAndVal variable) {
        contextVariables.add(variable);
    }

    @Override
    public Class<ScheduledTaskContextPb> getProtoBaseClass() {
        return ScheduledTaskContextPb.class;
    }
}
