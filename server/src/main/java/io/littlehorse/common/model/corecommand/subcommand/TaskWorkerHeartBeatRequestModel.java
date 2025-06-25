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
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
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
    public RegisterTaskWorkerResponse process(CoreProcessorContext executionContext, LHServerConfig config) {
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
        removeInactiveWorkers(taskWorkerGroup);

        // Get the specific worker, each worker is supposed to have a unique client id
        TaskWorkerMetadataModel taskWorker = taskWorkerGroup.taskWorkers.get(clientId);

        // If it is null then create it and add it to the task worker group
        if (taskWorker == null) {
            taskWorker = new TaskWorkerMetadataModel();
            taskWorker.taskWorkerId = clientId;
            taskWorkerGroup.taskWorkers.put(clientId, taskWorker);
        }
        Set<HostModel> internalHosts = executionContext.getInternalHosts();
        // Run assignor
        assignor.assign(internalHosts, taskWorkerGroup.taskWorkers.values());
        Set<String> assignedHosts = taskWorkerGroup.taskWorkers.values().stream()
                .flatMap(taskWorkerMetadataModel -> taskWorkerMetadataModel.hosts.stream())
                .map(HostModel::getKey)
                .collect(Collectors.toSet());
        if (internalHosts.size() != assignedHosts.size()) {
            log.warn("Unbalanced assignment for task " + taskDefId.getName());
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
        return prepareReply(yourHosts);
    }

    private RegisterTaskWorkerResponse prepareReply(Set<LHHostInfo> yourHosts) {
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

    private void removeInactiveWorkers(TaskWorkerGroupModel taskWorkerGroup) {
        taskWorkerGroup.taskWorkers = taskWorkerGroup.taskWorkers.values().stream()
                .filter(taskWorker -> Duration.between(taskWorker.latestHeartbeat.toInstant(), Instant.now())
                                .toSeconds()
                        < MAX_TASK_WORKER_INACTIVITY)
                .collect(Collectors.toMap(taskWorker -> taskWorker.taskWorkerId, Function.identity()));
    }

    @Override
    public String getPartitionKey() {
        return taskDefId.getName();
    }

    @Override
    public TaskWorkerHeartBeatRequest.Builder toProto() {
        return TaskWorkerHeartBeatRequest.newBuilder()
                .setClientId(clientId)
                .setTaskDefId(taskDefId.toProto())
                .setListenerName(listenerName);
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
