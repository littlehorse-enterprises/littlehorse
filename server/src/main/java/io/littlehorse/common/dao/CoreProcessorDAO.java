package io.littlehorse.common.dao;

import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.LHTimer;
import io.littlehorse.common.model.ScheduledTaskModel;
import io.littlehorse.common.model.command.CommandModel;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.core.taskrun.TaskRunModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.taskdef.TaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.objectId.NodeRunIdModel;
import io.littlehorse.common.model.getable.objectId.TaskRunIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskRunIdModel;
import io.littlehorse.sdk.common.proto.HostInfo;
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
public interface CoreProcessorDAO extends ReadOnlyMetadataStore {
    public String getCoreCmdTopic();

    public void setCommand(CommandModel command);

    public CommandModel getCommand();

    public default Date getEventTime() {
        return getCommand().time;
    }

    public void putNodeRun(NodeRunModel nr);

    public NodeRunModel getNodeRun(String wfRunId, int threadNum, int position);

    public default NodeRunModel getNodeRun(NodeRunIdModel id) {
        return getNodeRun(id.getWfRunId(), id.getThreadRunNumber(), id.getPosition());
    }

    public void putVariable(VariableModel var);

    public VariableModel getVariable(String wfRunId, String name, int threadNum);

    public ExternalEventModel getUnclaimedEvent(String wfRunId, String externalEventDefName);

    public ExternalEventModel getExternalEvent(String externalEventId);

    public void saveExternalEvent(ExternalEventModel evt);

    public void scheduleTask(ScheduledTaskModel scheduledTask);

    public void scheduleTimer(LHTimer timer);

    public void saveWfRun(WfRunModel wfRunModel);

    public WfRunModel getWfRun(String id);

    public WfSpecModel getWfSpec(String name, Integer version);

    public TaskDefModel getTaskDef(String name);

    public UserTaskDefModel getUserTaskDef(String name, Integer version);

    public ExternalEventDefModel getExternalEventDef(String name);

    public ScheduledTaskModel markTaskAsScheduled(TaskRunIdModel taskRunId);

    public DeleteObjectReply deleteWfRun(String wfRunId);

    public DeleteObjectReply deleteExternalEvent(String externalEventId);

    public TaskRunModel getTaskRun(TaskRunIdModel taskRunId);

    public void putTaskRun(TaskRunModel taskRun);

    public void putUserTaskRun(UserTaskRunModel taskRun);

    public UserTaskRunModel getUserTaskRun(UserTaskRunIdModel userTaskRunId);

    /*
     * Clear any dirty cache if necessary
     */
    public void abortChanges();

    /*
     * Clear any dirty cache if necessary, BUT also mark any wfRun's that were
     * in processing as ERROR and note an error message.
     */
    void abortChangesAndMarkWfRunFailed(Throwable failure, String wfRunId);

    /*
     * Commit changes to the backing store.
     */
    public void commitChanges();

    public List<TaskMetricUpdate> getTaskMetricWindows(String taskDefName, Date time);

    public List<WfMetricUpdate> getWfMetricWindows(String wfSpecName, int wfSpecVersion, Date time);

    public HostInfo getAdvertisedHost(HostModel host, String listenerName) throws LHBadRequestError, LHConnectionError;

    public InternalHosts getInternalHosts();

    public TaskWorkerGroupModel getTaskWorkerGroup(String taskDefName);

    public void putTaskWorkerGroup(TaskWorkerGroupModel taskWorkerGroup);
}
