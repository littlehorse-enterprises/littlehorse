package io.littlehorse.common.model.command.subcommand;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.command.SubCommand;
import io.littlehorse.common.model.command.subcommandresponse.PutWfSpecReply;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.PutWfSpecPb;
import io.littlehorse.common.proto.PutWfSpecPbOrBuilder;
import io.littlehorse.server.CommandProcessorDao;

public class PutWfSpec extends SubCommand<PutWfSpecPb> {

    private WfSpec spec;

    public Class<PutWfSpecPb> getProtoBaseClass() {
        return PutWfSpecPb.class;
    }

    public PutWfSpecPb.Builder toProto() {
        PutWfSpecPb.Builder out = PutWfSpecPb.newBuilder();
        out.setSpec(spec.toProto());
        return out;
    }

    public void initFrom(MessageOrBuilder proto) {
        PutWfSpecPbOrBuilder p = (PutWfSpecPbOrBuilder) proto;
        spec = new WfSpec();
        spec.initFrom(p.getSpec());
    }

    public boolean hasResponse() {
        return true;
    }

    public PutWfSpecReply process(CommandProcessorDao dao, LHConfig config) {
        PutWfSpecReply out = new PutWfSpecReply();

        return out;
    }
}
