package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.LHStoreType;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ExternalEventList;
import io.littlehorse.sdk.common.proto.ListExternalEventsRequest;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListExternalEventsReply;
import io.littlehorse.server.streams.lhinternalscan.util.BoundedObjectIdScanModel;
import io.littlehorse.server.streams.lhinternalscan.util.ScanBoundary;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.RequestExecutionContext;

public class ListExternalEventsRequestModel
        extends PublicScanRequest<
                ListExternalEventsRequest,
                ExternalEventList,
                ExternalEvent,
                ExternalEventModel,
                ListExternalEventsReply> {

    public WfRunIdModel wfRunId;

    public Class<ListExternalEventsRequest> getProtoBaseClass() {
        return ListExternalEventsRequest.class;
    }

    public ListExternalEventsRequest.Builder toProto() {
        return ListExternalEventsRequest.newBuilder().setWfRunId(wfRunId.toProto());
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListExternalEventsRequest p = (ListExternalEventsRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
    }

    @Override
    public GetableClassEnum getObjectType() {
        return GetableClassEnum.EXTERNAL_EVENT;
    }

    @Override
    public LHStoreType getStoreType() {
        return LHStoreType.CORE;
    }

    @Override
    public ScanBoundary<?> getScanBoundary(RequestExecutionContext ctx) throws LHApiException {
        return new BoundedObjectIdScanModel(getObjectType(), wfRunId);
    }
}
