package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.proto.EntrypointNode;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class EntrypointNodeModel extends SubNode<EntrypointNode> {

    public Class<EntrypointNode> getProtoBaseClass() {
        return EntrypointNode.class;
    }

    public EntrypointNode.Builder toProto() {
        return EntrypointNode.newBuilder();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {}

    @Override
    public void validate() throws LHApiException {}

    public EntrypointRunModel createSubNodeRun(Date time) {
        return new EntrypointRunModel();
    }
}
