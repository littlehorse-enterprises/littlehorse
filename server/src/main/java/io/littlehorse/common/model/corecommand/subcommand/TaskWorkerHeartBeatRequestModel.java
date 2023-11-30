package io.littlehorse.common.model.corecommand.subcommand;

import static io.littlehorse.common.LHConstants.MAX_TASK_WORKER_INACTIVITY;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.corecommand.subcommand.internals.RoundRobinAssignor;
import io.littlehorse.common.model.corecommand.subcommand.internals.TaskWorkerAssignor;
import io.littlehorse.common.model.getable.core.taskworkergroup.HostModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerGroupModel;
import io.littlehorse.common.model.getable.core.taskworkergroup.TaskWorkerMetadataModel;
import io.littlehorse.common.model.getable.objectId.TaskDefIdModel;
import io.littlehorse.common.model.getable.objectId.TaskWorkerGroupIdModel;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse;
import io.littlehorse.sdk.common.proto.TaskWorkerHeartBeatRequest;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import io.littlehorse.server.streams.util.InternalHosts;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class TaskWorkerHeartBeatRequestModel extends CoreSubCommand<TaskWorkerHeartBeatRequest> {

    @Setter // for unit test
    private String clientId;

    private TaskDefIdModel taskDefId;
    private String listenerName;

    private TaskWorkerAssignor assignor;
    private Set<HostModel> hosts;

    public TaskWorkerHeartBeatRequestModel() {
        assignor = new RoundRobinAssignor();
    }

    public TaskWorkerHeartBeatRequestModel(TaskWorkerAssignor assignor) {
        this.assignor = assignor;
    }

    @Override
    public RegisterTaskWorkerResponse process(ProcessorExecutionContext executionContext, LHServerConfig config) {
        log.debug("Processing a heartbeat");
        GetableManager getableManager = executionContext.getableManager();
        // Get the group, a group contains all the task worker for that specific task
        TaskWorkerGroupModel taskWorkerGroup = getableManager.get(new TaskWorkerGroupIdModel(taskDefId));

        // If it does not exist then create it with empty workers
        if (taskWorkerGroup == null) {
            taskWorkerGroup = new TaskWorkerGroupModel();
            taskWorkerGroup.createdAt = new Date();
            taskWorkerGroup.id = new TaskWorkerGroupIdModel(taskDefId);
        }

        // Remove inactive taskWorker
        boolean areInactiveWorkersRemoved = removeInactiveWorkers(taskWorkerGroup);

        // Get the specific worker, each worker is supposed to have a unique client id
        boolean isANewTaskWorker = false;
        TaskWorkerMetadataModel taskWorker = taskWorkerGroup.taskWorkers.get(clientId);

        // If it is null then create it and add it to the task worker group
        if (taskWorker == null) {
            isANewTaskWorker = true;
            taskWorker = new TaskWorkerMetadataModel();
            taskWorker.clientId = clientId;
            taskWorkerGroup.taskWorkers.put(clientId, taskWorker);
        }

        // Verify there are no changes on the current servers
        boolean thereAreNewHost = checkIfNewHostsHasChanges(executionContext.getInternalHosts());

        // If there are dead workers or new workers or new hosts let's rebalance
        if (areInactiveWorkersRemoved || isANewTaskWorker || thereAreNewHost) {
            // Get all internal servers (from kafka stream API), they are already sorted by
            // Host::getKey.
            // As it is a new worker then we need to rebalance
            log.info("Triggering rebalance");
            assignor.assign(hosts, taskWorkerGroup.taskWorkers.values());
        }

        // Update the latest heartbeat with the current timestamp
        taskWorker.latestHeartbeat = new Date();

        // Save the data
        getableManager.put(taskWorkerGroup);

        // Prepare the response with the assigned host for this specific task worker
        // (taskWorker.hosts)
        Set<LHHostInfo> yourHosts = new HashSet<>();
        for (HostModel hostInfo : taskWorker.hosts) {
            yourHosts.add(executionContext.getAdvertisedHost(hostInfo, listenerName));
        }
        return prepareReply(yourHosts, taskWorker.hosts);
    }

    private boolean checkIfNewHostsHasChanges(InternalHosts internalHosts) {
        hosts = internalHosts.getHosts();
        return internalHosts.hasChanges();
    }

    private RegisterTaskWorkerResponse prepareReply(Set<LHHostInfo> yourHosts, Set<HostModel> hosts) {
        RegisterTaskWorkerResponse.Builder reply = RegisterTaskWorkerResponse.newBuilder();
        reply.addAllYourHosts(yourHosts);

        // If there are no hosts for any reason, then reply an error.
        // This SHOULD be impossible unless there's a bug in LittleHorse.
        if (reply.getYourHostsCount() == 0) {
            log.error("Server hosts unavailable, this SHOULD be impossible");
            throw new LHApiException(Status.INTERNAL, "Should be impossible to have no server hosts");
        }

        return reply.build();
    }

    private boolean removeInactiveWorkers(TaskWorkerGroupModel taskWorkerGroup) {
        int sizeBeforeFiltering = taskWorkerGroup.taskWorkers.size();

        taskWorkerGroup.taskWorkers = taskWorkerGroup.taskWorkers.values().stream()
                .filter(taskWorker -> Duration.between(taskWorker.latestHeartbeat.toInstant(), Instant.now())
                                        .toSeconds()
                                < MAX_TASK_WORKER_INACTIVITY
                        || taskWorker.clientId == clientId)
                .collect(Collectors.toMap(taskWorker -> taskWorker.clientId, Function.identity()));

        return sizeBeforeFiltering > taskWorkerGroup.taskWorkers.size();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public String getPartitionKey() {
        return taskDefId.getName();
    }

    @Override
    public TaskWorkerHeartBeatRequest.Builder toProto() {
        TaskWorkerHeartBeatRequest.Builder builder = TaskWorkerHeartBeatRequest.newBuilder()
                .setClientId(clientId)
                .setTaskDefId(taskDefId.toProto())
                .setListenerName(listenerName);
        return builder;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskWorkerHeartBeatRequest heartBeatPb = (TaskWorkerHeartBeatRequest) proto;
        clientId = heartBeatPb.getClientId();
        taskDefId = LHSerializable.fromProto(heartBeatPb.getTaskDefId(), TaskDefIdModel.class, context);
        listenerName = heartBeatPb.getListenerName();
    }

    @Override
    public Class<TaskWorkerHeartBeatRequest> getProtoBaseClass() {
        return TaskWorkerHeartBeatRequest.class;
    }

    public static TaskWorkerHeartBeatRequestModel fromProto(TaskWorkerHeartBeatRequest p, ExecutionContext context) {
        TaskWorkerHeartBeatRequestModel out = new TaskWorkerHeartBeatRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
