package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.ObjectIdModel;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskRunIdModel extends ObjectIdModel<UserTaskRunId, UserTaskRun, UserTaskRunModel> {

    private String wfRunId;
    private String userTaskGuid;

    public UserTaskRunIdModel() {}

    public UserTaskRunIdModel(String partitionKey, String guid) {
        this.wfRunId = partitionKey;
        this.userTaskGuid = guid;
    }

    public UserTaskRunIdModel(String partitionKey) {
        this(partitionKey, LHUtil.generateGuid());
    }

    public Class<UserTaskRunId> getProtoBaseClass() {
        return UserTaskRunId.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        UserTaskRunId p = (UserTaskRunId) proto;
        wfRunId = p.getWfRunId();
        userTaskGuid = p.getUserTaskGuid();
    }

    public UserTaskRunId.Builder toProto() {
        UserTaskRunId.Builder out =
                UserTaskRunId.newBuilder().setWfRunId(wfRunId).setUserTaskGuid(userTaskGuid);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(wfRunId, userTaskGuid);
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        userTaskGuid = split[1];
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.USER_TASK_RUN;
    }
}
