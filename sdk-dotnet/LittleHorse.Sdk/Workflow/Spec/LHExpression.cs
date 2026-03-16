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
    public VariableMutationType? Operation { get; private set; }

    /// <value>The comparator for boolean condition expressions</value>
    public Comparator? Comparison { get; private set; }
    
    /// <value>The right-hand side object</value>
    public object? Rhs { get; private set; }

    
    /// <summary>
    /// Initializes a new instance of the <see cref="LHExpression"/> class.
    /// </summary>
    /// <param name="lhs">The specified left-hand side object.</param>
    /// <param name="operation">The operation to execute.</param>
    /// <param name="rhs">The specified right-hand side object.</param>
    public LHExpression(object lhs, VariableMutationType operation, object? rhs)
    {
        Lhs = lhs;
        Operation = operation;
        Comparison = null;
        Rhs = rhs;
    }

    /// <summary>
    /// Initializes a new instance of the <see cref="LHExpression"/> class for a comparator expression.
    /// </summary>
    /// <param name="lhs">The specified left-hand side object.</param>
    /// <param name="comparator">The comparator to execute.</param>
    /// <param name="rhs">The specified right-hand side object.</param>
    public LHExpression(object lhs, Comparator comparator, object? rhs)
    {
        Lhs = lhs;
        Comparison = comparator;
        Operation = null;
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
    /// <returns>An expression whose value is this expression with the <c>other</c> removed
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

    /// <summary>
    /// Creates a less-than comparison expression.
    /// </summary>
    public LHExpression IsLessThan(object other)
    {
        return new LHExpression(this, Comparator.LessThan, other);
    }

    /// <summary>
    /// Creates a less-than-or-equal comparison expression.
    /// </summary>
    public LHExpression IsLessThanEq(object other)
    {
        return new LHExpression(this, Comparator.LessThanEq, other);
    }

    /// <summary>
    /// Creates a greater-than comparison expression.
    /// </summary>
    public LHExpression IsGreaterThan(object other)
    {
        return new LHExpression(this, Comparator.GreaterThan, other);
    }

    /// <summary>
    /// Creates a greater-than-or-equal comparison expression.
    /// </summary>
    public LHExpression IsGreaterThanEq(object other)
    {
        return new LHExpression(this, Comparator.GreaterThanEq, other);
    }

    /// <summary>
    /// Creates an equality comparison expression.
    /// </summary>
    public LHExpression IsEqualTo(object other)
    {
        return new LHExpression(this, Comparator.Equals, other);
    }

    /// <summary>
    /// Creates an inequality comparison expression.
    /// </summary>
    public LHExpression IsNotEqualTo(object other)
    {
        return new LHExpression(this, Comparator.NotEquals, other);
    }

    /// <summary>
    /// Creates a contains comparison where this expression is the container.
    /// </summary>
    public LHExpression DoesContain(object other)
    {
        return new LHExpression(other, Comparator.In, this);
    }

    /// <summary>
    /// Creates a not-contains comparison where this expression is the container.
    /// </summary>
    public LHExpression DoesNotContain(object other)
    {
        return new LHExpression(other, Comparator.NotIn, this);
    }

    /// <summary>
    /// Creates an in comparison where this expression is the element.
    /// </summary>
    public LHExpression IsIn(object other)
    {
        return new LHExpression(this, Comparator.In, other);
    }

    /// <summary>
    /// Creates a not-in comparison where this expression is the element.
    /// </summary>
    public LHExpression IsNotIn(object other)
    {
        return new LHExpression(this, Comparator.NotIn, other);
    }

    /// <summary>
    /// Creates a logical and expression.
    /// </summary>
    public LHExpression And(object other)
    {
        return new LHExpression(this, VariableMutationType.And, other);
    }

    /// <summary>
    /// Creates a logical or expression.
    /// </summary>
    public LHExpression Or(object other)
    {
        return new LHExpression(this, VariableMutationType.Or, other);
    }

    internal LHExpression GetReverse()
    {
        if (!Comparison.HasValue)
        {
            throw new InvalidOperationException("Cannot reverse non-comparator expression.");
        }

        return new LHExpression(Lhs, ReverseComparator(Comparison.Value), Rhs);
    }

    private static Comparator ReverseComparator(Comparator comparator)
    {
        return comparator switch
        {
            Comparator.LessThan => Comparator.GreaterThanEq,
            Comparator.GreaterThan => Comparator.LessThanEq,
            Comparator.LessThanEq => Comparator.GreaterThan,
            Comparator.GreaterThanEq => Comparator.LessThan,
            Comparator.In => Comparator.NotIn,
            Comparator.NotIn => Comparator.In,
            Comparator.Equals => Comparator.NotEquals,
            Comparator.NotEquals => Comparator.Equals,
            _ => throw new InvalidOperationException($"Unexpected comparator: {comparator}")
        };
    }

    /// <summary>
    /// Returns a CastExpression representing this expression cast to the specified target type.
    /// </summary>
    /// <param name="targetType">The target VariableType to cast to.</param>
    /// <returns>A CastExpression wrapping this expression.</returns>
    public CastExpression CastTo(VariableType targetType)
    {
        return new CastExpression(this, targetType);
    }

    /// <summary>
    /// Returns a CastExpression that casts this expression to Int.
    /// </summary>
    /// <returns>A CastExpression for Int.</returns>
    public CastExpression CastToInt() => CastTo(VariableType.Int);

    /// <summary>
    /// Returns a CastExpression that casts this expression to Double.
    /// </summary>
    /// <returns>A CastExpression for Double.</returns>
    public CastExpression CastToDouble() => CastTo(VariableType.Double);

    /// <summary>
    /// Returns a CastExpression that casts this expression to Str.
    /// </summary>
    /// <returns>A CastExpression for Str.</returns>
    public CastExpression CastToStr() => CastTo(VariableType.Str);

    /// <summary>
    /// Returns a CastExpression that casts this expression to Bool.
    /// </summary>
    /// <returns>A CastExpression for Bool.</returns>
    public CastExpression CastToBool() => CastTo(VariableType.Bool);

    /// <summary>
    /// Returns a CastExpression that casts this expression to Bytes.
    /// </summary>
    /// <returns>A CastExpression for Bytes.</returns>
    public CastExpression CastToBytes() => CastTo(VariableType.Bytes);

    /// <summary>
    /// Returns a CastExpression that casts this expression to WfRunId.
    /// </summary>
    /// <returns>A CastExpression for WfRunId.</returns>
    public CastExpression CastToWfRunId() => CastTo(VariableType.WfRunId);
}