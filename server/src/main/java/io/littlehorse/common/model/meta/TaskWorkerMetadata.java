package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import com.google.protobuf.util.Timestamps;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.HostInfoPb;
import io.littlehorse.sdk.common.proto.TaskWorkerMetadataPb;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TaskWorkerMetadata extends LHSerializable<TaskWorkerMetadataPb> {

    public String clientId;
    public Date latestHeartbeat;
    public Set<Host> hosts = new TreeSet<>();

    @Override
    public TaskWorkerMetadataPb.Builder toProto() {
        return TaskWorkerMetadataPb
            .newBuilder()
            .setClientId(clientId)
            .setLatestHeartbeat(Timestamps.fromDate(latestHeartbeat))
            .addAllHosts(hostsToProto());
    }

    @Override
    public void initFrom(Message proto) {
        TaskWorkerMetadataPb metadataPb = (TaskWorkerMetadataPb) proto;
        clientId = metadataPb.getClientId();
        latestHeartbeat = LHUtil.fromProtoTs(metadataPb.getLatestHeartbeat());
        hosts =
            metadataPb
                .getHostsList()
                .stream()
                .map(host -> new Host(host.getHost(), host.getPort()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Class<TaskWorkerMetadataPb> getProtoBaseClass() {
        return TaskWorkerMetadataPb.class;
    }

    public List<HostInfoPb> hostsToProto() {
        return hosts
            .stream()
            .map(host -> host.toProto().build())
            .collect(Collectors.toList());
    }
}
