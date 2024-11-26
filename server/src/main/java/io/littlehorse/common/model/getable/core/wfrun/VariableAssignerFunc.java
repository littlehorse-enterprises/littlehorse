package io.littlehorse.common.model.getable.core.wfrun;

import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.global.wfspec.variable.VariableAssignmentModel;

public interface VariableAssignerFunc {
    VariableValueModel assign(VariableAssignmentModel varAssn) throws LHVarSubError;
}
