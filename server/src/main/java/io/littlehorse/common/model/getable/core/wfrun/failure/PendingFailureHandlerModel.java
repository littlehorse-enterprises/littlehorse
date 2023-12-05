package io.littlehorse.common.model.getable.core.wfrun.failure;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.PendingFailureHandler;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class PendingFailureHandlerModel extends LHSerializable<PendingFailureHandler> {

    public int failedThreadRun;
    public String handlerSpecName;

    public Class<PendingFailureHandler> getProtoBaseClass() {
        return PendingFailureHandler.class;
    }

    public PendingFailureHandler.Builder toProto() {
        PendingFailureHandler.Builder out = PendingFailureHandler.newBuilder()
                .setFailedThreadRun(failedThreadRun)
                .setHandlerSpecName(handlerSpecName);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PendingFailureHandler p = (PendingFailureHandler) proto;
        failedThreadRun = p.getFailedThreadRun();
        handlerSpecName = p.getHandlerSpecName();
    }

    public static PendingFailureHandlerModel fromProto(PendingFailureHandler p, ExecutionContext context) {
        PendingFailureHandlerModel out = new PendingFailureHandlerModel();
        out.initFrom(p, context);
        return out;
    }
}
