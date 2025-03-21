using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowTest
{
    public WorkflowTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
    }
    
    [Fact]
    public void Workflow_WithEntrypointFunction_ShouldCompileWorkflowWithThreadDetails()
    {
        var wfName = "example-basic";
        void Entrypoint(WorkflowThread wf)
        {
            wf.AddVariable("input-name", VariableType.Str);
        }
        var workflow = new Sdk.Workflow.Spec.Workflow(wfName, Entrypoint);

        var actualResult = workflow.Compile();

        var wfSpecRequest = new PutWfSpecRequest
        {
            Name = wfName,
            EntrypointThreadName = "entrypoint"
        };
        var threadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "1-exit-EXIT"
                }
            }
        };

        var exitNode = new Node
        {
            Exit = new ExitNode()
        };

        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Type = VariableType.Str,
                Name = "input-name"
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        threadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        threadSpec.Nodes.Add("1-exit-EXIT", exitNode);
        threadSpec.VariableDefs.Add(threadVarDef);
        
        wfSpecRequest.ThreadSpecs.Add("entrypoint", threadSpec);
        
        var expectedQuantityOfNodes = 2;
        Assert.Equal(expectedQuantityOfNodes, actualResult.ThreadSpecs["entrypoint"].Nodes.Count);
        Assert.Equal(wfSpecRequest, actualResult);
    }
}