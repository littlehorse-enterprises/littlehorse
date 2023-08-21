package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.dao.MetadataProcessorDAO;
import io.littlehorse.common.model.command.subcommandresponse.PutExternalEventDefResponseModel;
import io.littlehorse.common.model.getable.global.externaleventdef.ExternalEventDefModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;
import io.littlehorse.sdk.common.proto.PutExternalEventDefRequest;

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

    public void initFrom(Message proto) {
        PutExternalEventDefRequest p = (PutExternalEventDefRequest) proto;
        name = p.getName();
        if (p.hasRetentionHours()) retentionHours = p.getRetentionHours();
    }

    public boolean hasResponse() {
        return true;
    }

    public PutExternalEventDefResponseModel process(MetadataProcessorDAO dao, LHConfig config) {
        PutExternalEventDefResponseModel out = new PutExternalEventDefResponseModel();

        if (!LHUtil.isValidLHName(name)) {
            out.code = LHResponseCode.VALIDATION_ERROR;
            out.message = "ExternalEventDef name must be a valid hostname";
            return out;
        }

        ExternalEventDefModel oldVersion = dao.getExternalEventDef(name);
        if (oldVersion != null) {
            out.code = LHResponseCode.ALREADY_EXISTS_ERROR;
            out.message = "ExternalEventDef already exists and is immutable.";
            out.result = oldVersion;
        } else {
            ExternalEventDefModel spec = new ExternalEventDefModel();
            spec.name = name;
            spec.retentionHours =
                    retentionHours == null ? config.getDefaultExternalEventRetentionHours() : retentionHours;

            dao.put(spec);

            out.code = LHResponseCode.OK;
            out.result = spec;
        }
        return out;
    }

    public static PutExternalEventDefRequestModel fromProto(PutExternalEventDefRequest p) {
        PutExternalEventDefRequestModel out = new PutExternalEventDefRequestModel();
        out.initFrom(p);
        return out;
    }
}
