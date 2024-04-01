package io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.proto.UTActionTrigger;
import io.littlehorse.sdk.common.proto.UTActionTrigger.ActionCase;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTACancel;
import io.littlehorse.sdk.common.proto.UTActionTrigger.UTHook;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
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

    public void schedule(UserTaskRunModel utr, ProcessorExecutionContext processorContext) throws LHVarSubError {
        switch (actionType) {
            case TASK:
                task.schedule(utr, this);
                break;
            case REASSIGN:
                reassign.schedule(utr, this, processorContext);
                break;
            case CANCEL:
                log.warn("Unimplemented: Cancel trigger");
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
                cancel = p.getCancel();
                break;
            case ACTION_NOT_SET:
                throw new RuntimeException("Not possible");
        }
    }
}
