package io.littlehorse.common.model.getable.global.externaleventdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.CorrelatedEventConfig;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class CorrelatedEventConfigModel extends LHSerializable<CorrelatedEventConfig> {

    private Long ttlSeconds;
    private boolean deleteAfterFirstCorrelation;

    @Override
    public Class<CorrelatedEventConfig> getProtoBaseClass() {
        return CorrelatedEventConfig.class;
    }

    @Override
    public CorrelatedEventConfig.Builder toProto() {
        CorrelatedEventConfig.Builder out =
                CorrelatedEventConfig.newBuilder().setDeleteAfterFirstCorrelation(deleteAfterFirstCorrelation);
        if (ttlSeconds != null) out.setTtlSeconds(ttlSeconds);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        CorrelatedEventConfig p = (CorrelatedEventConfig) proto;
        if (p.hasTtlSeconds()) this.ttlSeconds = p.getTtlSeconds();
        this.deleteAfterFirstCorrelation = p.getDeleteAfterFirstCorrelation();
    }
}
