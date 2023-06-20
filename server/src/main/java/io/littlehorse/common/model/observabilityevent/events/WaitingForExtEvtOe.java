package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.WaitingForExtEvtOePb;
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

    public void initFrom(Message proto) {
        WaitingForExtEvtOePb p = (WaitingForExtEvtOePb) proto;
        extEvtDefName = p.getExtEvtDefName();
        threadRunNumber = p.getThreadRunNumber();
        nodeRunPosition = p.getNodeRunPosition();
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        // Nothing to do
    }
}
