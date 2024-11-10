package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;
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
     * Marks the Variable as "Searchable", which creates an Index on the Variable
     * in the LH Data Store.
     * @return same {@link WfRunVariable} instance
     */
    WfRunVariable searchable();

    /**
     * Marks the Variable as a `PUBLIC_VAR`, which does three things:
     * 1. Considers this variable in determining whether a new version of this WfSpec
     *    should be a major version or minor revision.
     * 2. Freezes the type of this variable so that you cannot create future WfSpec
     *    versions with a variable of the same name and different type.
     * 3. Allows defining child WfSpec's that use this variable.
     *
     * This is an advanced feature that you should use in any of the following cases:
     * - You are treating a WfSpec as a data model and a WfRun as an instance of data.
     * - You need child workflows to access this variable.
     * @return same {@link WfRunVariable} instance
     */
    WfRunVariable asPublic();

    /**
     * Marks the Variable as a `INHERITED_VAR`, which means that it comes from the
     * parent `WfRun`. This means that:
     * - There must be a parent WfSpec reference.
     * - The parent must have a PUBLIC_VAR variable of the same name and type.
     * @return same {@link WfRunVariable} instance
     */
    WfRunVariable asInherited();

    /**
     * Marks the JSON_OBJ or JSON_ARR Variable as "Searchable", and creates an
     * index on the specified field.
     * @param fieldPath is the JSON Path to the field that we are indexing.
     * @param fieldType is the type of the field we are indexing.
     * @return same {@link WfRunVariable} instance
     */
    WfRunVariable searchableOn(String fieldPath, VariableType fieldType);

    /**
     * Sets the access level of a WfRunVariable.
     * @param accessLevel is the access level to set.
     * @return this WfRunVariable.
     */
    WfRunVariable withAccessLevel(WfRunVariableAccessLevel accessLevel);

    /**
     * Marks a WfRunVariable to show masked values
     *
     * @return this WfRunVariable.
     */
    WfRunVariable masked();
}
