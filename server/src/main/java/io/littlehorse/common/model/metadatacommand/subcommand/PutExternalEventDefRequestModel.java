package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.ExternalEventDef;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class PutExternalEventDefRequestModel extends MetadataSubCommand<PutExternalEventDefRequest> {

    public String name;
    public Integer retentionHours;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<PutExternalEventDefRequest> getProtoBaseClass() {
        return PutExternalEventDefRequest.class;
    }

    public PutExternalEventDefRequest.Builder toProto() {
        PutExternalEventDefRequest.Builder out = PutExternalEventDefRequest.newBuilder();
        out.setName(name);

        if (retentionHours != null) out.setRetentionHours(retentionHours);

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutExternalEventDefRequest p = (PutExternalEventDefRequest) proto;
        name = p.getName();
        if (p.hasRetentionHours()) retentionHours = p.getRetentionHours();
    }

    public boolean hasResponse() {
        return true;
    }

    public ExternalEventDef process(MetadataProcessorDAO dao, LHServerConfig config) {

        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "ExternalEventDefName must be a valid hostname");
        }

        ExternalEventDefModel oldVersion = dao.getExternalEventDef(name);
        if (oldVersion != null) {
            throw new LHApiException(Status.ALREADY_EXISTS, "ExternalEventDef already exists and is immutable.");
        }
        ExternalEventDefModel spec = new ExternalEventDefModel();
        spec.name = name;
        spec.retentionHours = retentionHours == null ? config.getDefaultExternalEventRetentionHours() : retentionHours;

        dao.put(spec);
        return spec.toProto().build();
    }

    public static PutExternalEventDefRequestModel fromProto(PutExternalEventDefRequest p, ExecutionContext context) {
        PutExternalEventDefRequestModel out = new PutExternalEventDefRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
