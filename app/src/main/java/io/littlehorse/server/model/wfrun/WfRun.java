package io.littlehorse.server.model.wfrun;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.proto.WfRunPb;
import io.littlehorse.common.util.LHUtil;

public class WfRun extends GETable<WfRunPb> {
    public String id;
    public String wfSpecId;
    public String wfSpecName;
    public LHStatusPb status;
    public long lastUpdateOffset;
    public Date startTime;
    public Date endTime;
    public List<ThreadRun> threadRuns;

    public WfRun() {
        threadRuns = new ArrayList<>();
    }

    public Date getCreatedAt() {
        return startTime;
    }

    public void initFrom(WfRunPb proto) {
        id = proto.getId();
        wfSpecId = proto.getWfSpecId();
        wfSpecName = proto.getWfSpecName();
        status = proto.getStatus();
        lastUpdateOffset = proto.getLastUpdateOffset();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());

        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }

        for (ThreadRunPb trpb: proto.getThreadRunsList()) {
            threadRuns.add(ThreadRun.fromProto(trpb));
        }
    }

    public WfRunPb.Builder toProto() {
        WfRunPb.Builder out = WfRunPb.newBuilder()
            .setId(id)
            .setWfSpecId(wfSpecId)
            .setWfSpecName(wfSpecName)
            .setStatus(status)
            .setLastUpdateOffset(lastUpdateOffset)
            .setStartTime(LHUtil.fromDate(startTime));

        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }

        for (ThreadRun threadRun: threadRuns) {
            out.addThreadRuns(threadRun.toProto());
        }

        return out;
    }

    public Class<WfRunPb> getProtoBaseClass() {
        return WfRunPb.class;
    }

    @Override public String getStoreKey() {
        return id;
    }

    @Override public String getPartitionKey() {
        return id;
    }

}
