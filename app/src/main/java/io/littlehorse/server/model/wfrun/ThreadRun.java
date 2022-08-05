package io.littlehorse.server.model.wfrun;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.server.ThreadRunPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.server.model.internal.IndexEntry;

public class ThreadRun extends GETable<ThreadRunPb> {
    public String wfRunId;
    public int number;

    public LHStatusPb status;
    public String wfSpecId;
    public String threadSpecName;
    public int numSteps;

    public Date startTime;
    public Date endTime;

    public Date getCreatedAt() {
        return startTime;
    }

    public static String getStoreKey(String wfRunId, int threadRunNumber) {
        return wfRunId + "-" + threadRunNumber;
    }

    public void initFrom(MessageOrBuilder p) {
        ThreadRunPb proto = (ThreadRunPb) p;
        wfRunId = proto.getWfRunId();
        number = proto.getNumber();
        status = proto.getStatus();
        wfSpecId = proto.getWfSpecId();
        threadSpecName = proto.getThreadSpecName();
        numSteps = proto.getNumSteps();
        startTime = LHUtil.fromProtoTs(proto.getStartTime());
        if (proto.hasEndTime()) {
            endTime = LHUtil.fromProtoTs(proto.getEndTime());
        }
    }

    public ThreadRunPb.Builder toProto() {
        ThreadRunPb.Builder out = ThreadRunPb.newBuilder()
            .setWfRunId(wfRunId)
            .setNumber(number)
            .setStatus(status)
            .setWfSpecId(wfSpecId)
            .setThreadSpecName(threadSpecName)
            .setNumSteps(numSteps)
            .setStartTime(LHUtil.fromDate(startTime));

        if (endTime != null) {
            out.setEndTime(LHUtil.fromDate(endTime));
        }
        return out;
    }

    public static ThreadRun fromProto(MessageOrBuilder p) {
        ThreadRun out = new ThreadRun();
        out.initFrom(p);
        return out;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public String getStoreKey() {
        return ThreadRun.getStoreKey(wfRunId, number);
    }

    public Class<ThreadRunPb> getProtoBaseClass() {
        return ThreadRunPb.class;
    }

    public List<IndexEntry> getIndexEntries() {
        return new ArrayList<>();
    }
}
