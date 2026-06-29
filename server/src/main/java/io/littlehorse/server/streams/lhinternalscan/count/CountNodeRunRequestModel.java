package io.littlehorse.server.streams.lhinternalscan.count;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountNodeRunRequest;
import io.littlehorse.sdk.common.proto.CountNodeRunRequest.WfSpecFilter;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.ArrayList;
import java.util.List;

public class CountNodeRunRequestModel extends CountRequest<CountNodeRunRequest> {

    private String wfSpecName;
    private Integer wfSpecMajorVersion;
    private Integer wfSpecRevision;
    private CountNodeRunRequest.FilterCase filterCase;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CountNodeRunRequest p = (CountNodeRunRequest) proto;
        this.filterCase = p.getFilterCase();
        if (filterCase == CountNodeRunRequest.FilterCase.WF_SPEC_FILTER) {
            WfSpecFilter filter = p.getWfSpecFilter();
            wfSpecName = filter.getWfSpecName();
            if (filter.hasWfSpecMajorVersion()) wfSpecMajorVersion = filter.getWfSpecMajorVersion();
            if (filter.hasWfSpecRevision()) wfSpecRevision = filter.getWfSpecRevision();
        }
    }

    @Override
    public CountNodeRunRequest.Builder toProto() {
        CountNodeRunRequest.Builder out = CountNodeRunRequest.newBuilder();
        if (wfSpecName != null) {
            WfSpecFilter.Builder filter = WfSpecFilter.newBuilder().setWfSpecName(wfSpecName);
            if (wfSpecMajorVersion != null) filter.setWfSpecMajorVersion(wfSpecMajorVersion);
            if (wfSpecRevision != null) filter.setWfSpecRevision(wfSpecRevision);
            out.setWfSpecFilter(filter);
        }
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
        if (filterCase == CountNodeRunRequest.FilterCase.FILTER_NOT_SET) {
            return List.of(new Attribute("all", "all"));
        }

        if (wfSpecRevision != null && wfSpecMajorVersion == null) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "wfSpecRevision requires wfSpecMajorVersion to be set");
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
