using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WorkflowCondition
{
    private readonly VariableAssignment _leftHandSite;
    private readonly Comparator _comparator;
    private readonly VariableAssignment _rightHandSite;

    internal WorkflowCondition(VariableAssignment leftHandSite, Comparator comparator, VariableAssignment rightHandSite) 
    {
        _leftHandSite = leftHandSite;
        _comparator = comparator;
        _rightHandSite = rightHandSite;
    }

    internal EdgeCondition GetOpposite()
    {
        var output = new EdgeCondition
        {
            Left = _leftHandSite,
            Right = _rightHandSite
        };
        switch (_comparator) 
        {
            case Comparator.LessThan:
                output.Comparator = Comparator.GreaterThanEq;
                break;
            case Comparator.GreaterThan:
                output.Comparator = Comparator.LessThanEq;
                break;
            case Comparator.LessThanEq:
                output.Comparator = Comparator.GreaterThan;
                break;
            case Comparator.GreaterThanEq:
                output.Comparator = Comparator.LessThan;
                break;
            case Comparator.In:
                output.Comparator = Comparator.NotIn;
                break;
            case Comparator.NotIn:
                output.Comparator = Comparator.In;
                break;
            case Comparator.Equals:
                output.Comparator = Comparator.NotEquals;
                break;
            case Comparator.NotEquals:
                output.Comparator = Comparator.Equals;
                break;
            default:
                throw new Exception("Not possible");
        }
        
        return output;
    }

    internal EdgeCondition Compile()
    {
        return new EdgeCondition
        {
            Left = _leftHandSite,
            Comparator = _comparator,
            Right = _rightHandSite
        };
    }
}