package io.littlehorse.common.model.getable.core.wfrun;

import io.littlehorse.common.exceptions.LHVarSubError;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;

/**
 * Functional interface used to assign the value of a single variable. Useful for
 * making class responsibilities more explicit and keeping our Grumpy Maintainer
 * somewhat happy.
 */
public interface VariableFetcher {

    /**
     * Fetches the value of the variable with the provided name.
     * @param variableName The name of the variable whose value we want to fetch.
     * @return The value of the variable.
     * @throws LHVarSubError If the variable does not exist or has not been assigned
     * a value.
     */
    VariableValueModel fetchVariable(String variableName) throws LHVarSubError;

    /**
     * Fetches the output of the most recent instance NodeRun associated with the
     * provided Node name.
     * @param nodeName The name of the node whose output we want to fetch.
     * @return The output of the most recent instance of the node.
     * @throws LHVarSubError If the node does not exist, has not been run, or returned
     * no output.
     */
    VariableValueModel fetchNodeOutput(String nodeName) throws LHVarSubError;
}
