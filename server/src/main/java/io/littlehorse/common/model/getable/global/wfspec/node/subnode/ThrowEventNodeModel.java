package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.SubNodeRun;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
import io.littlehorse.sdk.common.exception.LHSerdeError;
import io.littlehorse.sdk.common.proto.ThrowEventNode;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.Date;

public class ThrowEventNodeModel extends SubNode<ThrowEventNode> {
    @Override
    public ThrowEventNode.Builder toProto() {
        return null;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeError {}

    @Override
    public Class<? extends GeneratedMessageV3> getProtoBaseClass() {
        return null;
    }

    @Override
    public SubNodeRun<?> createSubNodeRun(Date time) {
        return null;
    }

    @Override
    public void validate() throws LHApiException {}
}
