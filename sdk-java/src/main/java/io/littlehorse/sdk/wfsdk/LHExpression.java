package io.littlehorse.sdk.wfsdk;

import io.littlehorse.sdk.common.proto.Comparator;
import io.littlehorse.sdk.common.proto.VariableMutationType;
import io.littlehorse.sdk.common.proto.VariableType;
import io.littlehorse.sdk.wfsdk.internal.CastExpressionImpl;
import io.littlehorse.sdk.wfsdk.internal.LHExpressionImpl;
import java.io.Serializable;

public interface LHExpression extends Serializable {

    /**
     * Returns an expression whose value is the `other` added to this expression.
     * @param other the value to be added to this expression.
     * @return an expression whose value is the `other` added to this expression.
     */
    default LHExpression add(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.ADD, other);
    }

    /**
     * Returns an expression whose value is the `other` subtracted from this expression.
     * @param other the value to be subtracted from this expression.
     * @return an expression whose value is the `other` subtracted from this expression.
     */
    default LHExpression subtract(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.SUBTRACT, other);
    }

    /**
     * Returns an expression whose value is the `other` multiplied by this expression.
     * @param other the value to be multiplied by this expression.
     * @return an expression whose value is the `other` multiplied by this expression.
     */
    default LHExpression multiply(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.MULTIPLY, other);
    }

    /**
     * Returns an expression whose value is this expression divided by the `other`.
     * @param other the value to divide this expression by.
     * @return an expression whose value is this expression divided by the `other`.
     */
    default LHExpression divide(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.DIVIDE, other);
    }

    /**
     * Returns an expression whose value is this expression extended by the `other`.
     * @param other the value to extend this expression by.
     * @return an expression whose value is this expression extended by the `other`.
     */
    default LHExpression extend(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.EXTEND, other);
    }

    /**
     * Returns an expression whose value is this expression with all occurrences of
     * `other` removed.
     * @param other the value to remove from this expression.
     * @return an expression whose value is this expression with all occurrences of
     * `other` removed.
     */
    default LHExpression removeIfPresent(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_IF_PRESENT, other);
    }

    /**
     * Returns an expression whose value is this expression with the index specified
     * by `other` removed.
     *
     * Valid only for JSON_ARR expressions.
     * @param index the index at which to remove the value.
     * @return an expression whose value is this expression with the value at the
     * specified `index` removed.
     */
    default LHExpression removeIndex(int index) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_INDEX, index);
    }

    /**
     * Returns an expression whose value is this expression with the index specified
     * by `index` removed.
     *
     * Valid only for JSON_ARR expressions.
     * @param index the index at which to remove the value.
     * @return an expression whose value is this expression with the value at the
     * specified `index` removed.
     */
    default LHExpression removeIndex(LHExpression index) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_INDEX, index);
    }

    /**
     * Returns an expression whose value is this expression with the key specified
     * by `key` removed.
     *
     * Valid only for JSON_OBJ expressions.
     * @param key the key to remove from this expression.
     * @return an expression whose value is this expression with the value at the specified
     * `key` removed.
     */
    default LHExpression removeKey(Serializable key) {
        return new LHExpressionImpl(this, VariableMutationType.REMOVE_KEY, key);
    }

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
    default LHExpression castTo(VariableType targetType) {
        return new CastExpressionImpl(this, targetType);
    }

    default LHExpression isLessThan(Serializable other) {
        return new LHExpressionImpl(this, Comparator.LESS_THAN, other);
    }

    default LHExpression isGreaterThan(Serializable other) {
        return new LHExpressionImpl(this, Comparator.GREATER_THAN, other);
    }

    default LHExpression isEqualTo(Serializable other) {
        return new LHExpressionImpl(this, Comparator.EQUALS, other);
    }

    default LHExpression isNotEqualTo(Serializable other) {
        return new LHExpressionImpl(this, Comparator.NOT_EQUALS, other);
    }

    default LHExpression doesContain(Serializable other) {
        return new LHExpressionImpl(other, Comparator.IN, this);
    }

    default LHExpression doesNotContain(Serializable other) {
        return new LHExpressionImpl(other, Comparator.NOT_IN, this);
    }

    default LHExpression isIn(Serializable other) {
        return new LHExpressionImpl(this, Comparator.IN, other);
    }

    default LHExpression isNotIn(Serializable other) {
        return new LHExpressionImpl(this, Comparator.NOT_IN, other);
    }

    default LHExpression and(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.AND, other);
    }

    default LHExpression or(Serializable other) {
        return new LHExpressionImpl(this, VariableMutationType.OR, other);
    }

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
}
