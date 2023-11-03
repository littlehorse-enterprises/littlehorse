package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy;
import io.littlehorse.sdk.common.proto.WorkflowRetentionPolicy.WfGcPolicyCase;
import java.util.Date;
import lombok.Getter;

@Getter
public class WorkflowRetentionPolicyModel extends LHSerializable<WorkflowRetentionPolicy> {

    private WfGcPolicyCase type;
    private long secondsAfterTermination;

    @Override
    public Class<WorkflowRetentionPolicy> getProtoBaseClass() {
        return WorkflowRetentionPolicy.class;
    }

    @Override
    public WorkflowRetentionPolicy.Builder toProto() {
        WorkflowRetentionPolicy.Builder out = WorkflowRetentionPolicy.newBuilder();
        switch (type) {
            case SECONDS_AFTER_WF_TERMINATION:
                out.setSecondsAfterWfTermination(secondsAfterTermination);
                break;
            case WFGCPOLICY_NOT_SET:
                // nothing to do
        }
        return out;
    }

    @Override
    public void initFrom(Message proto) {
        WorkflowRetentionPolicy p = (WorkflowRetentionPolicy) proto;
        type = p.getWfGcPolicyCase();

        switch (type) {
            case SECONDS_AFTER_WF_TERMINATION:
                secondsAfterTermination = p.getSecondsAfterWfTermination();
                break;
            case WFGCPOLICY_NOT_SET:
                // nothing to do
        }
    }

    public Date scheduleTerminationFor(WfRunModel terminatedWfRun) {
        Date terminatedTime = terminatedWfRun.getEndTime();

        switch (type) {
            case SECONDS_AFTER_WF_TERMINATION:
                return new Date(terminatedTime
                        .toInstant()
                        .plusSeconds(secondsAfterTermination)
                        .toEpochMilli());
            case WFGCPOLICY_NOT_SET:
        }

        // If a future implementation of WorkflowRetentionPolicy returns null,
        // it means that the WfRun gets to hang out forever.
        return null;
    }
}
