package io.littlehorse.common.model.command.subcommand;

import static io.littlehorse.common.LHConstants.MAX_TASK_WORKER_INACTIVITY;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommand.internals.RoundRobinAssignor;
import io.littlehorse.common.model.command.subcommand.internals.TaskWorkerAssignor;
import io.littlehorse.common.model.command.subcommandresponse.RegisterTaskWorkerReply;
import io.littlehorse.common.model.meta.HostModel;
import io.littlehorse.common.model.meta.TaskWorkerGroupModel;
import io.littlehorse.common.model.meta.TaskWorkerMetadataModel;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatRequest;
import io.littlehorse.server.streamsimpl.util.InternalHosts;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskWorkerHeartBeatRequestModel
    extends SubCommand<TaskWorkerHeartBeatRequest> {

    public String clientId;
    public String taskDefName;
    public String listenerName;
    private TaskWorkerAssignor assignor;
    private Set<HostModel> hosts;

    public TaskWorkerHeartBeatRequestModel() {
        assignor = new RoundRobinAssignor();
    }

    public TaskWorkerHeartBeatRequestModel(TaskWorkerAssignor assignor) {
        this.assignor = assignor;
    }

    @Override
    public RegisterTaskWorkerReply process(LHDAO dao, LHConfig config) {
        log.debug("Processing a heartbeat");

        // Get the group, a group contains all the task worker for that specific task
        TaskWorkerGroupModel taskWorkerGroup = dao.getTaskWorkerGroup(taskDefName);

        // If it does not exist then create it with empty workers
        if (taskWorkerGroup == null) {
            taskWorkerGroup = new TaskWorkerGroupModel();
            taskWorkerGroup.createdAt = new Date();
            taskWorkerGroup.taskDefName = taskDefName;
        }

        // Remove inactive taskWorker
        boolean areInactiveWorkersRemoved = removeInactiveWorkers(taskWorkerGroup);

        // Get the specific worker, each worker is supposed to have a unique client id
        boolean isANewTaskWorker = false;
        TaskWorkerMetadataModel taskWorker = taskWorkerGroup.taskWorkers.get(
            clientId
        );

        // If it is null then create it and add it to the task worker group
        if (taskWorker == null) {
            isANewTaskWorker = true;
            taskWorker = new TaskWorkerMetadataModel();
            taskWorker.clientId = clientId;
            taskWorkerGroup.taskWorkers.put(clientId, taskWorker);
        }

        // Verify there are no changes on the current servers
        boolean thereAreNewHost = checkIfNewHostsHasChanges(dao);

        // If there are dead workers or new workers or new hosts let's rebalance
        if (areInactiveWorkersRemoved || isANewTaskWorker || thereAreNewHost) {
            // Get all internal servers (from kafka stream API), they are already sorted by Host::getKey.
            // As it is a new worker then we need to rebalance
            log.info("Triggering rebalance");
            assignor.assign(hosts, taskWorkerGroup.taskWorkers.values());
        }

        // Update the latest heartbeat with the current timestamp
        taskWorker.latestHeartbeat = new Date();

        // Save the data
        dao.putTaskWorkerGroup(taskWorkerGroup);

        // Prepare the response with the assigned host for this specific task worker (taskWorker.hosts)
        return prepareReply(dao, taskWorker.hosts);
    }

    private boolean checkIfNewHostsHasChanges(LHDAO dao) {
        InternalHosts internalHosts = dao.getInternalHosts();
        hosts = internalHosts.getHosts();
        return internalHosts.hasChanges();
    }

    private RegisterTaskWorkerReply prepareReply(LHDAO dao, Set<HostModel> hosts) {
        RegisterTaskWorkerReply reply = new RegisterTaskWorkerReply();
        for (HostModel hostInfo : hosts) {
            try {
                // Validate the host is reachable
                reply.yourHosts.add(dao.getAdvertisedHost(hostInfo, listenerName));
            } catch (LHBadRequestError e) {
                // Reply error if the listener name is not correct
                log.error(e.getMessage(), e);
                reply.code = LHResponseCode.BAD_REQUEST_ERROR;
                reply.message = e.getMessage();
                reply.yourHosts.clear();
                return reply;
            } catch (LHConnectionError e) {
                // Continue if it receives an internal error, it is probably that this server is not ready yet
                log.warn(e.getMessage());
                continue;
            }
        }

        // If there are no hosts for any reason, then reply an error.
        // This SHOULD be impossible unless there's a bug in LittleHorse.
        if (reply.yourHosts.isEmpty()) {
            log.error("Server hosts unavailable, this SHOULD be impossible");
            reply.code = LHResponseCode.CONNECTION_ERROR;
            reply.message = "Server hosts unavailable";
            return reply;
        }

        // Everything went well so reply with ok
        reply.code = LHResponseCode.OK;

        return reply;
    }

    private boolean removeInactiveWorkers(TaskWorkerGroupModel taskWorkerGroup) {
        int sizeBeforeFiltering = taskWorkerGroup.taskWorkers.size();

        taskWorkerGroup.taskWorkers =
            taskWorkerGroup.taskWorkers
                .values()
                .stream()
                .filter(taskWorker ->
                    Duration
                        .between(
                            taskWorker.latestHeartbeat.toInstant(),
                            Instant.now()
                        )
                        .toSeconds() <
                    MAX_TASK_WORKER_INACTIVITY ||
                    taskWorker.clientId == clientId
                )
                .collect(
                    Collectors.toMap(
                        taskWorker -> taskWorker.clientId,
                        Function.identity()
                    )
                );

        return sizeBeforeFiltering > taskWorkerGroup.taskWorkers.size();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public String getPartitionKey() {
        return taskDefName;
    }

    @Override
    public TaskWorkerHeartBeatRequest.Builder toProto() {
        TaskWorkerHeartBeatRequest.Builder builder = TaskWorkerHeartBeatRequest
            .newBuilder()
            .setClientId(clientId)
            .setTaskDefName(taskDefName)
            .setListenerName(listenerName);
        return builder;
    }

    @Override
    public void initFrom(Message proto) {
        TaskWorkerHeartBeatRequest heartBeatPb = (TaskWorkerHeartBeatRequest) proto;
        clientId = heartBeatPb.getClientId();
        taskDefName = heartBeatPb.getTaskDefName();
        listenerName = heartBeatPb.getListenerName();
    }

    @Override
    public Class<TaskWorkerHeartBeatRequest> getProtoBaseClass() {
        return TaskWorkerHeartBeatRequest.class;
    }

    public static TaskWorkerHeartBeatRequestModel fromProto(
        TaskWorkerHeartBeatRequest p
    ) {
        TaskWorkerHeartBeatRequestModel out = new TaskWorkerHeartBeatRequestModel();
        out.initFrom(p);
        return out;
    }
}
