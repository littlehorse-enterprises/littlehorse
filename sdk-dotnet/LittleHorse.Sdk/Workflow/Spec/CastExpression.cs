using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

/// <summary>
/// Represents a cast operation on an expression or node output.
/// </summary>
public class CastExpression : LHExpression
{
    /// <summary>
    /// The target LH primitive type for the cast.
    /// </summary>
    public VariableType TargetType { get; private set; }

    public CastExpression(object source, VariableType targetType) : base(source, VariableMutationType.Assign, null)
    {
        TargetType = targetType;
    }
}
