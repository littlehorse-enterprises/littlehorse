package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskRunIdModel extends CoreObjectId<UserTaskRunId, UserTaskRun, UserTaskRunModel> {

    private WfRunIdModel wfRunId;
    private String userTaskGuid;

    public UserTaskRunIdModel() {}

    public UserTaskRunIdModel(WfRunIdModel wfRunId, String guid) {
        this.wfRunId = wfRunId;
        this.userTaskGuid = guid;
    }

    public UserTaskRunIdModel(WfRunIdModel wfRunId) {
        this(wfRunId, LHUtil.generateGuid());
    }

    public UserTaskRunIdModel(String wfRunId) {
        this(new WfRunIdModel(wfRunId));
    }

    @Override
    public Class<UserTaskRunId> getProtoBaseClass() {
        return UserTaskRunId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return wfRunId.getPartitionKey();
    }

    @Override
    public void initFrom(Message proto) {
        UserTaskRunId p = (UserTaskRunId) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class);
        userTaskGuid = p.getUserTaskGuid();
    }

    @Override
    public UserTaskRunId.Builder toProto() {
        UserTaskRunId.Builder out =
                UserTaskRunId.newBuilder().setWfRunId(wfRunId.toProto()).setUserTaskGuid(userTaskGuid);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId.toString(), userTaskGuid);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = (WfRunIdModel) ObjectIdModel.fromString(split[0], WfRunIdModel.class);
        userTaskGuid = split[1];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.USER_TASK_RUN;
    }
}
