package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.WaitForThreadsNodePb.ThreadToWaitForPb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThreadToWaitFor extends LHSerializable<ThreadToWaitForPb> {

    private VariableAssignmentModel threadRunNumber;

    public Class<ThreadToWaitForPb> getProtoBaseClass() {
        return ThreadToWaitForPb.class;
    }

    public void initFrom(Message proto) {
        ThreadToWaitForPb p = (ThreadToWaitForPb) proto;
        threadRunNumber = VariableAssignmentModel.fromProto(p.getThreadRunNumber());
    }

    public ThreadToWaitForPb.Builder toProto() {
        ThreadToWaitForPb.Builder out = ThreadToWaitForPb.newBuilder();
        out.setThreadRunNumber(threadRunNumber.toProto());
        return out;
    }
}
