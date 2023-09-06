package io.littlehorse.common.model.getable.global.wfspec.node;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.WaitForThreadsNode.ThreadToWaitFor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadToWaitForModel extends LHSerializable<ThreadToWaitFor> {

    private VariableAssignmentModel threadRunNumber;

    public Class<ThreadToWaitFor> getProtoBaseClass() {
        return ThreadToWaitFor.class;
    }

    public void initFrom(Message proto) {
        ThreadToWaitFor p = (ThreadToWaitFor) proto;
        threadRunNumber = VariableAssignmentModel.fromProto(p.getThreadRunNumber());
    }

    public ThreadToWaitFor.Builder toProto() {
        ThreadToWaitFor.Builder out = ThreadToWaitFor.newBuilder();
        out.setThreadRunNumber(threadRunNumber.toProto());
        return out;
    }
}
