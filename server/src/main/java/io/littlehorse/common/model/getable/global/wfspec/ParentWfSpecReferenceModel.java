package io.littlehorse.common.model.getable.global.wfspec;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.sdk.common.proto.WfSpec.ParentWfSpecReference;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import lombok.Getter;

@Getter
public class ParentWfSpecReferenceModel extends LHSerializable<ParentWfSpecReference> {

    private String wfSpecName;
    private int wfSpecMajorVersion;

    public ParentWfSpecReferenceModel(String wfSpecName, int wfSpecMajorVersion) {
        this.wfSpecName = wfSpecName;
        this.wfSpecMajorVersion = wfSpecMajorVersion;
    }

    @Override
    public Class<ParentWfSpecReference> getProtoBaseClass() {
        return ParentWfSpecReference.class;
    }

    @Override
    public ParentWfSpecReference.Builder toProto() {
        ParentWfSpecReference.Builder out =
                ParentWfSpecReference.newBuilder().setWfSpecName(wfSpecName).setWfSpecMajorVersion(wfSpecMajorVersion);
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext ctx) {
        ParentWfSpecReference p = (ParentWfSpecReference) proto;
        wfSpecName = p.getWfSpecName();
        wfSpecMajorVersion = p.getWfSpecMajorVersion();
    }
}
