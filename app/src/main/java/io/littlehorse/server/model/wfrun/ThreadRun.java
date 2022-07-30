package io.littlehorse.server.model.wfrun;

import java.util.Date;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.util.LHUtil;

public class ThreadRun extends GETable<ThreadRunPb> {
    public String wfRunId;
    public int number;

    public LHStatusPb status;
    public String wfSpecId;
    public String threadSpecName;
    public int numSteps;

    public Date startTime;
    public Date endTime;


    public void initFrom(ThreadRunPb proto) {
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

    public static ThreadRun fromProto(ThreadRunPb proto) {
        ThreadRun out = new ThreadRun();
        out.initFrom(proto);
        return out;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public String getStoreKey() {
        return wfRunId + "-" + number;
    }

    public Class<ThreadRunPb> getProtoBaseClass() {
        return ThreadRunPb.class;
    }
}
