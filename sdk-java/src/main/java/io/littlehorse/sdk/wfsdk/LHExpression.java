package io.littlehorse.sdk.wfsdk;

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
}
