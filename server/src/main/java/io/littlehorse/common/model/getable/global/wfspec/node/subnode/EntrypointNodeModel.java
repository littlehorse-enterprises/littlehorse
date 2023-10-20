package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.proto.EntrypointNode;
import java.util.Date;

public class EntrypointNodeModel extends SubNode<EntrypointNode> {

    public Class<EntrypointNode> getProtoBaseClass() {
        return EntrypointNode.class;
    }

    public EntrypointNode.Builder toProto() {
        return EntrypointNode.newBuilder();
    }

    public void initFrom(Message proto) {}

    public void validate(ReadOnlyMetadataProcessorDAO readOnlyDao, LHServerConfig config) throws LHApiException {}

    public EntrypointRunModel createSubNodeRun(Date time) {
        return new EntrypointRunModel();
    }
}
