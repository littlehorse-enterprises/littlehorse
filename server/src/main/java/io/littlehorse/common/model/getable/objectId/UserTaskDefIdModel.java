package io.littlehorse.common.model.getable.objectId;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.MetadataId;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskDefId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTaskDefIdModel extends MetadataId<UserTaskDefId, UserTaskDef, UserTaskDefModel> {

    private String name;
    private int version;

    public UserTaskDefIdModel() {}

    public UserTaskDefIdModel(String name, int version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public Class<UserTaskDefId> getProtoBaseClass() {
        return UserTaskDefId.class;
    }

    @Override
    public void initFrom(Message proto) {
        UserTaskDefId p = (UserTaskDefId) proto;
        name = p.getName();
        version = p.getVersion();
    }

    @Override
    public UserTaskDefId.Builder toProto() {
        UserTaskDefId.Builder out = UserTaskDefId.newBuilder().setName(name).setVersion(version);
        return out;
    }

    @Override
    public String toString() {
        return LHUtil.getCompositeId(name, LHUtil.toLHDbVersionFormat(version));
    }

    @Override
    public void initFromString(String storeKey) {
        String[] split = storeKey.split("/");
        name = split[0];
        version = Integer.valueOf(split[1]);
    }

    // TODO: This leaks from Storeable.java
    public static String getPrefix(String name) {
        return GetableClassEnum.USER_TASK_DEF.getNumber() + "/" + name + "/";
    }

    @Override
    public GetableClassEnum getType() {
        return GetableClassEnum.USER_TASK_DEF;
    }
}
