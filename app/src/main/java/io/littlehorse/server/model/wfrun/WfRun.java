package io.littlehorse.server.model.wfrun;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.server.ThreadRunPb;
import io.littlehorse.common.proto.server.WfRunPb;
import io.littlehorse.common.proto.server.WfRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.IndexEntry;

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

    public void initFrom(MessageOrBuilder p) {
        WfRunPbOrBuilder proto = (WfRunPbOrBuilder) p;
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

    @Override public String getObjectId() {
        return id;
    }

    @Override public String getPartitionKey() {
        return id;
    }

    public List<IndexEntry> getIndexEntries() {
        return new ArrayList<>();
    }
}
