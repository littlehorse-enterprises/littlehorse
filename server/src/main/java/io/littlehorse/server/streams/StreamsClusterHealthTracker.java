package io.littlehorse.server.streams;

import com.google.protobuf.Empty;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.proto.LHInternalsGrpc.LHInternalsBlockingStub;
import io.littlehorse.common.proto.LocalTasksResponse;
import io.littlehorse.common.proto.ServerStatePb;
import io.littlehorse.common.proto.ServerStatusPb;
import io.littlehorse.common.proto.StandByTaskStatePb;
import io.littlehorse.common.proto.TaskStatePb;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TaskMetadata;

@Slf4j
public class StreamsClusterHealthTracker {

    private BackendInternalComms comms;
    private LHServerConfig config;

    public StreamsClusterHealthTracker(BackendInternalComms comms, LHServerConfig config) {
        this.comms = comms;
    }

    public List<ServerStatePb> buildServerStates(KafkaStreams kafkaStreams, String name) {
        List<ServerStatePb> serverStates = new ArrayList<>();
        kafkaStreams.metadataForAllStreamsClients().forEach(streamsClient -> {
            var hostInfo = streamsClient.hostInfo();

            LHInternalsBlockingStub client = comms.getInternalClient(hostInfo);
            try {
                LocalTasksResponse hostTask = client.localTasks(Empty.getDefaultInstance());
                ServerStatePb serverState = ServerStatePb.newBuilder()
                        .addAllActiveTasks(hostTask.getActiveTasksList())
                        .addAllStandbyTasks(hostTask.getStandbyTasksList())
                        .setHost(hostInfo.host())
                        .setPort(hostInfo.port())
                        .setServerStatus(ServerStatusPb.HOST_UP)
                        .setTopologyName(name)
                        .build();

                serverStates.add(serverState);
            } catch (Exception e) {
                log.warn("Host {} not available to get info", hostInfo);
                ServerStatePb serverState = ServerStatePb.newBuilder()
                        .addAllActiveTasks(List.of())
                        .addAllStandbyTasks(List.of())
                        .setHost(hostInfo.host())
                        .setPort(hostInfo.port())
                        .setServerStatus(ServerStatusPb.HOST_DOWN)
                        .setTopologyName(name)
                        .setErrorMessage(e.getMessage())
                        .build();

                serverStates.add(serverState);
            }
        });
        return serverStates;
    }

    public List<TaskStatePb> buildActiveTasksStatePb(TaskMetadata taskMetadata) {
        return taskMetadata.topicPartitions().stream()
                .map(topicPartition -> {
                    Long currentOffset = getCurrentOffset(taskMetadata, topicPartition);
                    Long endOffset = getEndOffset(taskMetadata, topicPartition);

                    return TaskStatePb.newBuilder()
                            .setTaskId(taskMetadata.taskId().toString())
                            .setTopic(topicPartition.topic())
                            .setPartition(topicPartition.partition())
                            .setHost(comms.getThisHost().host())
                            .setPort(comms.getThisHost().port())
                            .setCurrentOffset(currentOffset)
                            .setLag(calculateLag(currentOffset, endOffset))
                            .setRackId(config.getRackId())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<StandByTaskStatePb> buildStandbyTasksStatePb(TaskMetadata taskMetadata) {
        return taskMetadata.topicPartitions().stream()
                .map(topicPartition -> {
                    Long currentOffset = getCurrentOffset(taskMetadata, topicPartition);
                    Long endOffset = getEndOffset(taskMetadata, topicPartition);

                    return StandByTaskStatePb.newBuilder()
                            .setTaskId(taskMetadata.taskId().toString())
                            .setHost(comms.getThisHost().host())
                            .setPort(comms.getThisHost().port())
                            .setCurrentOffset(currentOffset)
                            .setLag(calculateLag(currentOffset, endOffset))
                            .setRackId(config.getRackId())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Long calculateLag(Long currentOffset, Long endOffset) {
        if (currentOffset < 0) {
            return endOffset + 1;
        }
        return endOffset - currentOffset;
    }

    private Long getCurrentOffset(TaskMetadata taskMetadata, TopicPartition topicPartition) {
        return taskMetadata.committedOffsets().containsKey(topicPartition)
                ? taskMetadata.committedOffsets().get(topicPartition)
                : -1;
    }

    private Long getEndOffset(TaskMetadata taskMetadata, TopicPartition topicPartition) {
        return taskMetadata.endOffsets().containsKey(topicPartition)
                ? taskMetadata.endOffsets().get(topicPartition)
                : -1;
    }
}
