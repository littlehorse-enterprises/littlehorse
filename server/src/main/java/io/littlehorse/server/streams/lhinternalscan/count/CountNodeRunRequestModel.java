package io.littlehorse.server.streams.lhinternalscan.count;

import com.google.protobuf.Message;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.CountNodeRunRequest;
import io.littlehorse.server.streams.storeinternals.index.Attribute;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.List;

public class CountNodeRunRequestModel extends CountRequest<CountNodeRunRequest> {

    private String wfSpecName;
    private CountNodeRunRequest.CriteriaCase criteriaCase;

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        CountNodeRunRequest p = (CountNodeRunRequest) proto;
        wfSpecName = p.getWfSpecName();
        criteriaCase = p.getCriteriaCase();
    }

    @Override
    public CountNodeRunRequest.Builder toProto() {
        CountNodeRunRequest.Builder out = CountNodeRunRequest.newBuilder();
        if (wfSpecName != null) {
            out.setWfSpecName(wfSpecName);
        }
        return out;
    }

    @Override
    public Class<CountNodeRunRequest> getProtoBaseClass() {
        return CountNodeRunRequest.class;
    }

    @Override
    protected List<Attribute> countAttributes() {
        if (criteriaCase == CountNodeRunRequest.CriteriaCase.WF_SPEC_NAME) {
            return List.of(new Attribute("wfSpecName", wfSpecName));
        }
        throw new IllegalArgumentException("unsupported criteria case " + criteriaCase);
    }
}
