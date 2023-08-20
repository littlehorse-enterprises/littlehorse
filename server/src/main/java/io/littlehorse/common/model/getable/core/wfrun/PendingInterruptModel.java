package io.littlehorse.common.model.getable.core.wfrun;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.objectId.ExternalEventIdModel;
import io.littlehorse.sdk.common.proto.PendingInterrupt;

public class PendingInterruptModel extends LHSerializable<PendingInterrupt> {

    public ExternalEventIdModel externalEventId;
    public String handlerSpecName;
    public int interruptedThreadId;

    public Class<PendingInterrupt> getProtoBaseClass() {
        return PendingInterrupt.class;
    }

    public PendingInterrupt.Builder toProto() {
        PendingInterrupt.Builder out = PendingInterrupt.newBuilder()
                .setHandlerSpecName(handlerSpecName)
                .setExternalEventId(externalEventId.toProto())
                .setInterruptedThreadId(interruptedThreadId);
        return out;
    }

    public void initFrom(Message proto) {
        PendingInterrupt p = (PendingInterrupt) proto;
        externalEventId = LHSerializable.fromProto(p.getExternalEventId(), ExternalEventIdModel.class);
        handlerSpecName = p.getHandlerSpecName();
        interruptedThreadId = p.getInterruptedThreadId();
    }

    public static PendingInterruptModel fromProto(PendingInterrupt p) {
        PendingInterruptModel out = new PendingInterruptModel();
        out.initFrom(p);
        return out;
    }
}
