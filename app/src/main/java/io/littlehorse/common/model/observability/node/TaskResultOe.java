package io.littlehorse.common.model.observability.node;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.TaskResultOePb;
import io.littlehorse.common.proto.TaskResultOePbOrBuilder;

public class TaskResultOe extends LHSerializable<TaskResultOePb> {

    public VariableValue result;
    public byte[] logOutput;

    public Class<TaskResultOePb> getProtoBaseClass() {
        return TaskResultOePb.class;
    }

    public TaskResultOePb.Builder toProto() {
        TaskResultOePb.Builder out = TaskResultOePb.newBuilder();

        if (result != null) {
            out.setResult(result.toProto());
        }
        if (logOutput != null) {
            out.setLogOutput(ByteString.copyFrom(logOutput));
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskResultOePbOrBuilder p = (TaskResultOePbOrBuilder) proto;
        if (p.hasResult()) {
            result = VariableValue.fromProto(p.getResultOrBuilder());
        }

        if (p.hasLogOutput()) {
            logOutput = p.getLogOutput().toByteArray();
        }
    }

    public static TaskResultOe fromProto(TaskResultOePbOrBuilder proto) {
        TaskResultOe out = new TaskResultOe();
        out.initFrom(proto);
        return out;
    }
}
