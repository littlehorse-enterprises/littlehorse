package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventRetentionPolicyModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;
import lombok.Getter;

@Getter
public class PutExternalEventDefRequestModel extends MetadataSubCommand<PutExternalEventDefRequest> {

    private String name;
    private ExternalEventRetentionPolicyModel retentionPolicy;
    private ReturnTypeModel contentType;

    @Override
    public Class<PutExternalEventDefRequest> getProtoBaseClass() {
        return PutExternalEventDefRequest.class;
    }

    @Override
    public PutExternalEventDefRequest.Builder toProto() {
        PutExternalEventDefRequest.Builder out = PutExternalEventDefRequest.newBuilder()
                .setName(name)
                .setRetentionPolicy(retentionPolicy.toProto())
                .setContentType(contentType.toProto());

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutExternalEventDefRequest p = (PutExternalEventDefRequest) proto;
        name = p.getName();
        retentionPolicy =
                LHSerializable.fromProto(p.getRetentionPolicy(), ExternalEventRetentionPolicyModel.class, context);
        contentType = LHSerializable.fromProto(p.getContentType(), ReturnTypeModel.class, context);
    }

    @Override
    public boolean hasResponse() {
        return true;
    }

    @Override
    public ExternalEventDef process(MetadataCommandExecution context) {
        MetadataManager metadataManager = context.metadataManager();

        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "ExternalEventDefName must be a valid hostname");
        }

        ExternalEventDefModel spec = new ExternalEventDefModel(name, retentionPolicy, contentType);

        metadataManager.put(spec);
        return spec.toProto().build();
    }

    public static PutExternalEventDefRequestModel fromProto(PutExternalEventDefRequest p, ExecutionContext context) {
        PutExternalEventDefRequestModel out = new PutExternalEventDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
