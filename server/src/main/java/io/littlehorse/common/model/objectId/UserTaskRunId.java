package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.wfrun.UserTaskRun;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskRunIdPb;
import io.littlehorse.sdk.common.proto.UserTaskRunPb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskRunId
    extends ObjectId<UserTaskRunIdPb, UserTaskRunPb, UserTaskRun> {

    private String wfRunId;
    private String userTaskGuid;

    public UserTaskRunId() {}

    public UserTaskRunId(String partitionKey, String guid) {
        this.wfRunId = partitionKey;
        this.userTaskGuid = guid;
    }

    public UserTaskRunId(String partitionKey) {
        this(partitionKey, LHUtil.generateGuid());
    }

    public Class<UserTaskRunIdPb> getProtoBaseClass() {
        return UserTaskRunIdPb.class;
    }

    public String getPartitionKey() {
        return wfRunId;
    }

    public void initFrom(Message proto) {
        UserTaskRunIdPb p = (UserTaskRunIdPb) proto;
        wfRunId = p.getWfRunId();
        userTaskGuid = p.getUserTaskGuid();
    }

    public UserTaskRunIdPb.Builder toProto() {
        UserTaskRunIdPb.Builder out = UserTaskRunIdPb
            .newBuilder()
            .setWfRunId(wfRunId)
            .setUserTaskGuid(userTaskGuid);
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

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.USER_TASK_RUN;
    }
}
