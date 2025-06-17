package io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.ActionCase;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class UTActionTriggerModel extends LHSerializable<UTActionTrigger> {

    public ActionCase actionType;
    public UTATaskModel task;
    public UTACancelModel cancel;
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
                out.setCancel(cancel.toProto());
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

    public void schedule(UserTaskRunModel utr, CoreProcessorContext processorContext) throws LHVarSubError {
        switch (actionType) {
            case TASK:
                task.schedule(utr, this, processorContext);
                break;
            case REASSIGN:
                reassign.schedule(utr, this, processorContext);
                break;
            case CANCEL:
                cancel.schedule(utr, this, processorContext);
                break;
            case ACTION_NOT_SET:
                // nothing to do
        }
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UTActionTrigger p = (UTActionTrigger) proto;
        hook = p.getHook();
        actionType = p.getActionCase();
        delaySeconds = LHSerializable.fromProto(p.getDelaySeconds(), VariableAssignmentModel.class, context);
        switch (actionType) {
            case TASK:
                task = LHSerializable.fromProto(p.getTask(), UTATaskModel.class, context);
                break;
            case REASSIGN:
                reassign = LHSerializable.fromProto(p.getReassign(), UTAReassignModel.class, context);
                break;
            case CANCEL:
                cancel = LHSerializable.fromProto(p.getCancel(), UTACancelModel.class, context);
                break;
            case ACTION_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
