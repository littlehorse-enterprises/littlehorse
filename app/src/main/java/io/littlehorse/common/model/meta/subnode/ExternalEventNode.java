package io.littlehorse.common.model.meta.subnode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.SubNode;
import io.littlehorse.common.model.wfrun.subnoderun.ExternalEventRun;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.jlib.common.proto.ExternalEventNodePb;
import io.littlehorse.jlib.common.proto.ExternalEventNodePbOrBuilder;
import java.util.Date;

public class ExternalEventNode extends SubNode<ExternalEventNodePb> {

    public String externalEventDefName;

    @JsonIgnore
    public ExternalEventDef externalEventDef;

    public ExternalEventNode() {}

    public Class<ExternalEventNodePb> getProtoBaseClass() {
        return ExternalEventNodePb.class;
    }

    public void initFrom(MessageOrBuilder proto) {
        ExternalEventNodePbOrBuilder p = (ExternalEventNodePbOrBuilder) proto;
        externalEventDefName = p.getExternalEventDefName();
    }

    public ExternalEventNodePb.Builder toProto() {
        ExternalEventNodePb.Builder out = ExternalEventNodePb
            .newBuilder()
            .setExternalEventDefName(externalEventDefName);
        return out;
    }

    public void validate(LHGlobalMetaStores stores, LHConfig config)
        throws LHValidationError {
        // Want to be able to release new versions of ExternalEventDef's and have old
        // workflows automatically use the new version. We will enforce schema
        // compatibility rules on the EED to ensure that this isn't an issue.
        ExternalEventDef eed = stores.getExternalEventDef(externalEventDefName, null);

        if (eed == null) {
            throw new LHValidationError(
                null,
                "Refers to nonexistent ExternalEventDef " + externalEventDefName
            );
        }
    }

    public ExternalEventRun createRun(Date time) {
        ExternalEventRun out = new ExternalEventRun();
        out.externalEventDefName = externalEventDefName;

        return out;
    }
}
