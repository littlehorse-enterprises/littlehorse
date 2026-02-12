package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.proto.BookmarkPb;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.ScanResultTypePb;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.sdk.common.proto.ListVariablesRequest;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableList;
import io.littlehorse.server.streams.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streams.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streams.lhinternalscan.publicsearchreplies.ListVariablesReply;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class ListVariablesRequestModel
        extends PublicScanRequest<ListVariablesRequest, VariableList, Variable, VariableModel, ListVariablesReply> {

    public WfRunIdModel wfRunId;

    public Class<ListVariablesRequest> getProtoBaseClass() {
        return ListVariablesRequest.class;
    }

    public ListVariablesRequest.Builder toProto() {
        ListVariablesRequest.Builder out = ListVariablesRequest.newBuilder().setWfRunId(wfRunId.toProto());
        if (bookmark != null) {
            out.setBookmark(bookmark.toByteString());
        }
        if (limit != null) {
            out.setLimit(limit);
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        ListVariablesRequest p = (ListVariablesRequest) proto;
        wfRunId = LHSerializable.fromProto(p.getWfRunId(), WfRunIdModel.class, context);
        if (!p.getBookmark().isEmpty()) {
            try {
                bookmark = BookmarkPb.parseFrom(p.getBookmark());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
        if (p.hasLimit()) {
            limit = p.getLimit();
        }
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.VARIABLE;
    }

    @Override
    public TagStorageType indexTypeForSearch() throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return ObjectIdScanBoundaryStrategy.from(wfRunId);
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }
}
