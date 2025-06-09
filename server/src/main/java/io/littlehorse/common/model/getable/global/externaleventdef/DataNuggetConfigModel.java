package io.littlehorse.common.model.getable.global.externaleventdef;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.DataNuggetConfig;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class DataNuggetConfigModel extends LHSerializable<DataNuggetConfig> {

    private Long ttlSeconds;
    private boolean deleteAfterFirstCorrelation;

    @Override
    public Class<DataNuggetConfig> getProtoBaseClass() {
        return DataNuggetConfig.class;
    }

    @Override
    public DataNuggetConfig.Builder toProto() {
        DataNuggetConfig.Builder out =
                DataNuggetConfig.newBuilder().setDeleteAfterFirstCorrelation(deleteAfterFirstCorrelation);
        if (ttlSeconds != null) out.setTtlSeconds(ttlSeconds);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        DataNuggetConfig p = (DataNuggetConfig) proto;
        if (p.hasTtlSeconds()) this.ttlSeconds = p.getTtlSeconds();
        this.deleteAfterFirstCorrelation = p.getDeleteAfterFirstCorrelation();
    }
}
