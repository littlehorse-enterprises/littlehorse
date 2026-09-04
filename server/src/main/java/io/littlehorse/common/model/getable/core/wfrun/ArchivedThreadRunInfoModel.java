package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.ArchivedThreadRunInfo;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ArchivedThreadRunInfoModel extends LHSerializable<ArchivedThreadRunInfo> {

    @Override
    public Class<ArchivedThreadRunInfo> getProtoBaseClass() {
        return ArchivedThreadRunInfo.class;
    }

    @Override
    public ArchivedThreadRunInfo.Builder toProto() {
        return ArchivedThreadRunInfo.newBuilder();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        // ArchivedThreadRunInfo has no fields to deserialize.
    }

    public static ArchivedThreadRunInfoModel fromProto(ArchivedThreadRunInfo p, ExecutionContext context) {
        ArchivedThreadRunInfoModel out = new ArchivedThreadRunInfoModel();
        out.initFrom(p, context);
        return out;
    }
}
