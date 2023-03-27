package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.common.model.wfrun.VarNameAndVal;
import io.littlehorse.jlib.common.proto.TaskScheduledOePb;
import io.littlehorse.jlib.common.proto.VarNameAndValPb;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskScheduledOe extends SubEvent<TaskScheduledOePb> {

    public String taskDefName;
    public int taskDefVersion;
    public int threadRunNumber;
    public int taskRunNumber;
    public int taskRunPosition;
    public List<VarNameAndVal> variables;
    public String nodeName;
    public String wfSpecName;
    public int wfSpecVersion;
    public int attemptNumber;

    public Class<TaskScheduledOePb> getProtoBaseClass() {
        return TaskScheduledOePb.class;
    }

    public TaskScheduledOe() {
        variables = new ArrayList<>();
    }

    public TaskScheduledOePb.Builder toProto() {
        TaskScheduledOePb.Builder out = TaskScheduledOePb
            .newBuilder()
            .setTaskDefName(taskDefName)
            .setTaskDefVersion(taskDefVersion)
            .setTaskRunNumber(taskRunNumber)
            .setTaskRunPosition(taskRunPosition)
            .setNodeName(nodeName)
            .setWfSpecName(wfSpecName)
            .setWfSpecVersion(wfSpecVersion)
            .setAttemptNumber(attemptNumber);

        for (VarNameAndVal vnav : variables) {
            out.addVariables(vnav.toProto());
        }

        return out;
    }

    public void initFrom(Message proto) {
        TaskScheduledOePb p = (TaskScheduledOePb) proto;
        taskDefName = p.getTaskDefName();
        taskDefVersion = p.getTaskDefVersion();
        taskRunNumber = p.getTaskRunNumber();
        taskRunPosition = p.getTaskRunPosition();
        nodeName = p.getNodeName();
        wfSpecName = p.getWfSpecName();
        wfSpecVersion = p.getWfSpecVersion();
        attemptNumber = p.getAttemptNumber();

        for (VarNameAndValPb vnav : p.getVariablesList()) {
            variables.add(LHSerializable.fromProto(vnav, VarNameAndVal.class));
        }
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        List<TaskMetricUpdate> tmus = dao.getTaskMetricWindows(taskDefName, time);

        for (TaskMetricUpdate tmu : tmus) {
            tmu.numEntries++;
            tmu.totalStarted++;
        }
    }
}
