package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ReturnTypeModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.proto.NopNode;
import io.littlehorse.server.streams.storeinternals.ReadOnlyMetadataManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.Optional;

public class NopNodeModel extends SubNode<NopNode> {

    @Override
    public Class<NopNode> getProtoBaseClass() {
        return NopNode.class;
    }

    @Override
    public NopNode.Builder toProto() {
        return NopNode.newBuilder();
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {}

    @Override
    public void validate(MetadataProcessorContext ctx) {}

    @Override
    public EntrypointRunModel createSubNodeRun(Date time, CoreProcessorContext processorContext) {
        return new EntrypointRunModel();
    }

    @Override
    public Optional<ReturnTypeModel> getOutputType(ReadOnlyMetadataManager manager) {
        // No output.
        return Optional.of(new ReturnTypeModel());
    }
}
