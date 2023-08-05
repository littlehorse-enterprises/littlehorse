package io.littlehorse.common.model.wfrun.subnoderun;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.Node;
import io.littlehorse.common.model.meta.subnode.UserTaskNode;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.model.objectId.UserTaskRunId;
import io.littlehorse.common.model.wfrun.Failure;
import io.littlehorse.common.model.wfrun.SubNodeRun;
import io.littlehorse.common.model.wfrun.User;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskNodePb;
import io.littlehorse.sdk.common.proto.UserTaskNodeRunPb;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskNodeRun extends SubNodeRun<UserTaskNodeRunPb> {

    private UserTaskRunId userTaskRunId;

    public UserTaskNodeRun() {}

    public Class<UserTaskNodeRunPb> getProtoBaseClass() {
        return UserTaskNodeRunPb.class;
    }

    public void initFrom(Message proto) {
        UserTaskNodeRunPb p = (UserTaskNodeRunPb) proto;
        if (p.hasUserTaskRunId()) {
            userTaskRunId =
                LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunId.class);
        }
    }

    public UserTaskNodeRunPb.Builder toProto() {
        UserTaskNodeRunPb.Builder out = UserTaskNodeRunPb.newBuilder();

        if (userTaskRunId != null) out.setUserTaskRunId(userTaskRunId.toProto());

        return out;
    }

    public boolean advanceIfPossible(Date time) {
        // UserTask Node does not care about other ThreadRuns or ExternalEvents;
        // therefore we only advance the node when the UserTaskRun completes.
        return false;
    }

    public void arrive(Date time) {
        // The UserTaskNode arrive() function should create a UserTaskRun.
        Node node = getNodeRun().getNode();
        UserTaskNode utn = node.getUserTaskNode();

        UserTaskDef utd = getDao()
            .getUserTaskDef(utn.getUserTaskDefName(), utn.getUserTaskDefVersion());

        if (utd == null) {
            // that means the UserTaskDef was deleted between now and the time that the
            // WfSpec was first created. Yikers!
            nodeRun.fail(
                new Failure(
                    "Appears that UserTaskDef was deleted!",
                    LHConstants.TASK_ERROR
                ),
                time
            );
            return;
        }
        UserTaskRun out;
        if (utn.getAssignmentType() == UserTaskNodePb.AssignmentCase.USER_ID) {
            out =
                new UserTaskRun(
                    utd,
                    new User(utn.getUserId().getRhsLiteralValue().getStrVal()),
                    getNodeRun()
                );
        } else {
            out =
                new UserTaskRun(
                    utd,
                    new User(utn.getUserGroup().getRhsLiteralValue().getStrVal()),
                    getNodeRun()
                );
        }
        // Now we create a new UserTaskRun.

        out.setDao(getDao());

        userTaskRunId = out.getObjectId();
        out.onArrival(time);
        getDao().putUserTaskRun(out);
    }
}
