package io.littlehorse.common.model.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.model.ObjectId;
import io.littlehorse.common.model.meta.usertasks.UserTaskDef;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.UserTaskDefIdPb;
import io.littlehorse.jlib.common.proto.UserTaskDefPb;

public class UserTaskDefId
    extends ObjectId<UserTaskDefIdPb, UserTaskDefPb, UserTaskDef> {

    public String name;
    public int version;

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
        name = storeKey;
    }

    public GETableClassEnumPb getType() {
        return GETableClassEnumPb.USER_TASK_DEF;
    }
}
