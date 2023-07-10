package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.PendingFailureHandlerPb;

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

    public void initFrom(Message proto) {
        PendingFailureHandlerPb p = (PendingFailureHandlerPb) proto;
        failedThreadRun = p.getFailedThreadRun();
        handlerSpecName = p.getHandlerSpecName();
    }

    public static PendingFailureHandler fromProto(PendingFailureHandlerPb p) {
        PendingFailureHandler out = new PendingFailureHandler();
        out.initFrom(p);
        return out;
    }
}
