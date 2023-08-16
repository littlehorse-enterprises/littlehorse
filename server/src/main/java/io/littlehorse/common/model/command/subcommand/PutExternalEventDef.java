package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutExternalEventDefReply;
import io.littlehorse.common.model.meta.ExternalEventDefModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.LHResponseCodePb;
import io.littlehorse.sdk.common.proto.PutExternalEventDefPb;

public class PutExternalEventDef extends SubCommand<PutExternalEventDefPb> {

    public String name;
    public Integer retentionHours;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<PutExternalEventDefPb> getProtoBaseClass() {
        return PutExternalEventDefPb.class;
    }

    public PutExternalEventDefPb.Builder toProto() {
        PutExternalEventDefPb.Builder out = PutExternalEventDefPb.newBuilder();
        out.setName(name);

        if (retentionHours != null) out.setRetentionHours(retentionHours);

        return out;
    }

    public void initFrom(Message proto) {
        PutExternalEventDefPb p = (PutExternalEventDefPb) proto;
        name = p.getName();
        if (p.hasRetentionHours()) retentionHours = p.getRetentionHours();
    }

    public boolean hasResponse() {
        return true;
    }

    public PutExternalEventDefReply process(LHDAO dao, LHConfig config) {
        PutExternalEventDefReply out = new PutExternalEventDefReply();

        if (!LHUtil.isValidLHName(name)) {
            out.code = LHResponseCodePb.VALIDATION_ERROR;
            out.message = "ExternalEventDef name must be a valid hostname";
            return out;
        }

        ExternalEventDefModel oldVersion = dao.getExternalEventDef(name);
        if (oldVersion != null) {
            out.code = LHResponseCodePb.ALREADY_EXISTS_ERROR;
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

            out.code = LHResponseCodePb.OK;
            out.result = spec;
        }
        return out;
    }

    public static PutExternalEventDef fromProto(PutExternalEventDefPb p) {
        PutExternalEventDef out = new PutExternalEventDef();
        out.initFrom(p);
        return out;
    }
}
