package io.littlehorse.common.model.getable.core.taskworkergroup;

import com.google.protobuf.Message;
import com.google.protobuf.util.Timestamps;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.proto.TaskWorkerMetadata;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class TaskWorkerMetadataModel extends LHSerializable<TaskWorkerMetadata>
        implements Comparable<TaskWorkerMetadataModel> {

    public String clientId;
    public Date latestHeartbeat;
    public Set<HostModel> hosts = new TreeSet<>();

    @Override
    public TaskWorkerMetadata.Builder toProto() {
        return TaskWorkerMetadata.newBuilder()
                .setClientId(clientId)
                .setLatestHeartbeat(Timestamps.fromDate(latestHeartbeat))
                .addAllHosts(hostsToProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        TaskWorkerMetadata metadataPb = (TaskWorkerMetadata) proto;
        clientId = metadataPb.getClientId();
        latestHeartbeat = LHUtil.fromProtoTs(metadataPb.getLatestHeartbeat());
        hosts = metadataPb.getHostsList().stream()
                .map(host -> new HostModel(host.getHost(), host.getPort()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Class<TaskWorkerMetadata> getProtoBaseClass() {
        return TaskWorkerMetadata.class;
    }

    public List<LHHostInfo> hostsToProto() {
        return hosts.stream().map(host -> host.toProto().build()).collect(Collectors.toList());
    }

    @Override
    public int compareTo(@NotNull TaskWorkerMetadataModel o) {
        if (clientId == null) return -1;
        return clientId.compareTo(o.clientId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskWorkerMetadataModel that = (TaskWorkerMetadataModel) o;
        return Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
}
