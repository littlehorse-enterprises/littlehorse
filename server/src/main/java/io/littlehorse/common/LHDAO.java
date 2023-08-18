package io.littlehorse.common;

import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.command.Command;
import io.littlehorse.common.model.command.subcommandresponse.DeleteObjectReply;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.model.meta.HostModel;
import io.littlehorse.common.model.meta.TaskDefModel;
import io.littlehorse.common.model.meta.TaskWorkerGroupModel;
import io.littlehorse.common.model.meta.WfSpecModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.objectId.NodeRunIdModel;
import io.littlehorse.common.model.objectId.TaskRunIdModel;
import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.wfrun.ExternalEventModel;
import io.littlehorse.common.model.wfrun.LHTimer;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.model.wfrun.ScheduledTaskModel;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.common.model.wfrun.VariableModel;
import io.littlehorse.common.model.wfrun.WfRunModel;
import io.littlehorse.common.model.wfrun.taskrun.TaskRunModel;
import io.littlehorse.common.util.LHGlobalMetaStores;
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
public interface LHDAO extends LHGlobalMetaStores {
    public String getCoreCmdTopic();

    public void setCommand(Command command);

    public Command getCommand();

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

    /*
     * Looks up a WfSpec. If in a partitioned environment (eg. KafkaStreams backend),
     * then the behavior should be:
     * - If on the metadata partition, return the strongly consistent version.
     * - else, look up from the global store in an eventually-consistent manner.
     * That behavior should be transparent to the caller.
     */
    public WfSpecModel getWfSpec(String name, Integer version);

    public void putWfSpec(WfSpecModel spec);

    public TaskDefModel getTaskDef(String name);

    public void putTaskDef(TaskDefModel spec);

    public UserTaskDefModel getUserTaskDef(String name, Integer version);

    public void putUserTaskDef(UserTaskDefModel spec);

    public ExternalEventDefModel getExternalEventDef(String name);

    public ScheduledTaskModel markTaskAsScheduled(TaskRunIdModel taskRunId);

    public void putExternalEventDef(ExternalEventDefModel eed);

    public DeleteObjectReply deleteWfRun(String wfRunId);

    public DeleteObjectReply deleteTaskDef(String name);

    public DeleteObjectReply deleteUserTaskDef(String name, int version);

    public DeleteObjectReply deleteWfSpec(String name, int version);

    public DeleteObjectReply deleteExternalEventDef(String name);

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

    public LHGlobalMetaStores getGlobalMetaStores();

    public List<TaskMetricUpdate> getTaskMetricWindows(String taskDefName, Date time);

    public List<WfMetricUpdate> getWfMetricWindows(String wfSpecName, int wfSpecVersion, Date time);

    public HostInfo getAdvertisedHost(HostModel host, String listenerName)
            throws LHBadRequestError, LHConnectionError;

    public InternalHosts getInternalHosts();

    public TaskWorkerGroupModel getTaskWorkerGroup(String taskDefName);

    public void putTaskWorkerGroup(TaskWorkerGroupModel taskWorkerGroup);
}
