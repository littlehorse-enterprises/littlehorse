package io.littlehorse.common.model.wfrun.noderun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.proto.ExternalEventRunPb;
import io.littlehorse.common.proto.ExternalEventRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.Date;

public class ExternalEventRun extends LHSerializable<ExternalEventRunPb> {

    public String externalEventDefId;
    public Date eventTime;

    public Class<ExternalEventRunPb> getProtoBaseClass() {
        return ExternalEventRunPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExternalEventRunPbOrBuilder p = (ExternalEventRunPbOrBuilder) proto;
        if (p.hasEventTime()) {
            eventTime = LHUtil.fromProtoTs(p.getEventTime());
        }
        externalEventDefId = p.getExternalEventDefId();
    }

    public ExternalEventRunPb.Builder toProto() {
        ExternalEventRunPb.Builder out = ExternalEventRunPb
            .newBuilder()
            .setExternalEventDefId(externalEventDefId);

        if (eventTime != null) {
            out.setEventTime(LHUtil.fromDate(eventTime));
        }

        return out;
    }

    public static ExternalEventRun fromProto(ExternalEventRunPbOrBuilder p) {
        ExternalEventRun out = new ExternalEventRun();
        out.initFrom(p);
        return out;
    }
}
