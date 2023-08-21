package io.littlehorse.common.model.getable.core.taskworkergroup;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.HostInfo;

public class HostModel extends LHSerializable<HostInfo> implements Comparable<HostModel> {

    public String host;
    public int port;

    public HostModel(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public HostModel() {}

    @Override
    public HostInfo.Builder toProto() {
        return HostInfo.newBuilder().setHost(host).setPort(port);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        HostInfo hostInfo = (HostInfo) proto;
        host = hostInfo.getHost();
        port = hostInfo.getPort();
    }

    @Override
    public Class<HostInfo> getProtoBaseClass() {
        return HostInfo.class;
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
