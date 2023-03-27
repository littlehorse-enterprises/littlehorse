package io.littlehorse.common.model.meta.subnode;

import com.google.protobuf.Message;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.wfrun.subnoderun.EntrypointRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.NopNodePb;
import java.util.Date;

public class NopNode extends SubNode<NopNodePb> {

    public Class<NopNodePb> getProtoBaseClass() {
        return NopNodePb.class;
    }

    public NopNodePb.Builder toProto() {
        return NopNodePb.newBuilder();
    }

    public void initFrom(Message proto) {}

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {}

    public EntrypointRun createRun(Date time) {
        return new EntrypointRun();
    }
}
