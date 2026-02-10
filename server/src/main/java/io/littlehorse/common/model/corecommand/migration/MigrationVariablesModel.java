package io.littlehorse.common.model.corecommand.migration;

import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.Message;

import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MigrationVariables;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.server.streams.topology.core.ExecutionContext;

public class MigrationVariablesModel extends LHSerializable<MigrationVariables>{
    private Map<String, VariableAssignmentModel> varValues;

    public MigrationVariablesModel() {
        varValues = new HashMap<>();
    }
    @Override
    public MigrationVariables.Builder toProto() {
        MigrationVariables.Builder out = MigrationVariables.newBuilder();
        for (Map.Entry<String, VariableAssignmentModel> e : varValues.entrySet()) {
            out.putVarValues(e.getKey(), e.getValue().toProto().build());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        MigrationVariables p = (MigrationVariables) proto;
        for (Map.Entry<String, VariableAssignment> e : p.getVarValuesMap().entrySet()) {
            varValues.put(e.getKey(), LHSerializable.fromProto(e.getValue(), VariableAssignmentModel.class, context));
        }
    }

    public static MigrationVariablesModel fromProto(MigrationVariables p, ExecutionContext context) {
        MigrationVariablesModel out = new MigrationVariablesModel();
        out.initFrom(p, context);
        return out;
    }

    public Map<String, VariableAssignmentModel> getVarValues() {
        return varValues;
    }

    @Override
    public Class<MigrationVariables> getProtoBaseClass() {
        return MigrationVariables.class;
    }
   
}
