package io.littlehorse.common.model.run;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import io.littlehorse.common.LHUtil;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.proto.WFRunPb;
import io.littlehorse.common.proto.WFRunPbOrBuilder;

public class WFRun {
    public String id;
    public String wfSpecId;
    public String wfSpecName;
    public Date startTime;
    public Date endTime;
    public LHStatusPb status;

    public List<ThreadRun> threadRuns;

    public WFRunPb.Builder toProtoBuilder() {
        WFRunPb.Builder b = WFRunPb.newBuilder()
            .setId(id)
            .setWfSpecId(wfSpecId)
            .setWfSpecName(wfSpecName)
            .setStartTime(LHUtil.fromDate(startTime))
            .setStatus(status);

        if (endTime != null) {
            b.setEndTime(LHUtil.fromDate(endTime));
        }

        for (ThreadRun t : threadRuns) {
            b.addThreadRuns(t.toProtoBuilder());
        }
        return b;
    }

    public static WFRun fromProto(WFRunPbOrBuilder proto) {
        WFRun out = new WFRun();
        out.id = proto.getId();
        out.wfSpecId = proto.getWfSpecId();
        out.wfSpecName = proto.getWfSpecName();
        out.startTime = LHUtil.fromProtoTs(proto.getStartTime());
        out.endTime = proto.hasEndTime()
            ? LHUtil.fromProtoTs(proto.getEndTime()) : null;
        out.status = proto.getStatus();
        out.threadRuns = new ArrayList<>();

        for (ThreadRunPb t : proto.getThreadRunsList()) {
            out.threadRuns.add(ThreadRun.fromProto(t));
        }
        return out;
    }
}
