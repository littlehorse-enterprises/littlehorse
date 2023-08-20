package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;

import io.littlehorse.common.dao.ReadOnlyMetadataStore;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.getable.core.externalevent.ExternalEventModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.ExternalEvent;
import io.littlehorse.sdk.common.proto.ListExternalEventsRequest;
import io.littlehorse.sdk.common.proto.ListExternalEventsResponse;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListExternalEventsReply;

public class ListExternalEventsRequestModel
        extends
        PublicScanRequest<ListExternalEventsRequest, ListExternalEventsResponse, ExternalEvent, ExternalEventModel, ListExternalEventsReply> {

    public String wfRunId;

    public Class<ListExternalEventsRequest> getProtoBaseClass() {
        return ListExternalEventsRequest.class;
    }

    public ListExternalEventsRequest.Builder toProto() {
        return ListExternalEventsRequest.newBuilder().setWfRunId(wfRunId);
    }

    public void initFrom(Message proto) {
        ListExternalEventsRequest p = (ListExternalEventsRequest) proto;
        wfRunId = p.getWfRunId();
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.EXTERNAL_EVENT;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataStore stores) throws LHValidationError {
        return TagStorageType.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new ObjectIdScanBoundaryStrategy(wfRunId);
    }
}
