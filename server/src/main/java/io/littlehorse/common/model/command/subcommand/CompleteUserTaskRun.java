package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.CompleteUserTaskRunReply;
import io.littlehorse.common.model.objectId.UserTaskRunIdModel;
import io.littlehorse.common.model.wfrun.UserTaskRunModel;
import io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.UserTaskResultPb;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteUserTaskRun extends SubCommand<CompleteUserTaskRunPb> {

    private UserTaskRunIdModel userTaskRunId;
    private String userId;
    private UserTaskResultPb result;
    private Date time;

    public Class<CompleteUserTaskRunPb> getProtoBaseClass() {
        return CompleteUserTaskRunPb.class;
    }

    public CompleteUserTaskRunPb.Builder toProto() {
        CompleteUserTaskRunPb.Builder out = CompleteUserTaskRunPb
            .newBuilder()
            .setUserTaskRunId(userTaskRunId.toProto())
            .setUserId(userId)
            .setResult(result);
        return out;
    }

    public void initFrom(Message proto) {
        CompleteUserTaskRunPb p = (CompleteUserTaskRunPb) proto;
        userId = p.getUserId();
        userTaskRunId =
            LHSerializable.fromProto(p.getUserTaskRunId(), UserTaskRunIdModel.class);
        result = p.getResult();
    }

    public CompleteUserTaskRunReply process(LHDAO dao, LHConfig config) {
        CompleteUserTaskRunReply out = new CompleteUserTaskRunReply();

        UserTaskRunModel utr = dao.getUserTaskRun(userTaskRunId);
        if (utr == null) {
            out.setCode(LHResponseCode.NOT_FOUND_ERROR);
            out.setMessage("Couldn't find userTaskRun " + userTaskRunId);
            return out;
        }

        try {
            utr.processTaskCompletedEvent(this);
        } catch (LHValidationError validationError) {
            out.code = LHResponseCode.BAD_REQUEST_ERROR;
            out.message = validationError.getMessage();
            return out;
        }

        out.code = LHResponseCode.OK;
        return out;
    }

    public boolean hasResponse() {
        return true;
    }

    public String getWfRunId() {
        return userTaskRunId.getWfRunId();
    }

    public String getPartitionKey() {
        return getWfRunId();
    }
}
