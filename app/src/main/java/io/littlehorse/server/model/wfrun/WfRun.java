package io.littlehorse.server.model.wfrun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.server.ThreadRunPb;
import io.littlehorse.common.proto.server.WfRunPb;
import io.littlehorse.common.proto.server.WfRunPbOrBuilder;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.Tag;

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

    @JsonIgnore public Date getCreatedAt() {
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

    @JsonIgnore public WfRunPb.Builder toProto() {
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

    @JsonIgnore public Class<WfRunPb> getProtoBaseClass() {
        return WfRunPb.class;
    }

    @JsonIgnore @Override public String getObjectId() {
        return id;
    }

    @JsonIgnore @Override public String getPartitionKey() {
        return id;
    }

    @JsonIgnore public List<Tag> getTags() {
        List<Tag> out = Arrays.asList(
            new Tag(this, Pair.of("wfSpecName", wfSpecName)),
            new Tag(this, Pair.of("wfSpecId", wfSpecId)),
            new Tag(
                this,
                Pair.of("wfSpecId", wfSpecId),
                Pair.of("status", status.toString())
            )
        );

        return out;
    }
}
