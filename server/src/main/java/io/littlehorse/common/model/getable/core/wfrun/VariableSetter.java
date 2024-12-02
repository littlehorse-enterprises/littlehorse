package io.littlehorse.common.model.getable.core.wfrun;

import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;

/**
 * Functional interface used to assign the value of a single variable. Useful for
 * making class responsibilities more explicit and keeping our Grumpy Maintainer
 * somewhat happy.
 */
public interface VariableSetter {

    /**
     * Sets the value of the variable with the provided name.
     * @param variableName The name of the variable whose value we want to set.
     * @param value The value to assign to the variable.
     * @throws LHVarSubError If the variable does not exist or has not been assigned
     * a value.
     */
    void setVariable(String variableName, VariableValueModel value) throws LHVarSubError;
}
