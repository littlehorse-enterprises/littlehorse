using System;
using System.Collections.Generic;
using LittleHorse.Sdk;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

public class WorkflowThreadRunWfTest
{
    private readonly Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WorkflowThreadRunWfTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }

    [Fact]
    public void RunWf_WithoutParams_ShouldCompileSuccessfully()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            var parentVar = wf.DeclareInt("parent-var");
            wf.RunWf("child-wf", new Dictionary<string, object>{});
            
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        var compiledWfThread = workflowThread.Compile();

        var actualRunChildWfNode = compiledWfThread.Nodes["1-run-child-wf-RUN_CHILD_WF"].RunChildWf;
        var expectedRunChildWfNode = new RunChildWfNode{
          WfSpecName = "child-wf",
          MajorVersion = -1,
        };

        Assert.Equal(expectedRunChildWfNode, actualRunChildWfNode);
    }

    [Fact]
    public void RunWf_WithParams_ShouldCompileSuccessfully()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            var parentVar = wf.DeclareInt("parent-var");
            var inputVar = wf.DeclareStr("input-var");
            wf.RunWf("child-wf", new Dictionary<string, object>
            {
              {
                "wf-input-1", inputVar
              },
              {
                "wf-input-2", 1000
              }
            });
            
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        var compiledWfThread = workflowThread.Compile();

        var actualRunChildWfNode = compiledWfThread.Nodes["1-run-child-wf-RUN_CHILD_WF"].RunChildWf;
        var actualInputs = actualRunChildWfNode.Inputs;

        var actualWfInput1 = actualInputs["wf-input-1"];
        var expectedWfInput1 = new VariableAssignment
        {
          VariableName="input-var"
        };

        var actualWfInput2 = actualInputs["wf-input-2"];
        var expectedWfInput2 = new VariableAssignment
        {
          LiteralValue = new VariableValue{
            Int = 1000
          }
        };

        Assert.Equal(expectedWfInput1, actualWfInput1);
        Assert.Equal(expectedWfInput2, actualWfInput2);
    }

    [Fact]
    public void WaitForChildWf_ShouldCompileSuccessfully()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            var parentVar = wf.DeclareInt("parent-var");
            var inputVar = wf.DeclareStr("input-var");
            var spawnedChildWf = wf.RunWf("child-wf", new Dictionary<string, object>{});
            wf.WaitForChildWf(spawnedChildWf);
            
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        var compiledWfThread = workflowThread.Compile();

        var actualWaitForChildWfNode = compiledWfThread.Nodes["2-wait-WAIT_FOR_CHILD_WF"].WaitForChildWf;
        var expectedWaitForChildWfNode = new WaitForChildWfNode{
          ChildWfRunSourceNode = "1-run-child-wf-RUN_CHILD_WF",
          ChildWfRunId = new VariableAssignment{
            NodeOutput = new VariableAssignment.Types.NodeOutputReference
            {
              NodeName = "1-run-child-wf-RUN_CHILD_WF"
            }  
          }
        };

        Assert.Equal(expectedWaitForChildWfNode, actualWaitForChildWfNode);
    }
}