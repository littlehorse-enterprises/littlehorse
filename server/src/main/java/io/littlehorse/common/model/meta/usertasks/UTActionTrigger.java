package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.OnAssignedTask;
import io.littlehorse.common.model.meta.VariableAssignment;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.ActionCase;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.ScheduleTimeCase;
import io.littlehorse.sdk.common.proto.UTActionTriggerPb.UTACancelPb;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class UTActionTrigger extends LHSerializable<UTActionTriggerPb> {

    public ActionCase actionType;
    public UTATask task;
    public UTACancelPb cancel;
    public UTAReassign reassign;
    private OnAssignedTask onAssignedTask;

    public ScheduleTimeCase scheduleTimeType;
    public VariableAssignment delaySeconds;

    @Override
    public Class<UTActionTriggerPb> getProtoBaseClass() {
        return UTActionTriggerPb.class;
    }

    @Override
    public UTActionTriggerPb.Builder toProto() {
        UTActionTriggerPb.Builder out = UTActionTriggerPb.newBuilder();

        switch (actionType) {
            case TASK:
                out.setTask(task.toProto());
                break;
            case CANCEL:
                out.setCancel(cancel);
                break;
            case REASSIGN:
                out.setReassign(reassign.toProto());
                break;
            case ACTION_NOT_SET:
                throw new RuntimeException("Not possible");
        }

        switch (scheduleTimeType) {
            case DELAY_SECONDS:
                out.setDelaySeconds(delaySeconds.toProto());
                break;
            case ON_ASSIGNED_TASK:
                out.setOnAssignedTask(onAssignedTask.toProto());
            case SCHEDULETIME_NOT_SET:
            // nothing to do
        }
        return out;
    }

    public void schedule(LHDAO dao, UserTaskRun utr) throws LHVarSubError {
        switch (actionType) {
            case TASK:
                task.schedule(dao, utr, this);
                break;
            case REASSIGN:
                log.warn("Unimplemented: Reassign trigger");
                break;
            case CANCEL:
                log.warn("Unimplemented: Cancel trigger");
                break;
            case ACTION_NOT_SET:
            // nothing to do
        }
    }

    @Override
    public void initFrom(Message proto) {
        UTActionTriggerPb p = (UTActionTriggerPb) proto;

        scheduleTimeType = p.getScheduleTimeCase();
        switch (scheduleTimeType) {
            case DELAY_SECONDS:
                delaySeconds = VariableAssignment.fromProto(p.getDelaySeconds());
                break;
            case ON_ASSIGNED_TASK:
                onAssignedTask =
                    LHSerializable.fromProto(
                        p.getOnAssignedTask(),
                        OnAssignedTask.class
                    );
            case SCHEDULETIME_NOT_SET:
            // nothing to do
        }

        actionType = p.getActionCase();
        switch (actionType) {
            case TASK:
                task = LHSerializable.fromProto(p.getTask(), UTATask.class);
                break;
            case REASSIGN:
                reassign =
                    LHSerializable.fromProto(p.getReassign(), UTAReassign.class);
                break;
            case CANCEL:
                cancel = p.getCancel();
                break;
            case ACTION_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
