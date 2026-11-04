package io.littlehorse.common.model.getable.global.migrations;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.MigrationVars;
import io.littlehorse.sdk.common.proto.VariableAssignment;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MigrationVarsModel extends LHSerializable<MigrationVars> {

    private Map<String, VariableAssignmentModel> varAssignmentByVarName;

    public MigrationVarsModel() {
        varAssignmentByVarName = new HashMap<>();
    }

    @Override
    public MigrationVars.Builder toProto() {
        MigrationVars.Builder out = MigrationVars.newBuilder();

        for (Map.Entry<String, VariableAssignmentModel> entry : varAssignmentByVarName.entrySet()) {
            out.putVarAssignmentByVarName(
                    entry.getKey(), entry.getValue().toProto().build());
        }

        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) throws LHSerdeException {
        MigrationVars p = (MigrationVars) proto;
        varAssignmentByVarName = new HashMap<>();

        for (Map.Entry<String, VariableAssignment> entry :
                p.getVarAssignmentByVarNameMap().entrySet()) {
            varAssignmentByVarName.put(
                    entry.getKey(), LHSerializable.fromProto(entry.getValue(), VariableAssignmentModel.class, context));
        }
    }

    @Override
    public Class<MigrationVars> getProtoBaseClass() {
        return MigrationVars.class;
    }
}
