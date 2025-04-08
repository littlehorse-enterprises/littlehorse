using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents an expression which contains Left-hand side, right-hand side values and an operation in a workflow.
/// </summary>
public class LHExpression
{
    /// <value>The left-hand side object</value>
    public object Lhs { get; private set; }
    
    /// <value>The operation to execute</value>
    public VariableMutationType Operation { get; private set; }
    
    /// <value>The right-hand side object</value>
    public object Rhs { get; private set; }

    
    /// <summary>
    /// Initializes a new instance of the <see cref="LHExpression"/> class.
    /// </summary>
    /// <param name="lhs">The specified left-hand side object.</param>
    /// <param name="operation">The operation to execute.</param>
    /// <param name="rhs">The specified right-hand side object.</param>
    public LHExpression(object lhs, VariableMutationType operation, object rhs)
    {
        Lhs = lhs;
        Operation = operation;
        Rhs = rhs;
    }
    
    /// <summary>
    /// Returns an expression whose value is the <c>other</c> added to this expression.
    /// </summary>
    /// <param name="other">The value to be added to this expression.</param>
    /// <returns>An expression whose value is the <c>other</c> added to this expression.</returns>
    public LHExpression Add(object other) 
    {
        return new LHExpression(this, VariableMutationType.Add, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is the <c>other</c> added to this expression.
    /// </summary>
    /// <param name="other">The value to be added to this expression.</param>
    /// <returns>An expression whose value is the <c>other</c> added to this expression.</returns>
    public LHExpression Subtract(object other) 
    {
        return new LHExpression(this, VariableMutationType.Subtract, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is the <c>other</c> multiplied by this expression.
    /// </summary>
    /// <param name="other">The value to be multiplied by this expression.</param>
    /// <returns>An expression whose value is the <c>other</c> multiplied by this expression.</returns>
    public LHExpression Multiply(object other) 
    {
        return new LHExpression(this, VariableMutationType.Multiply, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression divided by the <c>other</c>.
    /// </summary>
    /// <param name="other">The value to divide this expression by.</param>
    /// <returns>An expression whose value is this expression divided by the <c>other</c>.</returns>
    public LHExpression Divide(object other) 
    {
        return new LHExpression(this, VariableMutationType.Divide, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression extended by the <c>other</c>.
    /// </summary>
    /// <param name="other">The value to extend this expression by.</param>
    /// <returns>An expression whose value is this expression extended by the <c>other</c>.</returns>
    public LHExpression Extend(object other) 
    {
        return new LHExpression(this, VariableMutationType.Extend, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression with all occurrences of <c>other</c> removed.
    /// </summary>
    /// <param name="other">The value to remove from this expression.</param>
    /// <returns>An expression whose value is this expression with all occurrences of
    /// <c>other</c> removed.</returns>
    public LHExpression RemoveIfPresent(object other) 
    {
        return new LHExpression(this, VariableMutationType.RemoveIfPresent, other);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression with the index specified by <c>other</c> removed.
    /// 
    /// Valid only for JSON_ARR expressions.
    /// </summary>
    /// <param name="index">The index at which to remove the <c>other</c>.</param>
    /// <returns>An expression whose value is this expression with the `other` removed
    /// at the specified <c>index</c>.</returns>
    public LHExpression RemoveIndex(int index) 
    {
        return new LHExpression(this, VariableMutationType.RemoveIndex, index);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression with the index specified by <c>other</c> removed.
    /// 
    /// Valid only for JSON_ARR expressions.
    /// </summary>
    /// <param name="index">The index at which to remove the value.</param>
    /// <returns>An expression whose value is this expression with the value at the
    /// specified <c>index</c> removed.</returns>
    public LHExpression RemoveIndex(LHExpression index) 
    {
        return new LHExpression(this, VariableMutationType.RemoveIndex, index);
    }
    
    /// <summary>
    /// Returns an expression whose value is this expression with the key specified
    /// by <c>key</c> removed.
    /// 
    /// Valid only for JSON_OBJ expressions.
    /// </summary>
    /// <param name="key">The key to remove from this expression.</param>
    /// <returns>An expression whose value is this expression with the value at the specified
    /// <c>key</c> removed.</returns>
    public LHExpression RemoveKey(object key) 
    {
        return new LHExpression(this, VariableMutationType.RemoveKey, key);
    }
}