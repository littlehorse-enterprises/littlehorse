package io.littlehorse.common.model.outputtopic;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.usertaskrun.UserTaskRunModel;
import io.littlehorse.sdk.common.proto.UserTaskRunUpdateRecord;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class UserTaskRunUpdateRecordModel extends LHSerializable<UserTaskRunUpdateRecord>
        implements GenericOutputTopicRecordModel {

    private UserTaskRunModel getable;

    @Override
    public Class<UserTaskRunUpdateRecord> getProtoBaseClass() {
        return UserTaskRunUpdateRecord.class;
    }

    @Override
    public UserTaskRunUpdateRecord.Builder toProto() {
        UserTaskRunUpdateRecord.Builder result = UserTaskRunUpdateRecord.newBuilder();

        result.setUserTaskRun(getable.toProto());

        return result;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ignored) {
        UserTaskRunUpdateRecord p = (UserTaskRunUpdateRecord) proto;

        this.getable = LHSerializable.fromProto(p.getUserTaskRun(), UserTaskRunModel.class, ignored);
    }

    @Override
    public String getPartitionKey() {
        return getable.getPartitionKey().get();
    }
}
