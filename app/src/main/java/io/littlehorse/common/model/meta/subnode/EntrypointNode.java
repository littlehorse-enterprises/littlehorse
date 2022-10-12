package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.OutputSchema;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.wfrun.subnoderun.EntrypointRun;
import io.littlehorse.common.proto.EntrypointNodePb;
import io.littlehorse.common.proto.VariableTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import java.util.Date;

public class EntrypointNode extends SubNode<EntrypointNodePb> {

    public Class<EntrypointNodePb> getProtoBaseClass() {
        return EntrypointNodePb.class;
    }

    public EntrypointNodePb.Builder toProto() {
        return EntrypointNodePb.newBuilder();
    }

    public void initFrom(MessageOrBuilder proto) {}

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        node.outputSchema = new OutputSchema();
        node.outputSchema.outputType = VariableTypePb.VOID;
    }

    public EntrypointRun createRun(Date time) {
        return new EntrypointRun();
    }
}
