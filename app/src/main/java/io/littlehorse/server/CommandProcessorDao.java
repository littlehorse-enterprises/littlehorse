package io.littlehorse.server;

import io.littlehorse.common.model.command.subcommand.ExternalEvent;
import io.littlehorse.common.model.event.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.util.LHGlobalMetaStores;

public interface CommandProcessorDao {
    public String getWfRunEventQueue();

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

    public void saveWfRun(WfRun wfRun);

    public WfRun getWfRun(String id);

    /*
     * Clear any dirty cache if necessary
     */
    public void abortChanges();

    /*
     * Commit changes to the backing store.
     */
    public void commitChanges();

    public LHGlobalMetaStores getGlobalMetaStores();
}
