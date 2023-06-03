package io.littlehorse.common;

import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.Host;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.TaskWorkerGroup;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.observabilityevent.ObservabilityEvent;
import io.littlehorse.common.model.wfrun.ExternalEvent;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.ScheduledTask;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.HostInfoPb;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.TaskMetricUpdate;
import io.littlehorse.server.streamsimpl.coreprocessors.repartitioncommand.repartitionsubcommand.WfMetricUpdate;
import io.littlehorse.server.streamsimpl.util.InternalHosts;
import java.util.Date;
import java.util.List;

/*
 * All PUT() commands throw errors if the processing partition does not match
 * the partition of the resource being PUT.
 *
 * TODO: decide whether it's ugly or not to have this interface extend
 * `LHGlobalMetaStores`.
 */
public interface LHDAO extends LHGlobalMetaStores {
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

    public void scheduleTask(ScheduledTask scheduledTask);

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

    public TaskDef getTaskDef(String name);

    public void putTaskDef(TaskDef spec);

    public ExternalEventDef getExternalEventDef(String name);

    public ScheduledTask markTaskAsScheduled(
        String wfRunId,
        int threadRunNumber,
        int taskRunPosition
    );

    public void putExternalEventDef(ExternalEventDef eed);

    public DeleteObjectReply deleteWfRun(String wfRunId);

    public DeleteObjectReply deleteTaskDef(String name);

    public DeleteObjectReply deleteWfSpec(String name, int version);

    public DeleteObjectReply deleteExternalEventDef(String name);

    public DeleteObjectReply deleteExternalEvent(String externalEventId);

    /*
     * Clear any dirty cache if necessary
     */
    public void abortChanges();

    /*
     * Clear any dirty cache if necessary, BUT also mark any wfRun's that were
     * in processing as ERROR and note an error message.
     */
    public void abortChangesAndMarkWfRunFailed(String message);

    /*
     * Commit changes to the backing store.
     */
    public List<ObservabilityEvent> commitChanges();

    public List<ObservabilityEvent> getObservabilityEvents();

    public void addObservabilityEvent(ObservabilityEvent evt);

    public LHGlobalMetaStores getGlobalMetaStores();

    public List<TaskMetricUpdate> getTaskMetricWindows(String taskDefName, Date time);

    public List<WfMetricUpdate> getWfMetricWindows(
        String wfSpecName,
        int wfSpecVersion,
        Date time
    );

    public List<HostInfoPb> getAllAdvertisedHosts(String listenerName)
        throws LHBadRequestError;

    public HostInfoPb getAdvertisedHost(Host host, String listenerName)
        throws LHBadRequestError, LHConnectionError;

    public InternalHosts getInternalHosts();

    public TaskWorkerGroup getTaskWorkerGroup(String taskDefName);

    public void putTaskWorkerGroup(TaskWorkerGroup taskWorkerGroup);
}
