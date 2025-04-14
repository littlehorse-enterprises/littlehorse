package io.littlehorse.sdk.wfsdk;

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
}
