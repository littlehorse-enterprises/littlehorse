package io.littlehorse.common.model.getable.global.wfspec.thread;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.ThreadRunModel;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy;
import io.littlehorse.sdk.common.proto.ThreadRetentionPolicy.ThreadGcPolicyCase;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;
import lombok.Getter;

@Getter
public class ThreadRetentionPolicyModel extends LHSerializable<ThreadRetentionPolicy> {

    private ThreadGcPolicyCase type;
    private long secondsAfterTermination;

    @Override
    public Class<ThreadRetentionPolicy> getProtoBaseClass() {
        return ThreadRetentionPolicy.class;
    }

    @Override
    public ThreadRetentionPolicy.Builder toProto() {
        ThreadRetentionPolicy.Builder out = ThreadRetentionPolicy.newBuilder();
        switch (type) {
            case SECONDS_AFTER_THREAD_TERMINATION:
                out.setSecondsAfterThreadTermination(secondsAfterTermination);
                break;
            case THREADGCPOLICY_NOT_SET:
                // nothing to do
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ThreadRetentionPolicy p = (ThreadRetentionPolicy) proto;
        type = p.getThreadGcPolicyCase();

        switch (type) {
            case SECONDS_AFTER_THREAD_TERMINATION:
                secondsAfterTermination = p.getSecondsAfterThreadTermination();
                break;
            case THREADGCPOLICY_NOT_SET:
                // nothing to do
        }
    }

    private Date getScheduledTerminationFor(Date threadEndTime) {

        switch (type) {
            case SECONDS_AFTER_THREAD_TERMINATION:
                return new Date(threadEndTime
                        .toInstant()
                        .plusSeconds(secondsAfterTermination)
                        .toEpochMilli());
            case THREADGCPOLICY_NOT_SET:
        }

        // If a future implementation of WorkflowRetentionPolicy returns null,
        // it means that the WfRun gets to hang out forever.
        return null;
    }

    public boolean shouldGcThreadRun(ThreadRunModel thread) {
        if (thread.getNumber() == 0) {
            return false;
        }
        Date threadEndTime = thread.getEndTime();
        if (threadEndTime != null && getScheduledTerminationFor(threadEndTime) != null) {
            return !(new Date().before(getScheduledTerminationFor(threadEndTime)));
        }
        return false;
    }
}
