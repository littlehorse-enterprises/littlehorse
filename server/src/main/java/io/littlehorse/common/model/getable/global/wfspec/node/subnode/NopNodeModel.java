package io.littlehorse.common.model.getable.global.wfspec.node.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.wfrun.subnoderun.EntrypointRunModel;
import io.littlehorse.common.model.getable.global.wfspec.node.SubNode;
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

    public void validate(ReadOnlyMetadataStore stores, LHConfig config) throws LHApiException {}

    public EntrypointRunModel createSubNodeRun(Date time) {
        return new EntrypointRunModel();
    }
}
