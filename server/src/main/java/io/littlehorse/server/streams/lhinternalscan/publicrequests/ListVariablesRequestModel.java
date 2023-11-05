package io.littlehorse.server.streams.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.LHStore;
import io.littlehorse.common.dao.ReadOnlyMetadataProcessorDAO;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.variable.VariableModel;
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

public class ListVariablesRequestModel
        extends PublicScanRequest<ListVariablesRequest, VariableList, Variable, VariableModel, ListVariablesReply> {

    public String wfRunId;

    public Class<ListVariablesRequest> getProtoBaseClass() {
        return ListVariablesRequest.class;
    }

    public ListVariablesRequest.Builder toProto() {
        return ListVariablesRequest.newBuilder().setWfRunId(wfRunId);
    }

    public void initFrom(Message proto) {
        ListVariablesRequest p = (ListVariablesRequest) proto;
        wfRunId = p.getWfRunId();
    }

    public GetableClassEnum getObjectType() {
        return GetableClassEnum.VARIABLE;
    }

    @Override
    public TagStorageType indexTypeForSearch(ReadOnlyMetadataProcessorDAO readOnlyDao) throws LHApiException {
        return TagStorageType.LOCAL;
    }

    @Override
    public ScanResultTypePb getResultType() {
        return ScanResultTypePb.OBJECT;
    }

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new ObjectIdScanBoundaryStrategy(wfRunId);
    }

    @Override
    public LHStore getStoreType() {
        return LHStore.CORE;
    }
}
