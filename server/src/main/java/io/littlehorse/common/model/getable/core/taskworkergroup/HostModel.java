package io.littlehorse.common.model.getable.core.taskworkergroup;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.LHHostInfo;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

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
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {
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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final HostModel hostInfo = (HostModel) o;
        return port == hostInfo.port && host.equals(hostInfo.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
