package io.littlehorse.common.model.observability.node;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.ExternalEventResultOePb;
import io.littlehorse.common.proto.ExternalEventResultOePbOrBuilder;

public class ExternalEventRunOe extends LHSerializable<ExternalEventResultOePb> {

    public String correlatedEventId;

    public Class<ExternalEventResultOePb> getProtoBaseClass() {
        return ExternalEventResultOePb.class;
    }

    public ExternalEventResultOePb.Builder toProto() {
        ExternalEventResultOePb.Builder out = ExternalEventResultOePb.newBuilder();

        if (correlatedEventId != null) {
            out.setCorrelatedEventId(correlatedEventId);
        }
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExternalEventResultOePbOrBuilder p = (ExternalEventResultOePbOrBuilder) proto;
        if (p.hasCorrelatedEventId()) {
            correlatedEventId = p.getCorrelatedEventId();
        }
    }

    public static ExternalEventRunOe fromProto(ExternalEventResultOePbOrBuilder p) {
        ExternalEventRunOe out = new ExternalEventRunOe();
        out.initFrom(p);
        return out;
    }
}
