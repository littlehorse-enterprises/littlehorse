package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.wfrun.VariableModel;
import io.littlehorse.sdk.common.proto.ListVariablesReplyPb;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListVariablesReply
    extends PublicScanReply<ListVariablesReplyPb, Variable, VariableModel> {

    public Class<VariableModel> getResultJavaClass() {
        return VariableModel.class;
    }

    public Class<Variable> getResultProtoClass() {
        return Variable.class;
    }

    public Class<ListVariablesReplyPb> getProtoBaseClass() {
        return ListVariablesReplyPb.class;
    }
}
