package io.littlehorse.server.streams.lhinternalscan.count;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountNodeRunRequest;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;

public class CountNodeRunRequestModel extends CountRequest<CountNodeRunRequest> {

    private String wfSpecName;
    private Integer wfSpecMajorVersion;
    private Integer wfSpecRevision;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CountNodeRunRequest p = (CountNodeRunRequest) proto;
        wfSpecName = p.getWfSpecName();
        if (p.hasWfSpecMajorVersion()) wfSpecMajorVersion = p.getWfSpecMajorVersion();
        if (p.hasWfSpecRevision()) wfSpecRevision = p.getWfSpecRevision();
    }

    @Override
    public CountNodeRunRequest.Builder toProto() {
        CountNodeRunRequest.Builder out = CountNodeRunRequest.newBuilder();
        if (wfSpecName != null) out.setWfSpecName(wfSpecName);
        if (wfSpecMajorVersion != null) out.setWfSpecMajorVersion(wfSpecMajorVersion);
        if (wfSpecRevision != null) out.setWfSpecRevision(wfSpecRevision);
        return out;
    }

    @Override
    public Class<CountNodeRunRequest> getProtoBaseClass() {
        return CountNodeRunRequest.class;
    }

    @Override
    protected GetableClassEnum getObjectType() {
        return GetableClassEnum.NODE_RUN;
    }

    @Override
    protected List<Attribute> countAttributes() {
        if (wfSpecName == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "wfSpecName is required");
        }

        List<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("wfSpecName", wfSpecName));

        if (wfSpecMajorVersion != null) {
            attributes.add(new Attribute("majorVersion", String.valueOf(wfSpecMajorVersion)));
            if (wfSpecRevision != null) {
                attributes.add(new Attribute("revision", String.valueOf(wfSpecRevision)));
            }
        }

        return attributes;
    }
}
