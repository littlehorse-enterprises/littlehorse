package io.littlehorse.server.streams.lhinternalscan.publicsearchreplies;

import io.littlehorse.common.model.getable.core.variable.VariableModel;
import io.littlehorse.sdk.common.proto.Variable;
import io.littlehorse.sdk.common.proto.VariableList;
import io.littlehorse.server.streams.lhinternalscan.PublicScanReply;

public class ListVariablesReply extends PublicScanReply<VariableList, Variable, VariableModel> {

    public Class<VariableModel> getResultJavaClass() {
        return VariableModel.class;
    }

    public Class<Variable> getResultProtoClass() {
        return Variable.class;
    }

    public Class<VariableList> getProtoBaseClass() {
        return VariableList.class;
    }
}
