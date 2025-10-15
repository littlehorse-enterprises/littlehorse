package io.littlehorse.sdk.wfsdk;

import java.io.Serializable;

import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.common.proto.WfRunVariableAccessLevel;

/** A WfRunVariable is a handle on a Variable in a WfSpec. */
public interface WfRunVariable extends LHExpression {
    /**
     * Valid only for output of the JSON_OBJ or JSON_ARR types. Returns a new WfRunVariable handle
     * which points to Json element referred to by the json path.
     *
     * Can only be called once--you can't call var.jsonPath().jsonPath().
     *
     * @param path is the json path to evaluate.
     * @return a WfRunVariable.
     */
    WfRunVariable jsonPath(String path);

    /**
     * Valid only for output of JSON_OBJ or Struct types. Returns a new WfRunVariable handle
     * which points to the sub-element referred to by the field.
     * 
     * Note: You can call this method consecutively to get values from nested objects.
     * 
     * @param field is the name of the field to access.
     * @return a WfRunVariable.
     */
    WfRunVariable get(String field);

    /**
     * Valid only for output of JSON_ARRs. Returns a new WfRunVariable handle
     * which points to the sub-element referred to by the index.
     * 
     * Note: You can call this method consecutively to get values from nested objects.
     * 
     * @param index is the index of the item to access.
     * @return a WfRunVariable.
     */
    WfRunVariable get(int index);

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

    /**
     * Sets the default value for this WfRunVariable.
     * @param defaultVal is the default value for this variable.
     * @return this WfRunVariable.
     */
    WfRunVariable withDefault(Object defaultVal);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is LESS_THAN the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.LESS_THAN, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is LESS_THAN the provided rhs.
     */
    WorkflowCondition isLessThan(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is LESS_THAN_EQU the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.LESS_THAN_EQ, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is LESS_THAN_EQ the provided rhs.
     */
    WorkflowCondition isLessThanEq(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is GREATER_THAN_EQ the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.GREATER_THAN_EQ, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is GREATER_THAN_EQ the provided rhs.
     */
    WorkflowCondition isGreaterThanEq(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is GREATER_THAN the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.GREATER_THAN, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is GREATER_THAN the provided rhs.
     */
    WorkflowCondition isGreaterThan(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is EQUALS to the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.EQUALS, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is EQUALS the provided rhs.
     */
    WorkflowCondition isEqualTo(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is NOT_EQUALS to the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.NOT_EQUALS, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is NOT_EQUALS the provided rhs.
     */
    WorkflowCondition isNotEqualTo(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if the RHS is contained inside this variable. For JSON_OBJ, returns
     * true if the RHS is a key. For JSON_ARR, returns true if the RHS is equal to one of the
     * elements in the array.
     *
     * Equivalent to WorkflowThread#condition(rhs, Comparator.IN, this);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if the provided rhs is INSIDE this WfRunVariable.
     */
    WorkflowCondition doesContain(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if the RHS is not contained inside this variable. For JSON_OBJ, returns
     * true if the RHS is not a key. For JSON_ARR, returns true if the RHS is not equal to any of
     * the elements in the array.
     *
     * Equivalent to WorkflowThread#condition(rhs, Comparator.NOT_IN, this);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is NOT INSIDE the provided rhs.
     */
    WorkflowCondition doesNotContain(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if the value of this WfRunVariable is contained in the provided RHS.
     * For an RHS of type JSON_OBJ, returns true if the RHS contains a key that is equal to the
     * value of this WfRunVariable. For an RHS of type JSON_ARR, returns true if the RHS contains
     * an element that is equal to the value of this WfRunVariable.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.IN, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is INSIDE the provided rhs.
     */
    WorkflowCondition isIn(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if the value of this WfRunVariable is not contained in the provided RHS.
     * For an RHS of type JSON_OBJ, returns true if the RHS does not contain a key that is equal
     * to the value of this WfRunVariable. For an RHS of type JSON_ARR, returns true if the RHS does
     * not contain an element that is equal to the value of this WfRunVariable.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.NOT_IN, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is NOT INSIDE the provided rhs.
     */
    WorkflowCondition isNotIn(Serializable rhs);

    /**
     * Mutates the value of this WfRunVariable and sets it to the the value provided on the RHS.
     *
     * If the LHS of this WfRunVariable is set, then the sub-element of this WfRunVariable
     * provided by the Json Path is mutated.
     * @param rhs is the value to set this WfRunVariable to.
     */
    void assign(Serializable rhs);

    // cast methods are provided by LHExpression
}
