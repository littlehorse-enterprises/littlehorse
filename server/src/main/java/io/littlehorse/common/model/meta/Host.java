package io.littlehorse.common.model.meta;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.jlib.common.exception.LHSerdeError;
import io.littlehorse.jlib.common.proto.HostInfoPb;

public class Host extends LHSerializable<HostInfoPb> implements Comparable<Host> {

    public String host;
    public int port;

    public Host(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Host() {}

    @Override
    public HostInfoPb.Builder toProto() {
        return HostInfoPb.newBuilder().setHost(host).setPort(port);
    }

    @Override
    public void initFrom(Message proto) throws LHSerdeError {
        HostInfoPb hostInfo = (HostInfoPb) proto;
        host = hostInfo.getHost();
        port = hostInfo.getPort();
    }

    @Override
    public Class<HostInfoPb> getProtoBaseClass() {
        return HostInfoPb.class;
    }

    public String getKey() {
        return host + ":" + port;
    }

    @Override
    public int compareTo(Host otherHost) {
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

        final Host hostInfo = (Host) o;
        return port == hostInfo.port && host.equals(hostInfo.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
