package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.proto.NopNode;
import java.util.Date;

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
    public void initFrom(Message proto) {}

    @Override
    public void validate(ReadOnlyMetadataProcessorDAO readOnlyDao, LHServerConfig config) throws LHApiException {}

    @Override
    public EntrypointRunModel createSubNodeRun(Date time) {
        return new EntrypointRunModel();
    }
}
