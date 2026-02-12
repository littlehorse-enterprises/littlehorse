package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.VariableType;
import java.io.Serializable;

public interface LHExpression extends Serializable {

    /**
     * Returns an expression whose value is the `other` added to this expression.
     * @param other the value to be added to this expression.
     * @return an expression whose value is the `other` added to this expression.
     */
    LHExpression add(Serializable other);

    /**
     * Returns an expression whose value is the `other` subtracted from this expression.
     * @param other the value to be subtracted from this expression.
     * @return an expression whose value is the `other` subtracted from this expression.
     */
    LHExpression subtract(Serializable other);

    /**
     * Returns an expression whose value is the `other` multiplied by this expression.
     * @param other the value to be multiplied by this expression.
     * @return an expression whose value is the `other` multiplied by this expression.
     */
    LHExpression multiply(Serializable other);

    /**
     * Returns an expression whose value is this expression divided by the `other`.
     * @param other the value to divide this expression by.
     * @return an expression whose value is this expression divided by the `other`.
     */
    LHExpression divide(Serializable other);

    /**
     * Returns an expression whose value is this expression extended by the `other`.
     * @param other the value to extend this expression by.
     * @return an expression whose value is this expression extended by the `other`.
     */
    LHExpression extend(Serializable other);

    /**
     * Returns an expression whose value is this expression with all occurrences of
     * `other` removed.
     * @param other the value to remove from this expression.
     * @return an expression whose value is this expression with all occurrences of
     * `other` removed.
     */
    LHExpression removeIfPresent(Serializable other);

    /**
     * Returns an expression whose value is this expression with the index specified
     * by `other` removed.
     *
     * Valid only for JSON_ARR expressions.
     * @param index the index at which to remove the value.
     * @return an expression whose value is this expression with the value at the
     * specified `index` removed.
     */
    LHExpression removeIndex(int index);

    /**
     * Returns an expression whose value is this expression with the index specified
     * by `index` removed.
     *
     * Valid only for JSON_ARR expressions.
     * @param index the index at which to remove the value.
     * @return an expression whose value is this expression with the value at the
     * specified `index` removed.
     */
    LHExpression removeIndex(LHExpression index);

    /**
     * Returns an expression whose value is this expression with the key specified
     * by `key` removed.
     *
     * Valid only for JSON_OBJ expressions.
     * @param key the key to remove from this expression.
     * @return an expression whose value is this expression with the value at the specified
     * `key` removed.
     */
    LHExpression removeKey(Serializable key);

    /**
     * Returns a new LHExpression that represents this expression cast to the specified type.
     * This enables manual type conversions that are not automatic, such as:
     * - STR → INT/DOUBLE/BOOL (manual casting required)
     * - DOUBLE → INT (manual casting required)
     *
     * The cast operation is non-mutating: the original expression remains unchanged.
     *
     * @param targetType the type to cast this expression to
     * @return a new LHExpression representing the cast value
     */
    LHExpression castTo(VariableType targetType);

    /**
     * Equivalent to cast(VariableType.INT).
     *
     * @return a new LHExpression representing the value cast to INT
     */
    default LHExpression castToInt() {
        return castTo(VariableType.INT);
    }

    /**
     * Equivalent to cast(VariableType.DOUBLE).
     *
     * @return a new LHExpression representing the value cast to DOUBLE
     */
    default LHExpression castToDouble() {
        return castTo(VariableType.DOUBLE);
    }

    /**
     * Equivalent to cast(VariableType.STR).
     *
     * @return a new LHExpression representing the value cast to STR
     */
    default LHExpression castToStr() {
        return castTo(VariableType.STR);
    }

    /**
     * Equivalent to cast(VariableType.BOOL).
     *
     * @return a new LHExpression representing the value cast to BOOL
     */
    default LHExpression castToBool() {
        return castTo(VariableType.BOOL);
    }

    /**
     * Equivalent to cast(VariableType.BYTES).
     *
     * @return a new LHExpression representing the value cast to BYTES
     */
    default LHExpression castToBytes() {
        return castTo(VariableType.BYTES);
    }

    /**
     * Equivalent to cast(VariableType.WF_RUN_ID).
     *
     * @return a new LHExpression representing the value cast to WF_RUN_ID
     */
    default LHExpression castToWfRunId() {
        return castTo(VariableType.WF_RUN_ID);
    }

    LHExpression or(LHExpression elseExpr);

    /**
     * Returns an LHExpression (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is LESS_THAN the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.LESS_THAN, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is LESS_THAN the provided rhs.
     */
    LHExpression isLessThan(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is LESS_THAN_EQU the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.LESS_THAN_EQ, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is LESS_THAN_EQ the provided rhs.
     */
    LHExpression isLessThanEq(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is GREATER_THAN_EQ the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.GREATER_THAN_EQ, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is GREATER_THAN_EQ the provided rhs.
     */
    LHExpression isGreaterThanEq(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is GREATER_THAN the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.GREATER_THAN, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is GREATER_THAN the provided rhs.
     */
    LHExpression isGreaterThan(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is EQUALS to the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.EQUALS, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is EQUALS the provided rhs.
     */
    LHExpression isEqualTo(Serializable rhs);

    /**
     * Returns a WorkflowCondition (treated like a boolean in the WfSpec control flow logic) that
     * evaluates to true if this WfRunVariable is NOT_EQUALS to the provided rhs.
     *
     * Equivalent to WorkflowThread#condition(this, Comparator.NOT_EQUALS, rhs);
     *
     * @param rhs is the RHS to compare this WfRunVariable to.
     * @return true if this WfRunVariable is NOT_EQUALS the provided rhs.
     */
    LHExpression isNotEqualTo(Serializable rhs);

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
    LHExpression doesContain(Serializable rhs);

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
    LHExpression doesNotContain(Serializable rhs);

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
    LHExpression isIn(Serializable rhs);

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
    LHExpression isNotIn(Serializable rhs);
}
