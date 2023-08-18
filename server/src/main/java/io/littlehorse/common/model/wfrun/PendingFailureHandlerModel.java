package io.littlehorse.common.model.wfrun;

import com.google.protobuf.Message;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.sdk.common.proto.PendingFailureHandler;

public class PendingFailureHandlerModel extends LHSerializable<PendingFailureHandler> {

    public int failedThreadRun;
    public String handlerSpecName;

    public Class<PendingFailureHandler> getProtoBaseClass() {
        return PendingFailureHandler.class;
    }

    public PendingFailureHandler.Builder toProto() {
        PendingFailureHandler.Builder out =
                PendingFailureHandler.newBuilder()
                        .setFailedThreadRun(failedThreadRun)
                        .setHandlerSpecName(handlerSpecName);
        return out;
    }

    public void initFrom(Message proto) {
        PendingFailureHandler p = (PendingFailureHandler) proto;
        failedThreadRun = p.getFailedThreadRun();
        handlerSpecName = p.getHandlerSpecName();
    }

    public static PendingFailureHandlerModel fromProto(PendingFailureHandler p) {
        PendingFailureHandlerModel out = new PendingFailureHandlerModel();
        out.initFrom(p);
        return out;
    }
}
