package io.littlehorse.common.model.wfrun.noderun;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.common.proto.TaskRunPb;
import io.littlehorse.common.proto.TaskRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class TaskRun extends LHSerializable<TaskRunPb> {

    public int attemptNumber;
    public VariableValue output;
    public byte[] logOutput;

    public Date startTime;
    public String taskDefName;

    public Class<TaskRunPb> getProtoBaseClass() {
        return TaskRunPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        TaskRunPbOrBuilder p = (TaskRunPbOrBuilder) proto;
        attemptNumber = p.getAttemptNumber();
        if (p.hasOutput()) {
            output = VariableValue.fromProto(p.getOutputOrBuilder());
        }
        if (p.hasLogOutput()) {
            logOutput = p.getLogOutput().toByteArray();
        }

        if (p.hasStartTime()) {
            startTime = LHUtil.fromProtoTs(p.getStartTime());
        }
        taskDefName = p.getTaskDefId();
    }

    public TaskRunPb.Builder toProto() {
        TaskRunPb.Builder out = TaskRunPb
            .newBuilder()
            .setTaskDefId(taskDefName)
            .setAttemptNumber(attemptNumber);

        if (output != null) {
            out.setOutput(output.toProto());
        }
        if (logOutput != null) {
            out.setLogOutput(ByteString.copyFrom(logOutput));
        }
        if (startTime != null) {
            out.setStartTime(LHUtil.fromDate(startTime));
        }

        return out;
    }

    public static TaskRun fromProto(TaskRunPbOrBuilder proto) {
        TaskRun out = new TaskRun();
        out.initFrom(proto);
        return out;
    }

    public List<Tag> getTags(NodeRun parent) {
        List<Tag> out = new ArrayList<>();
        out.add(
            new Tag(
                parent,
                Pair.of("type", "TASK"),
                Pair.of("taskDefName", taskDefName)
            )
        );

        return out;
    }
}
