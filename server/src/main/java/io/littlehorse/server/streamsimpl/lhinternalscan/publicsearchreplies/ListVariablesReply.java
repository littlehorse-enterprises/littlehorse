package io.littlehorse.server.streamsimpl.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.sdk.common.proto.ListVariablesReplyPb;
import io.littlehorse.sdk.common.proto.VariablePb;
import io.littlehorse.server.streamsimpl.lhinternalscan.PublicScanReply;

public class ListVariablesReply
    extends PublicScanReply<ListVariablesReplyPb, VariablePb, Variable> {

    public Class<Variable> getResultJavaClass() {
        return Variable.class;
    }

    public Class<VariablePb> getResultProtoClass() {
        return VariablePb.class;
    }

    public Class<ListVariablesReplyPb> getProtoBaseClass() {
        return ListVariablesReplyPb.class;
    }
}
