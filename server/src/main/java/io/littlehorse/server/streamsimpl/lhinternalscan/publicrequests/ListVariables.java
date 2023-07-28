package io.littlehorse.server.streamsimpl.lhinternalscan.publicrequests;

import com.google.protobuf.Message;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.proto.GetableClassEnumPb;
import io.littlehorse.common.proto.TagStorageTypePb;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.sdk.common.proto.ListVariablesPb;
import io.littlehorse.sdk.common.proto.ListVariablesReplyPb;
import io.littlehorse.sdk.common.proto.VariablePb;
import io.littlehorse.server.streamsimpl.lhinternalscan.ObjectIdScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanRequest;
import io.littlehorse.server.streamsimpl.lhinternalscan.SearchScanBoundaryStrategy;
import io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies.ListVariablesReply;

public class ListVariables
    extends PublicScanRequest<ListVariablesPb, ListVariablesReplyPb, VariablePb, Variable, ListVariablesReply> {

    public String wfRunId;

    public Class<ListVariablesPb> getProtoBaseClass() {
        return ListVariablesPb.class;
    }

    public ListVariablesPb.Builder toProto() {
        return ListVariablesPb.newBuilder().setWfRunId(wfRunId);
    }

    public void initFrom(Message proto) {
        ListVariablesPb p = (ListVariablesPb) proto;
        wfRunId = p.getWfRunId();
    }

    public GetableClassEnumPb getObjectType() {
        return GetableClassEnumPb.VARIABLE;
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
