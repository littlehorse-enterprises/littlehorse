package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.proto.ExitRunPb;
import io.littlehorse.common.proto.ExitRunPbOrBuilder;
import java.util.Date;

public class ExitRun extends SubNodeRun<ExitRunPb> {

    public Class<ExitRunPb> getProtoBaseClass() {
        return ExitRunPb.class;
    }

    public void initFrom(MessageOrBuilder p) {}

    public ExitRunPb.Builder toProto() {
        return ExitRunPb.newBuilder();
    }

    public static ExitRun fromProto(ExitRunPbOrBuilder p) {
        ExitRun out = new ExitRun();
        out.initFrom(p);
        return out;
    }

    public void processEvent(WfRunEvent event) {
        // I don't believe there's anything to do here.
    }

    public void advanceIfPossible(Date time) {
        // nothing to do
    }

    public void arrive(Date time) {
        nodeRun.threadRun.complete(time);
    }
}
