package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskFieldModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskField;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PutUserTaskDefRequestModel extends MetadataSubCommand<PutUserTaskDefRequest> {

    public String name;
    public String description;
    public List<UserTaskFieldModel> fields;

    public PutUserTaskDefRequestModel() {
        fields = new ArrayList<>();
    }

    public Class<PutUserTaskDefRequest> getProtoBaseClass() {
        return PutUserTaskDefRequest.class;
    }

    public PutUserTaskDefRequest.Builder toProto() {
        PutUserTaskDefRequest.Builder out = PutUserTaskDefRequest.newBuilder().setName(name);
        if (description != null) {
            out.setDescription(description);
        }
        for (UserTaskFieldModel f : fields) {
            out.addFields(f.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutUserTaskDefRequest p = (PutUserTaskDefRequest) proto;
        name = p.getName();
        if (p.hasDescription()) description = p.getDescription();
        for (UserTaskField utfpb : p.getFieldsList()) {
            fields.add(LHSerializable.fromProto(utfpb, UserTaskFieldModel.class, context));
        }
    }

    public String getPartitionkey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public boolean hasResponse() {
        return true;
    }

    public UserTaskDef process(MetadataProcessorDAO dao, LHServerConfig config) {
        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "UserTaskDefName must be a valid hostname");
        }

        UserTaskDefModel spec = new UserTaskDefModel();
        spec.name = name;
        spec.description = description;
        spec.fields = fields;
        spec.createdAt = new Date();

        UserTaskDefModel oldVersion = dao.getUserTaskDef(name, null);
        if (oldVersion != null) {
            spec.version = oldVersion.version + 1;
        } else {
            spec.version = 0;
        }

        spec.validate(dao, config);
        dao.put(spec);

        return spec.toProto().build();
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }
}
