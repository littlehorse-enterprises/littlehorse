using LittleHorse.Sdk.Common.Proto;

namespace LittleHorse.Sdk.Workflow.Spec;

public class WorkflowCondition
{
    private readonly EdgeCondition _spec;

    public WorkflowCondition(EdgeCondition spec) 
    {
        _spec = spec;
    }

    public EdgeCondition GetSpec() 
    {
        return _spec;
    }

    public EdgeCondition GetOpposite()
    {
        var output = new EdgeCondition
        {
            Right = _spec.Right,
            Left = _spec.Left
        };
        switch (_spec.Comparator) 
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
}