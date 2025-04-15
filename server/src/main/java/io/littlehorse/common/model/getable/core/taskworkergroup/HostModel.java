package io.littlehorse.common.model.getable.core.taskworkergroup;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Objects;

public class HostModel extends LHSerializable<LHHostInfo> implements Comparable<HostModel> {

    public String host;
    public int port;

    public HostModel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HostModel() {}

    @Override
    public LHHostInfo.Builder toProto() {
        return LHHostInfo.newBuilder().setHost(host).setPort(port);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        LHHostInfo hostInfo = (LHHostInfo) proto;
        host = hostInfo.getHost();
        port = hostInfo.getPort();
    }

    @Override
    public Class<LHHostInfo> getProtoBaseClass() {
        return LHHostInfo.class;
    }

    public String getKey() {
        return host + ":" + port;
    }

    @Override
    public int compareTo(HostModel otherHost) {
        return getKey().compareTo(otherHost.getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HostModel hostModel)) return false;
        return port == hostModel.port && Objects.equals(host, hostModel.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
