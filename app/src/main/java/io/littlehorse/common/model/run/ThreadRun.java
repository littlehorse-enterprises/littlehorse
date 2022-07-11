package io.littlehorse.common.model.run;

import java.util.ArrayList;
import java.util.List;
import io.littlehorse.common.proto.LHStatusPb;
import io.littlehorse.common.proto.TaskRunPbOrBuilder;
import io.littlehorse.common.proto.ThreadRunPb;
import io.littlehorse.common.proto.ThreadRunPbOrBuilder;

public class ThreadRun {
    public String threadSpecName;
    public LHStatusPb status;
    public int threadRunNumber;

    public List<TaskRun> taskRuns;

    public ThreadRunPb.Builder toProtoBuilder() {
        ThreadRunPb.Builder b = ThreadRunPb.newBuilder()
            .setThreadRunNumber(threadRunNumber)
            .setStatus(status)
            .setThreadSpecName(threadSpecName);

        for (TaskRun t : taskRuns) {
            b.addActiveTaskRuns(t.toProtoBuilder());
        }
        return b;
    }

    public static ThreadRun fromProto(ThreadRunPbOrBuilder proto) {
        ThreadRun out = new ThreadRun();
        out.threadRunNumber = proto.getThreadRunNumber();
        out.status = proto.getStatus();
        out.threadSpecName = proto.getThreadSpecName();
        out.taskRuns = new ArrayList<>();

        for (TaskRunPbOrBuilder t: proto.getActiveTaskRunsList()) {
            out.taskRuns.add(TaskRun.fromProto(t));
        }

        return out;
    }
}
