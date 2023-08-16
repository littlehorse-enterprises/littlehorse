package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.VariableAssignmentModel;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.ActionCase;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTACancel;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class UTActionTriggerModel extends LHSerializable<UTActionTrigger> {

    public ActionCase actionType;
    public UTATaskModel task;
    public UTACancel cancel;
    public UTAReassignModel reassign;

    public UTHook hook;
    public VariableAssignmentModel delaySeconds;

    @Override
    public Class<UTActionTrigger> getProtoBaseClass() {
        return UTActionTrigger.class;
    }

    @Override
    public UTActionTrigger.Builder toProto() {
        UTActionTrigger.Builder out = UTActionTrigger.newBuilder();

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
        out.setHook(hook);
        out.setDelaySeconds(delaySeconds.toProto());
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
        UTActionTrigger p = (UTActionTrigger) proto;
        hook = p.getHook();
        actionType = p.getActionCase();
        delaySeconds =
            LHSerializable.fromProto(
                p.getDelaySeconds(),
                VariableAssignmentModel.class
            );
        switch (actionType) {
            case TASK:
                task = LHSerializable.fromProto(p.getTask(), UTATaskModel.class);
                break;
            case REASSIGN:
                reassign =
                    LHSerializable.fromProto(p.getReassign(), UTAReassignModel.class);
                break;
            case CANCEL:
                cancel = p.getCancel();
                break;
            case ACTION_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
