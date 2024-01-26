package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.VariableType;
import java.io.Serializable;

/** A WfRunVariable is a handle on a Variable in a WfSpec. */
public interface WfRunVariable extends Serializable {
    /**
     * Valid only for output of the JSON_OBJ or JSON_ARR types. Returns a new WfRunVariable handle
     * which points to Json element referred to by the json path.
     *
     * <p>Can only be called once--you can't call node.jsonPath().jsonPath().
     *
     * @param path is the json path to evaluate.
     * @return a WfRunVariable.
     */
    WfRunVariable jsonPath(String path);

    /**
     * Marks the variable as "Required", meaning that the ThreadSpec cannot be
     * started without this variable being provided as input. For Entrypoint
     * ThreadSpec's, this also triggers the WfSpec Required Variable Compatibiltiy
     * Rules.
     * @return
     */
    WfRunVariable required();

    /**
     * Marks the Variable as "Searchable", which:
     * - Creates an Index on the Variable in the LH Data Store
     * - Due to the fact that the Variable is now Searchable, all future WfSpec
     *   versions must use the same Type for this Variable.
     * @return same {@link WfRunVariable} instance
     */
    WfRunVariable searchable();

    /**
     * Marks the JSON_OBJ or JSON_ARR Variable as "Searchable", and creates an
     * index on the specified field.
     * @param fieldPath is the JSON Path to the field that we are indexing.
     * @param fieldType is the type of the field we are indexing.
     * @return same {@link WfRunVariable} instance
     */
    WfRunVariable searchableOn(String fieldPath, VariableType fieldType);
}
