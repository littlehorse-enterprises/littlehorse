package io.littlehorse.common.model.observability;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.WaitForEvtOePb;
import io.littlehorse.common.proto.WaitForEvtOePbOrBuilder;

public class WaitForEvtOe extends LHSerializable<WaitForEvtOePb> {

    public String externalEventDefName;

    public Class<WaitForEvtOePb> getProtoBaseClass() {
        return WaitForEvtOePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        WaitForEvtOePbOrBuilder p = (WaitForEvtOePbOrBuilder) proto;
        externalEventDefName = p.getExternalEventDefName();
    }

    public WaitForEvtOePb.Builder toProto() {
        WaitForEvtOePb.Builder out = WaitForEvtOePb.newBuilder();
        out.setExternalEventDefName(externalEventDefName);
        return out;
    }

    public static WaitForEvtOe fromProto(WaitForEvtOePbOrBuilder proto) {
        WaitForEvtOe out = new WaitForEvtOe();
        out.initFrom(proto);
        return out;
    }
}
