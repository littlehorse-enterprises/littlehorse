package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskDefPb;
import io.littlehorse.sdk.common.proto.UserTaskFieldPb;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import io.littlehorse.server.streamsimpl.storeinternals.utils.StoreUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class UserTaskDef extends Getable<UserTaskDefPb> {

    public String name;
    public Date createdAt;
    public List<UserTaskField> fields;
    public String description;
    public int version;

    public UserTaskDef() {
        fields = new ArrayList<>();
    }

    public Class<UserTaskDefPb> getProtoBaseClass() {
        return UserTaskDefPb.class;
    }

    public UserTaskDefPb.Builder toProto() {
        UserTaskDefPb.Builder out = UserTaskDefPb
            .newBuilder()
            .setName(name)
            .setCreatedAt(LHUtil.fromDate(createdAt))
            .setVersion(version);

        if (description != null) out.setDescription(description);

        for (UserTaskField utf : fields) {
            out.addFields(utf.toProto());
        }
        return out;
    }

    public void initFrom(Message proto) {
        UserTaskDefPb p = (UserTaskDefPb) proto;
        name = p.getName();
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        version = p.getVersion();
        if (p.hasDescription()) description = p.getDescription();

        for (UserTaskFieldPb utf : p.getFieldsList()) {
            fields.add(LHSerializable.fromProto(utf, UserTaskField.class));
        }
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public UserTaskDefId getObjectId() {
        return new UserTaskDefId(name, version);
    }

    @Override
    public List<IndexedField> getIndexValues(
        String key,
        Optional<TagStorageTypePb> tagStorageTypePb
    ) {
        return List.of();
    }

    public static String getFullPrefixByName(String name) {
        // TODO MVP-140: Remove StoreUtils.java
        return StoreUtils.getFullStoreKey(name + "/", UserTaskDef.class);
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        // TODO: Add validation
    }
}
