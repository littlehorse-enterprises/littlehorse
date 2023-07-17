package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskDefIdPb;
import io.littlehorse.sdk.common.proto.UserTaskDefPb;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskDefId
    extends ObjectId<UserTaskDefIdPb, UserTaskDefPb, UserTaskDef> {

    private String name;
    private int version;

    public UserTaskDefId() {}

    public UserTaskDefId(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public Class<UserTaskDefIdPb> getProtoBaseClass() {
        return UserTaskDefIdPb.class;
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public void initFrom(Message proto) {
        UserTaskDefIdPb p = (UserTaskDefIdPb) proto;
        name = p.getName();
        version = p.getVersion();
    }

    public UserTaskDefIdPb.Builder toProto() {
        UserTaskDefIdPb.Builder out = UserTaskDefIdPb
            .newBuilder()
            .setName(name)
            .setVersion(version);
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

    public GetableClassEnumPb getType() {
        return GetableClassEnumPb.USER_TASK_DEF;
    }
}
