package io.littlehorse.common.model.wfrun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.PendingFailureHandlerPb;
import io.littlehorse.common.proto.PendingFailureHandlerPbOrBuilder;

public class PendingFailureHandler extends LHSerializable<PendingFailureHandlerPb> {

    public int failedThreadRun;
    public String handlerSpecName;

    public Class<PendingFailureHandlerPb> getProtoBaseClass() {
        return PendingFailureHandlerPb.class;
    }

    public PendingFailureHandlerPb.Builder toProto() {
        PendingFailureHandlerPb.Builder out = PendingFailureHandlerPb
            .newBuilder()
            .setFailedThreadRun(failedThreadRun)
            .setHandlerSpecName(handlerSpecName);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PendingFailureHandlerPbOrBuilder p = (PendingFailureHandlerPbOrBuilder) proto;
        failedThreadRun = p.getFailedThreadRun();
        handlerSpecName = p.getHandlerSpecName();
    }

    public static PendingFailureHandler fromProto(
        PendingFailureHandlerPbOrBuilder p
    ) {
        PendingFailureHandler out = new PendingFailureHandler();
        out.initFrom(p);
        return out;
    }
}
