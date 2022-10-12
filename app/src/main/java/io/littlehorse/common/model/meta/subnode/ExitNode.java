package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.model.meta.OutputSchema;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.wfrun.subnoderun.ExitRun;
import io.littlehorse.common.proto.ExitNodePb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.Date;

public class ExitNode extends SubNode<ExitNodePb> {

    public Class<ExitNodePb> getProtoBaseClass() {
        return ExitNodePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {}

    public ExitNodePb.Builder toProto() {
        return ExitNodePb.newBuilder();
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config) {
        node.outputSchema = new OutputSchema();
        node.outputSchema.outputType = VariableTypePb.VOID;
    }

    public ExitRun createRun(Date time) {
        return new ExitRun();
    }
}
