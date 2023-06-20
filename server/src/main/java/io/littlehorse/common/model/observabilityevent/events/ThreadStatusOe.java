package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.ThreadStatusOePb;
import java.util.Date;

public class ThreadStatusOe extends SubEvent<ThreadStatusOePb> {

    public int threadRunNumber;
    public LHStatusPb status;

    public Class<ThreadStatusOePb> getProtoBaseClass() {
        return ThreadStatusOePb.class;
    }

    public ThreadStatusOePb.Builder toProto() {
        ThreadStatusOePb.Builder out = ThreadStatusOePb
            .newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setStatus(status);

        return out;
    }

    public void initFrom(Message proto) {
        ThreadStatusOePb p = (ThreadStatusOePb) proto;
        status = p.getStatus();
        threadRunNumber = p.getThreadRunNumber();
    }

    public void updateMetrics(LHDAO dao, Date time, String wfRunId) {
        // Nothing to do
    }
}
