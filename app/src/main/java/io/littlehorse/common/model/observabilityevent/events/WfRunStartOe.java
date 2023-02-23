package io.littlehorse.common.model.observabilityevent.events;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.observabilityevent.SubEvent;
import io.littlehorse.jlib.common.proto.WfRunStartOePb;
import io.littlehorse.jlib.common.proto.WfRunStartOePbOrBuilder;

public class WfRunStartOe extends SubEvent<WfRunStartOePb> {

    public int wfSpecVersion;
    public String wfSpecName;

    public Class<WfRunStartOePb> getProtoBaseClass() {
        return WfRunStartOePb.class;
    }

    public WfRunStartOePb.Builder toProto() {
        WfRunStartOePb.Builder out = WfRunStartOePb
            .newBuilder()
            .setWfSpecName(wfSpecName)
            .setWfSpecVersion(wfSpecVersion);
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        WfRunStartOePbOrBuilder p = (WfRunStartOePbOrBuilder) proto;
        wfSpecName = p.getWfSpecName();
        wfSpecVersion = p.getWfSpecVersion();
    }
}
