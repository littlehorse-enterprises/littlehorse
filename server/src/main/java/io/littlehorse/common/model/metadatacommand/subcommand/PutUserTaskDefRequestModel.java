package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.formdef.FormDefModel;
import io.littlehorse.common.model.getable.global.formdef.FormDefReferenceModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskDefModel;
import io.littlehorse.common.model.getable.global.wfspec.node.subnode.usertasks.UserTaskFieldModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.getable.objectId.UserTaskDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.util.UserTaskUtil;
import io.littlehorse.sdk.common.proto.PutUserTaskDefRequest;
import io.littlehorse.sdk.common.proto.UserTaskDef;
import io.littlehorse.sdk.common.proto.UserTaskField;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PutUserTaskDefRequestModel extends MetadataSubCommand<PutUserTaskDefRequest> {

    public String name;
    public String description;
    public List<UserTaskFieldModel> fields;
    private StructDefIdModel resultStructDefId;
    private FormDefReferenceModel formDef;

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
        if (resultStructDefId != null) {
            out.setResultStructDefId(resultStructDefId.toProto());
        }
        if (formDef != null) out.setFormDef(formDef.toProto());
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
        if (p.hasResultStructDefId()) {
            resultStructDefId = StructDefIdModel.fromProto(p.getResultStructDefId(), context);
        }
        if (p.hasFormDef()) {
            formDef = LHSerializable.fromProto(p.getFormDef(), FormDefReferenceModel.class, context);
        }
        for (UserTaskField utfpb : p.getFieldsList()) {
            fields.add(LHSerializable.fromProto(utfpb, UserTaskFieldModel.class, context));
        }
    }

    public String getPartitionkey() {
        return LHConstants.META_PARTITION_KEY;
    }

    @Override
    public UserTaskDef process(MetadataProcessorContext context) {
        MetadataManager metadataManager = context.metadataManager();
        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "UserTaskDefName must be a valid hostname");
        }
        if (!fields.isEmpty() && resultStructDefId != null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "UserTaskDef cannot define both fields and result_struct_def_id");
        }
        StructDefModel resultStructDef = resultStructDefId == null ? null : metadataManager.get(resultStructDefId);
        if (resultStructDefId != null && resultStructDef == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "StructDef %s does not exist".formatted(resultStructDefId));
        }
        if (formDef != null) validateFormDef(context);

        UserTaskDefModel spec = new UserTaskDefModel();
        spec.name = name;
        spec.description = description;
        spec.fields = fields;
        spec.setResultStructDefId(resultStructDefId);
        spec.setFormDef(formDef);
        spec.createdAt = new Date();

        UserTaskDefModel oldVersion =
                metadataManager.getLastFromPrefix(UserTaskDefIdModel.getPrefix(name), UserTaskDefModel.class);
        if (oldVersion != null) {
            if (UserTaskUtil.equals(spec, oldVersion))
                return oldVersion.toProto().build();
            spec.version = oldVersion.version + 1;
        } else {
            spec.version = 0;
        }

        metadataManager.put(spec);

        return spec.toProto().build();
    }

    private void validateFormDef(MetadataProcessorContext context) {
        if (resultStructDefId == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "A FormDef requires result_struct_def_id");
        }
        FormDefModel resolved = context.service().getFormDef(formDef);
        if (resolved == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "FormDef %s does not exist".formatted(formDef.getName()));
        }
        // Versions may differ: the UserTaskDef sets a minimum StructDef version, the FormDef an exact one.
        if (!resolved.getResultStructDefId().getName().equals(resultStructDefId.getName())) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "FormDef %s describes StructDef %s, not %s"
                            .formatted(
                                    formDef.getName(),
                                    resolved.getResultStructDefId().getName(),
                                    resultStructDefId.getName()));
        }
    }

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }
}
