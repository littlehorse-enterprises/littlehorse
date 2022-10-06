package io.littlehorse.common.model.meta.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.proto.ExternalEventNodePb;
import io.littlehorse.common.proto.ExternalEventNodePbOrBuilder;

public class ExternalEventNode extends LHSerializable<ExternalEventNodePb> {

    public String externalEventDefName;

    @JsonIgnore
    public ExternalEventDef externalEventDef;

    public ExternalEventNode() {}

    public Class<ExternalEventNodePb> getProtoBaseClass() {
        return ExternalEventNodePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExternalEventNodePbOrBuilder p = (ExternalEventNodePbOrBuilder) proto;
        externalEventDefName = p.getExternalEventDefName();
    }

    public ExternalEventNodePb.Builder toProto() {
        ExternalEventNodePb.Builder out = ExternalEventNodePb
            .newBuilder()
            .setExternalEventDefName(externalEventDefName);
        return out;
    }
}
