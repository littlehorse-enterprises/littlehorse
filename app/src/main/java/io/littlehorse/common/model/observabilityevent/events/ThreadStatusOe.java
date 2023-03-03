package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.ThreadStatusOePb;
import io.littlehorse.jlib.common.proto.ThreadStatusOePbOrBuilder;

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

    public void initFrom(MessageOrBuilder proto) {
        ThreadStatusOePbOrBuilder p = (ThreadStatusOePbOrBuilder) proto;
        status = p.getStatus();
        threadRunNumber = p.getThreadRunNumber();
    }
}
