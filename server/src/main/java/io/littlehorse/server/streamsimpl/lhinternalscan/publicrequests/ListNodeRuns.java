package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.wfrun.NodeRunModel;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.ListNodeRunsPb;
import io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListNodeRunsReply;

public class ListNodeRuns
    extends PublicScanRequest<ListNodeRunsPb, ListNodeRunsReplyPb, NodeRun, NodeRunModel, ListNodeRunsReply> {

    public String wfRunId;

    public Class<ListNodeRunsPb> getProtoBaseClass() {
        return ListNodeRunsPb.class;
    }

    public ListNodeRunsPb.Builder toProto() {
        return ListNodeRunsPb.newBuilder().setWfRunId(wfRunId);
    }

    public void initFrom(Message proto) {
        ListNodeRunsPb p = (ListNodeRunsPb) proto;
        wfRunId = p.getWfRunId();
    }

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.NODE_RUN;
    }

    @Override
    public TagStorageTypePb indexTypeForSearch(LHGlobalMetaStores stores)
        throws LHValidationError {
        return TagStorageTypePb.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new ObjectIdScanBoundaryStrategy(wfRunId);
    }
}
