package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.wfrun.VariableModel;
import io.littlehorse.sdk.common.proto.ListVariablesResponse;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListVariablesReply extends PublicScanReply<ListVariablesResponse, Variable, VariableModel> {

    public Class<VariableModel> getResultJavaClass() {
        return VariableModel.class;
    }

    public Class<Variable> getResultProtoClass() {
        return Variable.class;
    }

    public Class<ListVariablesResponse> getProtoBaseClass() {
        return ListVariablesResponse.class;
    }
}
