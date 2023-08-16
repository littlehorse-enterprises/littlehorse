package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.NodeModel;
import io.littlehorse.common.model.meta.UserTaskNodeModel;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.sdk.common.proto.UserTaskNodeRunPb;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskNodeRun extends SubNodeRun<UserTaskNodeRunPb> {

    private UserTaskRunIdModel userTaskRunId;

    public UserTaskNodeRun() {}

    @Override
    public Class<UserTaskNodeRunPb> getProtoBaseClass() {
        return UserTaskNodeRunPb.class;
    }

    @Override
    public void initFrom(Message proto) {
        UserTaskNodeRunPb p = (UserTaskNodeRunPb) proto;
        if (p.hasUserTaskRunId()) {
            userTaskRunId =
                LHSerializable.fromProto(
                    p.getUserTaskRunId(),
                    UserTaskRunIdModel.class
                );
        }
    }

    @Override
    public UserTaskNodeRunPb.Builder toProto() {
        UserTaskNodeRunPb.Builder out = UserTaskNodeRunPb.newBuilder();

        if (userTaskRunId != null) out.setUserTaskRunId(userTaskRunId.toProto());

        return out;
    }

    @Override
    public boolean advanceIfPossible(Date time) {
        // UserTask Node does not care about other ThreadRuns or ExternalEvents;
        // therefore we only advance the node when the UserTaskRun completes.
        return false;
    }

    @Override
    public void arrive(Date time) {
        // The UserTaskNode arrive() function should create a UserTaskRun.
        NodeModel node = getNodeRunModel().getNode();
        UserTaskNodeModel utn = node.getUserTaskNode();

        UserTaskDefModel utd = getDao()
            .getUserTaskDef(utn.getUserTaskDefName(), utn.getUserTaskDefVersion());
        if (utd == null) {
            // that means the UserTaskDef was deleted between now and the time that the
            // WfSpec was first created. Yikers!
            nodeRunModel.fail(
                new Failure(
                    "Appears that UserTaskDef was deleted!",
                    LHConstants.TASK_ERROR
                ),
                time
            );
            return;
        }
        UserTaskRunModel out = new UserTaskRunModel(utd, utn, getNodeRunModel());
        // Now we create a new UserTaskRun.

        out.setDao(getDao());

        userTaskRunId = out.getObjectId();
        out.onArrival(time);
        getDao().putUserTaskRun(out);
    }
}
