package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.NopNode;
import java.util.Date;

public class NopNodeModel extends SubNode<NopNode> {

    public Class<NopNode> getProtoBaseClass() {
        return NopNode.class;
    }

    public NopNode.Builder toProto() {
        return NopNode.newBuilder();
    }

    public void initFrom(Message proto) {}

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {}

    public EntrypointRunModel createSubNodeRun(Date time) {
        return new EntrypointRunModel();
    }
}
