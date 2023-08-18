package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.wfrun.VariableModel;
import io.littlehorse.common.proto.GetableClassEnum;
import io.littlehorse.common.proto.TagStorageType;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.ListVariablesRequest;
import io.littlehorse.sdk.common.proto.ListVariablesResponse;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListVariablesReply;

public class ListVariablesRequestModel
        extends PublicScanRequest<
                ListVariablesRequest,
                ListVariablesResponse,
                Variable,
                VariableModel,
                ListVariablesReply> {

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
    public TagStorageType indexTypeForSearch(LHGlobalMetaStores stores) throws LHValidationError {
        return TagStorageType.LOCAL;
    }

    @Override
    public void validate() throws LHValidationError {}

    @Override
    public SearchScanBoundaryStrategy getScanBoundary(String searchAttributeString) {
        return new ObjectIdScanBoundaryStrategy(wfRunId);
    }
}
