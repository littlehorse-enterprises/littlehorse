package io.littlehorse.common.model.getable.core.taskworkergroup;

import com.google.protobuf.Message;
import com.google.protobuf.util.Timestamps;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.sdk.common.proto.TaskWorkerMetadata;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TaskWorkerMetadataModel extends LHSerializable<TaskWorkerMetadata> {

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
    public void initFrom(Message proto) {
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
}
