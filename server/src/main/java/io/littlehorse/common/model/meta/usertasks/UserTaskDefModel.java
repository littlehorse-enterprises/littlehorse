package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.Getable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.UserTaskDefIdModel;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskField;
import io.littlehorse.server.streamsimpl.storeinternals.GetableIndex;
import io.littlehorse.server.streamsimpl.storeinternals.IndexedField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

public class UserTaskDefModel extends Getable<UserTaskDef> {

    public String name;
    public Date createdAt;

    @Getter
    public List<UserTaskFieldModel> fields;

    public String description;
    public int version;

    public UserTaskDefModel() {
        fields = new ArrayList<>();
    }

    public Class<UserTaskDef> getProtoBaseClass() {
        return UserTaskDef.class;
    }

    public UserTaskDef.Builder toProto() {
        UserTaskDef.Builder out = UserTaskDef.newBuilder()
                .setName(name)
                .setCreatedAt(LHUtil.fromDate(createdAt))
                .setVersion(version);

        if (description != null) out.setDescription(description);

        for (UserTaskFieldModel utf : fields) {
            out.addFields(utf.toProto());
        }
        return out;
    }

    public void initFrom(Message proto) {
        UserTaskDef p = (UserTaskDef) proto;
        name = p.getName();
        createdAt = LHUtil.fromProtoTs(p.getCreatedAt());
        version = p.getVersion();
        if (p.hasDescription()) description = p.getDescription();

        for (UserTaskField utf : p.getFieldsList()) {
            fields.add(LHSerializable.fromProto(utf, UserTaskFieldModel.class));
        }
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public List<GetableIndex<? extends Getable<?>>> getIndexConfigurations() {
        return List.of();
    }

    public UserTaskDefIdModel getObjectId() {
        return new UserTaskDefIdModel(name, version);
    }

    @Override
    public List<IndexedField> getIndexValues(String key, Optional<TagStorageType> tagStorageType) {
        return List.of();
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config) throws LHValidationError {
        // TODO: Add validation
    }
}
