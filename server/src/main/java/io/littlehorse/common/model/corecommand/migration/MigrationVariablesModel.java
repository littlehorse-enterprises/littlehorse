package io.littlehorse.common.model.corecommand.migration;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MigrationVariables;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class MigrationVariablesModel extends LHSerializable<MigrationVariables>{
    private Map<String, VariableValueModel> varValues;

    public MigrationVariablesModel() {
        varValues = new HashMap<>();
    }
    @Override
    public MigrationVariables.Builder toProto() {
        MigrationVariables.Builder out = MigrationVariables.newBuilder();
        for (Map.Entry<String, VariableValueModel> e : varValues.entrySet()) {
            out.putVarValues(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        MigrationVariables p = (MigrationVariables) proto;
        for (Map.Entry<String, VariableValue> e : p.getVarValuesMap().entrySet()) {
            varValues.put(e.getKey(), VariableValueModel.fromProto(e.getValue(), context));
        }
    }

    public static MigrationVariablesModel fromProto(MigrationVariables p, ExecutionContext context) {
        MigrationVariablesModel out = new MigrationVariablesModel();
        out.initFrom(p, context);
        return out;
    }

    @Override
    public Class<MigrationVariables> getProtoBaseClass() {
        return MigrationVariables.class;
    }
   
}
