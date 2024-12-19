package io.littlehorse.sdk.wfsdk;

import java.io.Serializable;

/**
 * `NodeOutput` represents the output of a Node execution. It can be used to set a timeout on a
 * node, or as input for a Variable Mutation.
 */
public interface NodeOutput extends Serializable {
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
     * Adds a timeout to a Node. Valid on TaskRuns and ExternalEvents.
     *
     * @param timeoutSeconds the timeout length.
     * @return the NodeOutput.
     */
    public NodeOutput timeout(int timeoutSeconds);

        /**
     * Returns an expression whose value is the `other` added to this node's output.
     * @param other the value to be added to this node's output.
     * @return an expression whose value is the `other` added to this node's output.
     */
    LHExpression add(Serializable other);

    /**
     * Returns an expression whose value is the `other` subtracted from this node's output.
     * @param other the value to be subtracted from this node's output.
     * @return an expression whose value is the `other` subtracted from this node's output.
     */
    LHExpression subtract(Serializable other);

    /**
     * Returns an expression whose value is the `other` multiplied by this node's output.
     * @param other the value to be multiplied by this node's output.
     * @return an expression whose value is the `other` multiplied by this node's output.
     */
    LHExpression multiply(Serializable other);

    /**
     * Returns an expression whose value is this node's output divided by the `other`.
     * @param other the value to divide this node's output by.
     * @return an expression whose value is this node's output divided by the `other`.
     */
    LHExpression divide(Serializable other);

    /**
     * Returns an expression whose value is this node's output extended by the `other`.
     * @param other the value to extend this node's output by.
     * @return an expression whose value is this node's output extended by the `other`.
     */
    LHExpression extend(Serializable other);

    /**
     * Returns an expression whose value is this node's output with all occurrences of
     * `other` removed.
     * @param other the value to remove from this node's output.
     * @return an expression whose value is this node's output with all occurrences of
     * `other` removed.
     */
    LHExpression removeIfPresent(Serializable other);

    /**
     * Returns an expression whose value is this node's output with the index specified
     * by `other` removed.
     *
     * Valid only for JSON_ARR expressions.
     * @param index the index at which to insert the `other`.
     * @return an expression whose value is this node's output with the `other` inserted
     * at the specified `index`.
     */
    LHExpression removeIndex(int index);

    /**
     * Returns an expression whose value is this node's output with the index specified
     * by `index` removed.
     *
     * Valid only for JSON_ARR expressions.
     * @param index the index at which to remove the value.
     * @return an expression whose value is this node's output with the value at the
     * specified `index` removed.
     */
    LHExpression removeIndex(LHExpression index);

    /**
     * Returns an expression whose value is this node's output with the key specified
     * by `key` removed.
     *
     * Valid only for JSON_OBJ expressions.
     * @param key the key to remove from this node's output.
     * @return an expression whose value is this node's output with the key specified
     * by `key` removed.
     */
    LHExpression removeKey(Serializable key);
}
