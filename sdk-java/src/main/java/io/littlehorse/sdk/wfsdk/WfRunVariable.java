package io.littlehorse.sdk.wfsdk;

/**
 * A WfRunVariable is a handle on a Variable in a WfSpec.
 */
public interface WfRunVariable {
    /**
     * Valid only for output of the JSON_OBJ or JSON_ARR types. Returns
     * a new NodeOutput handle which points to Json element referred to
     * by the json path.
     *
     * Can only be called once--you can't call node.jsonPath().jsonPath().
     * @param path is the json path to evaluate.
     * @return a NodeOutput.
     */
    public WfRunVariable jsonPath(String path);
}
