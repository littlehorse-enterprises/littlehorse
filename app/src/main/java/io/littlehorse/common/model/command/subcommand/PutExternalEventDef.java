package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHDAO;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutExternalEventDefReply;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.jlib.common.proto.LHResponseCodePb;
import io.littlehorse.jlib.common.proto.PutExternalEventDefPb;
import io.littlehorse.jlib.common.proto.PutExternalEventDefPbOrBuilder;

public class PutExternalEventDef extends SubCommand<PutExternalEventDefPb> {

    public String name;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<PutExternalEventDefPb> getProtoBaseClass() {
        return PutExternalEventDefPb.class;
    }

    public PutExternalEventDefPb.Builder toProto() {
        PutExternalEventDefPb.Builder out = PutExternalEventDefPb.newBuilder();
        out.setName(name);

        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PutExternalEventDefPbOrBuilder p = (PutExternalEventDefPbOrBuilder) proto;
        name = p.getName();
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

        ExternalEventDef spec = new ExternalEventDef();
        spec.name = name;

        ExternalEventDef oldVersion = dao.getExternalEventDef(name, null);
        if (oldVersion != null) {
            spec.version = oldVersion.version + 1;
        } else {
            spec.version = 0;
        }
        // TODO: Check for schema evolution here
        out.code = LHResponseCodePb.OK;
        out.result = spec;
        dao.putExternalEventDef(spec);
        return out;
    }

    public static PutExternalEventDef fromProto(PutExternalEventDefPbOrBuilder p) {
        PutExternalEventDef out = new PutExternalEventDef();
        out.initFrom(p);
        return out;
    }
}
