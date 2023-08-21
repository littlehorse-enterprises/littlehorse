package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.ListNodeRunsRequest;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunIdList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListNodeRunReply;

public class ListNodeRunsRequestModel
        extends PublicScanRequest<ListNodeRunsRequest, NodeRunIdList, NodeRun, NodeRunModel, ListNodeRunReply> {

    public String wfRunId;

    public Class<ListNodeRunsRequest> getProtoBaseClass() {
        return ListNodeRunsRequest.class;
    }

    public ListNodeRunsRequest.Builder toProto() {
        return ListNodeRunsRequest.newBuilder().setWfRunId(wfRunId);
    }

    public void initFrom(Message proto) {
        ListNodeRunsRequest p = (ListNodeRunsRequest) proto;
        wfRunId = p.getWfRunId();
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.NODE_RUN;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataStore stores) throws LHValidationError {
        return TagStorageType.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new ObjectIdScanBoundaryStrategy(wfRunId);
    }
}
