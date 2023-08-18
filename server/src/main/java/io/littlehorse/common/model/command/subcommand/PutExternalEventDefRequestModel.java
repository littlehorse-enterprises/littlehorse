package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutExternalEventDefResponseModel;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCode;

public class PutExternalEventDefRequestModel
        extends SubCommand<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest> {

    public String name;
    public Integer retentionHours;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest> getProtoBaseClass() {
        return io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.class;
    }

    public io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.Builder toProto() {
        io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.Builder out =
                io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.newBuilder();
        out.setName(name);

        if (retentionHours != null) out.setRetentionHours(retentionHours);

        return out;
    }

    public void initFrom(Message proto) {
        io.littlehorse.sdk.common.proto.PutExternalEventDefRequest p =
                (io.littlehorse.sdk.common.proto.PutExternalEventDefRequest) proto;
        name = p.getName();
        if (p.hasRetentionHours()) retentionHours = p.getRetentionHours();
    }

    public boolean hasResponse() {
        return true;
    }

    public PutExternalEventDefResponseModel process(LHDAO dao, LHConfig config) {
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
                    retentionHours == null
                            ? config.getDefaultExternalEventRetentionHours()
                            : retentionHours;

            dao.putExternalEventDef(spec);

            out.code = LHResponseCode.OK;
            out.result = spec;
        }
        return out;
    }

    public static PutExternalEventDefRequestModel fromProto(
            io.littlehorse.sdk.common.proto.PutExternalEventDefRequest p) {
        PutExternalEventDefRequestModel out = new PutExternalEventDefRequestModel();
        out.initFrom(p);
        return out;
    }
}
