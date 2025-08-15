package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.VariableType;

/**
 * `NodeOutput` represents the output of a Node execution. It can be used to set a timeout on a
 * node, or as input for a Variable Mutation.
 */
public interface NodeOutput extends LHExpression {
    /**
     * Valid only for output of the JSON_OBJ or JSON_ARR types. Returns a new NodeOutput handle
     * which points to Json element referred to by the json path.
     *
     * <p>This method is most often used to create the `rhs` parameter for
     * `ThreadBuilder::mutate()`.
     *
     * <p>Can only be called once--you can't call node.jsonPath().jsonPath().
     *
     * @param path is the json path to evaluate.
     * @return a NodeOutput.
     */
    public NodeOutput jsonPath(String path);

    /**
     * Returns a new LHExpression that represents this node output cast to the specified type.
     * This enables manual type conversions that are not automatic, such as:
     * - STR → INT/DOUBLE/BOOL (manual casting required)
     * - DOUBLE → INT (manual casting required)
     *
     * The cast operation is non-mutating: the original node output remains unchanged.
     *
     * @param targetType the type to cast this node output to
     * @return a new LHExpression representing the cast value
     */
    LHExpression cast(VariableType targetType);

    /**
     * Equivalent to cast(VariableType.INT).
     *
     * @return a new LHExpression representing the value cast to INT
     */
    default LHExpression castToInt() {
        return cast(VariableType.INT);
    }

    /**
     * Equivalent to cast(VariableType.DOUBLE).
     *
     * @return a new LHExpression representing the value cast to DOUBLE
     */
    default LHExpression castToDouble() {
        return cast(VariableType.DOUBLE);
    }

    /**
     * Equivalent to cast(VariableType.STR).
     *
     * @return a new LHExpression representing the value cast to STR
     */
    default LHExpression castToStr() {
        return cast(VariableType.STR);
    }

    /**
     * Equivalent to cast(VariableType.BOOL).
     *
     * @return a new LHExpression representing the value cast to BOOL
     */
    default LHExpression castToBool() {
        return cast(VariableType.BOOL);
    }

    /**
     * Equivalent to cast(VariableType.BYTES).
     *
     * @return a new LHExpression representing the value cast to BYTES
     */
    default LHExpression castToBytes() {
        return cast(VariableType.BYTES);
    }

    /**
     * Equivalent to cast(VariableType.WF_RUN_ID).
     *
     * @return a new LHExpression representing the value cast to WF_RUN_ID
     */
    default LHExpression castToWfRunId() {
        return cast(VariableType.WF_RUN_ID);
    }
}
