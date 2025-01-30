using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WorkflowCondition
{
    private readonly object _leftHandSite;
    private readonly Comparator _comparator;
    private readonly object _rightHandSite;

    public WorkflowCondition(object leftHandSite, Comparator comparator, object rightHandSite) 
    {
        _leftHandSite = leftHandSite;
        _comparator = comparator;
        _rightHandSite = rightHandSite;
    }

    public EdgeCondition GetOpposite()
    {
        var output = new EdgeCondition
        {
            Left = LHVariableAssigmentHelper.AssignVariable(_leftHandSite),
            Right = LHVariableAssigmentHelper.AssignVariable(_rightHandSite)
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

    /// <summary>
    /// Compiles the EdgeCondition into Proto objects
    /// </summary>
    /// <returns>The value of <paramref name="EdgeCondition" /> </returns>
    public EdgeCondition Compile()
    {
        return new EdgeCondition
        {
            Left = LHVariableAssigmentHelper.AssignVariable(_leftHandSite),
            Comparator = _comparator,
            Right = LHVariableAssigmentHelper.AssignVariable(_rightHandSite)
        };
    }
}