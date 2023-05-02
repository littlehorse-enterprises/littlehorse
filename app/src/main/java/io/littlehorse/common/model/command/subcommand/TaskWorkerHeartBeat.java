package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHBadRequestError;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommand.internals.RoundRobinAssignor;
import io.littlehorse.common.model.command.subcommand.internals.TaskWorkerAssignor;
import io.littlehorse.common.model.command.subcommandresponse.RegisterTaskWorkerReply;
import io.littlehorse.common.model.meta.Host;
import io.littlehorse.common.model.meta.TaskWorkerGroup;
import io.littlehorse.common.model.meta.TaskWorkerMetadata;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import io.littlehorse.jlib.common.proto.TaskWorkerHeartBeatPb;
import java.util.Date;
import java.util.LinkedList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskWorkerHeartBeat extends SubCommand<TaskWorkerHeartBeatPb> {

    private Logger log = LoggerFactory.getLogger(TaskWorkerHeartBeat.class);

    public String clientId;
    public String taskDefName;
    public String listenerName;
    private TaskWorkerAssignor assignor = new RoundRobinAssignor();

    @Override
    public RegisterTaskWorkerReply process(LHDAO dao, LHConfig config) {
        log.debug("Processing a heartbeat");
        // Get all internal servers (from kafka stream API), they are already sorted by Host::getKey.
        LinkedList<Host> allServerHosts = dao
            .getAllInternalHosts()
            .stream()
            .collect(Collectors.toCollection(LinkedList::new));

        // Get the group, a group contains all the task worker for that specific task
        TaskWorkerGroup taskWorkerGroup = dao.getTaskWorkerGroup(taskDefName);

        // If it does not exist then create it with empty workers
        if (taskWorkerGroup == null) {
            taskWorkerGroup = new TaskWorkerGroup();
            taskWorkerGroup.createdAt = new Date();
            taskWorkerGroup.taskDefName = taskDefName;
        }

        // Get the specific worker, each worker is supposed to have a unique client id
        TaskWorkerMetadata taskWorker = taskWorkerGroup.taskWorkers.get(clientId);

        // If it is null then create it and add it to the task worker group
        if (taskWorker == null) {
            taskWorker = new TaskWorkerMetadata();
            taskWorker.clientId = clientId;
            taskWorkerGroup.taskWorkers.put(clientId, taskWorker);

            // As it is a new worker then we need to rebalance
            assignor.assign(allServerHosts, taskWorkerGroup.taskWorkers.values());
        }

        // Update the latest heartbeat with the current timestamp
        taskWorker.latestHeartbeat = new Date();

        // Save the data
        dao.putTaskWorkerGroup(taskWorkerGroup);

        // Prepare the response with the assigned host for this specific task worker (taskWorker.hosts)
        RegisterTaskWorkerReply reply = new RegisterTaskWorkerReply();
        for (Host hostInfo : taskWorker.hosts) {
            try {
                // Validate the host is reachable
                reply.yourHosts.add(dao.getAdvertisedHost(hostInfo, listenerName));
            } catch (LHBadRequestError e) {
                // Reply error if the listener name is not correct
                log.error(e.getMessage(), e);
                reply.code = LHResponseCodePb.BAD_REQUEST_ERROR;
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
            reply.code = LHResponseCodePb.CONNECTION_ERROR;
            reply.message = "Server hosts unavailable";
            return reply;
        }

        // Everything went well so reply with ok
        reply.code = LHResponseCodePb.OK;

        return reply;
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
    public TaskWorkerHeartBeatPb.Builder toProto() {
        TaskWorkerHeartBeatPb.Builder builder = TaskWorkerHeartBeatPb
            .newBuilder()
            .setClientId(clientId)
            .setTaskDefName(taskDefName)
            .setListenerName(listenerName);
        return builder;
    }

    @Override
    public void initFrom(Message proto) {
        TaskWorkerHeartBeatPb heartBeatPb = (TaskWorkerHeartBeatPb) proto;
        clientId = heartBeatPb.getClientId();
        taskDefName = heartBeatPb.getTaskDefName();
        listenerName = heartBeatPb.getListenerName();
    }

    @Override
    public Class<TaskWorkerHeartBeatPb> getProtoBaseClass() {
        return TaskWorkerHeartBeatPb.class;
    }

    public static TaskWorkerHeartBeat fromProto(TaskWorkerHeartBeatPb p) {
        TaskWorkerHeartBeat out = new TaskWorkerHeartBeat();
        out.initFrom(p);
        return out;
    }
}
