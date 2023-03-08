package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.VariableValue;
import io.littlehorse.jlib.common.proto.TaskResultCodePb;
import io.littlehorse.jlib.common.proto.TaskResultOePb;
import io.littlehorse.jlib.common.proto.TaskResultOePbOrBuilder;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import java.util.Date;
import java.util.List;

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

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        NodeRun nr = dao.getNodeRun(wfRunId, threadRunNumber, taskRunPosition);
        long startToComplete =
            (nr.endTime.getTime() - nr.taskRun.startTime.getTime());

        List<TaskMetricUpdate> tmus = dao.getTaskMetricWindows(
            nr.taskRun.taskDefName,
            time
        );

        for (TaskMetricUpdate tmu : tmus) {
            tmu.numEntries++;
            if (resultCode.equals(TaskResultCodePb.SUCCESS)) {
                tmu.totalCompleted++;

                tmu.startToCompleteTotal += startToComplete;
                if (startToComplete > tmu.startToCompleteMax) {
                    tmu.startToCompleteMax = startToComplete;
                }
            } else {
                tmu.totalErrored++;
            }
        }
    }
}
