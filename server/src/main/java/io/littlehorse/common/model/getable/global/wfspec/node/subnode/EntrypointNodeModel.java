package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.proto.EntrypointNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.Optional;

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
    public void validate(MetadataProcessorContext ctx) throws LHApiException {}

    @Override
    public EntrypointRunModel createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        return new EntrypointRunModel();
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        // This means that we KNOW that there is no output. If it were set to `Optional.empty()`, that means we
        // DONT know what the output type is.
        return Optional.of(new ReturnTypeModel());
    }
}
