package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.event.ExternalEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.subnode.ExternalEventNode;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.proto.ExternalEventRunPb;
import io.littlehorse.common.proto.ExternalEventRunPbOrBuilder;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.util.LHUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class ExternalEventRun extends SubNodeRun<ExternalEventRunPb> {

    public String externalEventDefName;
    public Date eventTime;
    public String externalEventId;

    public Class<ExternalEventRunPb> getProtoBaseClass() {
        return ExternalEventRunPb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExternalEventRunPbOrBuilder p = (ExternalEventRunPbOrBuilder) proto;
        if (p.hasEventTime()) {
            eventTime = LHUtil.fromProtoTs(p.getEventTime());
        }
        if (p.hasExternalEventId()) {
            externalEventId = p.getExternalEventId();
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

        if (externalEventId != null) {
            out.setExternalEventId(externalEventId);
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

    public void processEvent(WfRunEvent event) {
        LHUtil.log(
            "Got an event but not doing anything with it from ExternalEventRun"
        );
    }

    public boolean advanceIfPossible(Date time) {
        Node node = nodeRun.getNode();
        ExternalEventNode eNode = node.externalEventNode;

        ExternalEvent evt = nodeRun.threadRun.wfRun.stores.getUnclaimedEvent(
            eNode.externalEventDefName
        );
        if (evt == null) {
            // It hasn't come in yet.
            return false;
        }

        eventTime = evt.getCreatedAt();

        evt.claimed = true;
        evt.taskRunPosition = nodeRun.position;
        evt.threadRunNumber = nodeRun.threadRunNumber;

        nodeRun.complete(evt.content, time);
        return true;
    }

    /*
     * Need to override this for ExternalEventRun because it's technically in the
     * "RUNNING" status when waiting for the Event, and while waiting it's
     * perfectly fine (in fact, the *most expected*) time for the interrupt to
     * happen.
     */
    @Override
    public boolean canBeInterrupted() {
        return true;
    }

    public void arrive(Date time) {
        // Nothing to do
        nodeRun.status = LHStatusPb.RUNNING;
    }
}
