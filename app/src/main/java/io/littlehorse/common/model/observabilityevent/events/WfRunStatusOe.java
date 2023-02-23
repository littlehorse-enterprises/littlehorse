package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.LHStatusPb;
import io.littlehorse.jlib.common.proto.WfRunStatusOePb;
import io.littlehorse.jlib.common.proto.WfRunStatusOePbOrBuilder;

public class WfRunStatusOe extends SubEvent<WfRunStatusOePb> {

    public LHStatusPb status;

    public Class<WfRunStatusOePb> getProtoBaseClass() {
        return WfRunStatusOePb.class;
    }

    public WfRunStatusOePb.Builder toProto() {
        WfRunStatusOePb.Builder out = WfRunStatusOePb.newBuilder().setStatus(status);

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        WfRunStatusOePbOrBuilder p = (WfRunStatusOePbOrBuilder) proto;
        status = p.getStatus();
    }
}
