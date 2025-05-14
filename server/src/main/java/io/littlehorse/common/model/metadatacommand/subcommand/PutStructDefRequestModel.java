package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.util.StructDefUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutStructDefRequest.AllowedStructDefUpdateType;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import java.util.Date;

public class PutStructDefRequestModel extends MetadataSubCommand<PutStructDefRequest> {

    private String name;
    private InlineStructDefModel structDef;
    private AllowedStructDefUpdateType allowedUpdateType;

    public PutStructDefRequestModel() {}

    @Override
    public PutStructDefRequest.Builder toProto() {
        PutStructDefRequest.Builder out = PutStructDefRequest.newBuilder()
                .setName(name)
                .setStructDef(structDef.toProto())
                .setAllowedUpdates(allowedUpdateType);

        return out;
    }

    @Override
    public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
        PutStructDefRequest proto = (PutStructDefRequest) p;

        name = proto.getName();
        structDef = LHSerializable.fromProto(proto.getStructDef(), InlineStructDefModel.class, context);
        allowedUpdateType = proto.getAllowedUpdates();
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public Message process(MetadataCommandExecution context) {
        MetadataManager metadataManager = context.metadataManager();
        this.validate();

        StructDefModel spec = new StructDefModel(context);
        spec.setId(new StructDefIdModel(name, 0));
        spec.setStructDef(structDef);
        spec.setCreatedAt(new Date());

        StructDefModel oldVersion = context.service().getStructDef(name, null);

        if (oldVersion != null) {
            if (StructDefUtil.equals(spec, oldVersion)) {
                return oldVersion.toProto().build();
            }
        }

        verifyUpdateType(allowedUpdateType, spec, oldVersion);
        metadataManager.put(spec);
        return structDef.toProto().build();
    }

    private void verifyUpdateType(
            AllowedStructDefUpdateType allowedUpdateType, StructDefModel spec, StructDefModel oldSpec) {
        switch (allowedUpdateType) {
            case FULLY_COMPATIBLE_SCHEMA_UPDATES:
                if (StructDefUtil.hasBreakingChanges(spec, oldSpec)) {
                    throw new LHApiException(
                            Status.FAILED_PRECONDITION, "The resulting StructDef has a breaking change");
                }
                break;
            case NO_SCHEMA_UPDATES:
                throw new LHApiException(Status.ALREADY_EXISTS, "StructDef already exists.");
            case UNRECOGNIZED:
            default:
                break;
        }
    }

    @Override
    public Class<PutStructDefRequest> getProtoBaseClass() {
        return PutStructDefRequest.class;
    }

    public void validate() {
        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "StructDef name must be a valid hostname");
        }

        structDef.validate();
    }

    public static PutStructDefRequestModel fromProto(PutStructDefRequest p, ExecutionContext context) {
        PutStructDefRequestModel out = new PutStructDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
