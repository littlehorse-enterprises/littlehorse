package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.WaitingForExtEvtOePb;
import io.littlehorse.jlib.common.proto.WaitingForExtEvtOePbOrBuilder;
import java.util.Date;

public class WaitingForExtEvtOe extends SubEvent<WaitingForExtEvtOePb> {

    public String extEvtDefName;
    public int threadRunNumber;
    public int nodeRunPosition;

    public Class<WaitingForExtEvtOePb> getProtoBaseClass() {
        return WaitingForExtEvtOePb.class;
    }

    public WaitingForExtEvtOePb.Builder toProto() {
        WaitingForExtEvtOePb.Builder out = WaitingForExtEvtOePb
            .newBuilder()
            .setExtEvtDefName(extEvtDefName)
            .setNodeRunPosition(nodeRunPosition)
            .setThreadRunNumber(threadRunNumber);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        WaitingForExtEvtOePbOrBuilder p = (WaitingForExtEvtOePbOrBuilder) proto;
        extEvtDefName = p.getExtEvtDefName();
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        // Nothing to do
    }
}
