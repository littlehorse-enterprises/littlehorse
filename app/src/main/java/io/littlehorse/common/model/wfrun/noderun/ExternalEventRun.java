package io.littlehorse.common.model.wfrun.noderun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.proto.ExternalEventRunPb;
import io.littlehorse.common.proto.ExternalEventRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class ExternalEventRun extends LHSerializable<ExternalEventRunPb> {

    public String externalEventDefName;
    public Date eventTime;

    public Class<ExternalEventRunPb> getProtoBaseClass() {
        return ExternalEventRunPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExternalEventRunPbOrBuilder p = (ExternalEventRunPbOrBuilder) proto;
        if (p.hasEventTime()) {
            eventTime = LHUtil.fromProtoTs(p.getEventTime());
        }
        externalEventDefName = p.getExternalEventDefName();
    }

    public ExternalEventRunPb.Builder toProto() {
        ExternalEventRunPb.Builder out = ExternalEventRunPb
            .newBuilder()
            .setExternalEventDefName(externalEventDefName);

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

    public List<Tag> getTags(NodeRun parent) {
        List<Tag> out = new ArrayList<>();
        out.add(
            new Tag(
                parent,
                Pair.of("type", "EXTERNAL_EVENT"),
                Pair.of("externalEventDefName", externalEventDefName)
            )
        );

        return out;
    }
}
