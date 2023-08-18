package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.usertasks.UserTaskDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskDefIdModel extends ObjectId<UserTaskDefId, UserTaskDef, UserTaskDefModel> {

    private String name;
    private int version;

    public UserTaskDefIdModel() {}

    public UserTaskDefIdModel(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public Class<UserTaskDefId> getProtoBaseClass() {
        return UserTaskDefId.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        UserTaskDefId p = (UserTaskDefId) proto;
        name = p.getName();
        version = p.getVersion();
    }

    public UserTaskDefId.Builder toProto() {
        UserTaskDefId.Builder out = UserTaskDefId.newBuilder().setName(name).setVersion(version);
        return out;
    }

    public String getStoreKey() {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    public void initFrom(String storeKey) {
        String[] split = storeKey.split("/");
        name = split[0];
        version = Integer.valueOf(split[1]);
    }

    public GetableClassEnum getType() {
        return GetableClassEnum.USER_TASK_DEF;
    }
}
