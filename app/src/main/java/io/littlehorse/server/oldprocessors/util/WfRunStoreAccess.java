package io.littlehorse.server.oldprocessors.util;

import io.littlehorse.common.model.command.subcommand.ExternalEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;

public interface WfRunStoreAccess {
    public void putNodeRun(NodeRun nr);

    public NodeRun getNodeRun(String wfRunId, int threadNum, int position);

    public void putVariable(Variable var);

    public Variable getVariable(String wfRunId, String name, int threadNum);

    public ExternalEvent getUnclaimedEvent(
        String wfRunId,
        String externalEventDefName
    );

    public ExternalEvent getExternalEvent(String externalEventId);

    public void saveExternalEvent(ExternalEvent evt);

    public void scheduleTask(TaskScheduleRequest tsr);

    public void scheduleTimer(LHTimer timer);

    public void commitChanges();
}
