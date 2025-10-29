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

    /**
     * Valid only for output of JSON_OBJ or Struct types. Returns a new NodeOutput handle
     * which points to the sub-element referred to by the field.
     *
     * @param field is the name of the field to access.
     * @return a NodeOutput.
     */
    public NodeOutput get(String field);

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
    // cast methods are provided by LHExpression
}
