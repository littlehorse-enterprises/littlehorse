package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.noderun.NodeRunModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.ListNodeRunsRequest;
import io.littlehorse.sdk.common.proto.NodeRun;
import io.littlehorse.sdk.common.proto.NodeRunList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListNodeRunReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ListNodeRunsRequestModel
        extends PublicScanRequest<ListNodeRunsRequest, NodeRunList, NodeRun, NodeRunModel, ListNodeRunReply> {

    public WfRunIdModel wfRunId;

    public Class<ListNodeRunsRequest> getProtoBaseClass() {
        return ListNodeRunsRequest.class;
    }

    public ListNodeRunsRequest.Builder toProto() {
        return ListNodeRunsRequest.newBuilder().setWfRunId(wfRunId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListNodeRunsRequest p = (ListNodeRunsRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.NODE_RUN;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return ObjectIdScanBoundaryStrategy.from(wfRunId, getObjectType());
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }
}
