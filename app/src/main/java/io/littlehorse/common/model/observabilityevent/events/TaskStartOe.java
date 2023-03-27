package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.jlib.common.proto.TaskStartOePb;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import java.util.Date;
import java.util.List;

public class TaskStartOe extends SubEvent<TaskStartOePb> {

    public String workerId;
    public int threadRunNumber;
    public int taskRunPosition;

    public Class<TaskStartOePb> getProtoBaseClass() {
        return TaskStartOePb.class;
    }

    public TaskStartOePb.Builder toProto() {
        TaskStartOePb.Builder out = TaskStartOePb
            .newBuilder()
            .setWorkerId(workerId)
            .setThreadRunNumber(threadRunNumber)
            .setTaskRunPosition(taskRunPosition);
        return out;
    }

    public void initFrom(Message proto) {
        TaskStartOePb p = (TaskStartOePb) proto;
        taskRunPosition = p.getTaskRunPosition();
        threadRunNumber = p.getThreadRunNumber();
        workerId = p.getWorkerId();
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        NodeRun nr = dao.getNodeRun(wfRunId, threadRunNumber, taskRunPosition);
        long scheduleToStart =
            (nr.taskRun.startTime.getTime() - nr.arrivalTime.getTime());

        List<TaskMetricUpdate> tmus = dao.getTaskMetricWindows(
            nr.taskRun.taskDefName,
            time
        );

        for (TaskMetricUpdate tmu : tmus) {
            tmu.numEntries++;
            tmu.totalStarted++;
            tmu.scheduleToStartTotal += scheduleToStart;

            if (scheduleToStart > tmu.scheduleToStartMax) {
                tmu.scheduleToStartMax = scheduleToStart;
            }
        }
    }
}
