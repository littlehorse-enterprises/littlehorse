using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
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

        var result = workflow.Compile();

        var actualResult = LHMappingHelper.ProtoToJson(result);
        var expectedResult = "{ \"name\": \"example-basic\", \"threadSpecs\": { \"entrypoint\": " +
                             "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": " +
                             "[ { \"sinkNodeName\": \"1-exit-Exit\", \"variableMutations\": [ ] } ], " +
                             "\"failureHandlers\": [ ], \"entrypoint\": { } }, \"1-exit-Exit\": { \"outgoingEdges\": " +
                             "[ ], \"failureHandlers\": [ ], \"exit\": { } } }, \"variableDefs\": [ { \"varDef\": " +
                             "{ \"type\": \"STR\", \"name\": \"input-name\", \"maskedValue\": false }, \"required\": " +
                             "false, \"searchable\": false, \"jsonIndexes\": [ ], \"accessLevel\": " +
                             "\"PRIVATE_VAR\" } ], \"interruptDefs\": [ ] } }, \"entrypointThreadName\": " +
                             "\"entrypoint\", \"allowedUpdates\": \"ALL_UPDATES\" }";
        var expectedQuantityOfNodes = 2;
        Assert.Equal(expectedQuantityOfNodes, result.ThreadSpecs["entrypoint"].Nodes.Count);
        Assert.Equal(expectedResult, actualResult);
    }
}