package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.InlineStructDefUtil;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.StructDefCompatibilityType;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PutStructDefRequestModel extends MetadataSubCommand<PutStructDefRequest> {

    private String name;
    private String description;
    private InlineStructDefModel structDef;
    private StructDefCompatibilityType allowedUpdateType;

    public PutStructDefRequestModel() {}

    @Override
    public PutStructDefRequest.Builder toProto() {
        PutStructDefRequest.Builder out = PutStructDefRequest.newBuilder()
                .setName(name)
                .setStructDef(structDef.toProto())
                .setAllowedUpdates(allowedUpdateType);

        if (description != null) {
            out.setDescription(description);
        }

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        PutStructDefRequest proto = (PutStructDefRequest) p;

        name = proto.getName();
        structDef = LHSerializable.fromProto(proto.getStructDef(), InlineStructDefModel.class, context);
        allowedUpdateType = proto.getAllowedUpdates();

        if (proto.hasDescription()) {
            description = proto.getDescription();
        }
    }

    @Override
    public Message process(MetadataProcessorContext context) {
        MetadataManager metadataManager = context.metadataManager();

        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "StructDef name must be a valid hostname");
        }

        structDef.validate();

        StructDefModel spec = new StructDefModel();
        spec.setStructDef(structDef);
        spec.setCreatedAt(new Date());

        if (description != null) {
            spec.setDescription(description);
        }

        StructDefModel latestVersion = context.service().getStructDef(name, null);

        if (latestVersion == null) {
            spec.setId(new StructDefIdModel(name, 0));
        } else {
            if (InlineStructDefUtil.equals(spec.getStructDef(), latestVersion.getStructDef())) {
                return latestVersion.toProto().build();
            }

            verifyUpdateType(allowedUpdateType, spec.getStructDef(), latestVersion.getStructDef());
            spec.setId(latestVersion.getObjectId().bumpVersion());
        }

        metadataManager.put(spec);
        return spec.toProto().build();
    }

    private void verifyUpdateType(
            StructDefCompatibilityType allowedUpdateType, InlineStructDefModel newSpec, InlineStructDefModel oldSpec) {
        Set<String> changedFields = new HashSet<>();

        switch (allowedUpdateType) {
            case FULLY_COMPATIBLE_SCHEMA_UPDATES:
                changedFields.addAll(InlineStructDefUtil.getIncompatibleFields(
                        StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES, newSpec, oldSpec));
                break;
            case NO_SCHEMA_UPDATES:
                changedFields.addAll(InlineStructDefUtil.getIncompatibleFields(
                        StructDefCompatibilityType.NO_SCHEMA_UPDATES, newSpec, oldSpec));
            case UNRECOGNIZED:
            default:
                break;
        }

        if (changedFields.isEmpty()) return;

        StringBuilder errorMessage = new StringBuilder("Incompatible StructDef evolution on field(s): ");
        errorMessage.append(changedFields.toString());
        errorMessage.append(String.format(" using %s compatibility type", allowedUpdateType.toString()));
        throw new LHApiException(Status.INVALID_ARGUMENT, errorMessage.toString());
    }

    @Override
    public Class<PutStructDefRequest> getProtoBaseClass() {
        return PutStructDefRequest.class;
    }

    public static PutStructDefRequestModel fromProto(PutStructDefRequest p, ExecutionContext context) {
        PutStructDefRequestModel out = new PutStructDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
