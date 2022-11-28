package io.littlehorse.server;

import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.TaskScheduleRequest;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.Date;

/*
 * All PUT() commands throw errors if the processing partition does not match
 * the partition of the resource being PUT.
 *
 * TODO: decide whether it's ugly or not to have this interface extend
 * `LHGlobalMetaStores`.
 */
public interface CommandProcessorDao extends LHGlobalMetaStores {
    public String getWfRunEventQueue();

    public void setCommand(Command command);

    public Command getCommand();

    public default Date getEventTime() {
        return getCommand().time;
    }

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
     * Looks up a WfSpec. If in a partitioned environment (eg. KafkaStreams backend),
     * then the behavior should be:
     * - If on the metadata partition, return the strongly consistent version.
     * - else, look up from the global store in an eventually-consistent manner.
     * That behavior should be transparent to the caller.
     */
    public WfSpec getWfSpec(String name, Integer version);

    public void putWfSpec(WfSpec spec);

    public TaskDef getTaskDef(String name, Integer version);

    public void putTaskDef(TaskDef spec);

    public ExternalEventDef getExternalEventDef(String name, Integer version);

    public void putExternalEventDef(ExternalEventDef eed);

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
