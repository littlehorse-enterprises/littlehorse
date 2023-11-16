package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.CoreObjectId;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskRun;
import io.littlehorse.sdk.common.proto.UserTaskRunId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskRunIdModel extends CoreObjectId<UserTaskRunId, UserTaskRun, UserTaskRunModel> {

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

    @Override
    public Class<UserTaskRunId> getProtoBaseClass() {
        return UserTaskRunId.class;
    }

    @Override
    public Optional<String> getPartitionKey() {
        return Optional.of(wfRunId);
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        UserTaskRunId p = (UserTaskRunId) proto;
        wfRunId = p.getWfRunId();
        userTaskGuid = p.getUserTaskGuid();
    }

    @Override
    public UserTaskRunId.Builder toProto() {
        UserTaskRunId.Builder out =
                UserTaskRunId.newBuilder().setWfRunId(wfRunId).setUserTaskGuid(userTaskGuid);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(wfRunId, userTaskGuid);
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        wfRunId = split[0];
        userTaskGuid = split[1];
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.USER_TASK_RUN;
    }
}
