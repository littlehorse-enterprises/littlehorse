package io.littlehorse.common.model.meta.usertasks;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.objectId.UserTaskDefId;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.UserTaskDefPb;
import io.littlehorse.jlib.common.proto.UserTaskFieldPb;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserTaskDef extends GETable<UserTaskDefPb> {

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
        if (p.hasDescription()) description = p.getDescription();

        for (UserTaskFieldPb utf : p.getFieldsList()) {
            fields.add(LHSerializable.fromProto(utf, UserTaskField.class));
        }
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public UserTaskDefId getObjectId() {
        return new UserTaskDefId(name, version);
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        // TODO: Add validation
    }
}
