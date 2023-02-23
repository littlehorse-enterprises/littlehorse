package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.TaskResultOePb;
import io.littlehorse.jlib.common.proto.TaskResultOePbOrBuilder;

public class TaskResultOe extends SubEvent<TaskResultOePb> {

    public TaskResultCodePb resultCode;
    public int threadRunNumber;
    public int taskRunPosition;
    public VariableValue output;
    public VariableValue logOutput;

    public Class<TaskResultOePb> getProtoBaseClass() {
        return TaskResultOePb.class;
    }

    public TaskResultOePb.Builder toProto() {
        TaskResultOePb.Builder out = TaskResultOePb
            .newBuilder()
            .setResultCode(resultCode)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunPosition(taskRunPosition);
        if (output != null) out.setOutput(output.toProto());
        if (logOutput != null) out.setLogOutput(logOutput.toProto());
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskResultOePbOrBuilder p = (TaskResultOePbOrBuilder) proto;
        resultCode = p.getResultCode();
        taskRunPosition = p.getTaskRunPosition();
        threadRunNumber = p.getThreadRunNumber();
        if (p.hasOutput()) output = VariableValue.fromProto(p.getOutput());
        if (p.hasLogOutput()) {
            logOutput = VariableValue.fromProto(p.getLogOutput());
        }
    }
}
