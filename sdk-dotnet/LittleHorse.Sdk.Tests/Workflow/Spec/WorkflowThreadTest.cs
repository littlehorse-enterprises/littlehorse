using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Helper;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowThreadTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WorkflowThreadTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact]
    public void WorkflowThread_WithEmptyFunction_ShouldCompileSpecEntrypointAndExitNodes()
    {
        var workflowName = "TestWorkflow";
        void Entrypoint(WorkflowThread thread)
        {
            
        }
        var mockWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var workflowThread = new WorkflowThread(mockWorkflow.Object, Entrypoint);

        var wfThreadCompiled = workflowThread.Compile();
        
        var actualResult = LHMappingHelper.ProtoToJson(wfThreadCompiled);
        var expectedResult =
            "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": [ { \"sinkNodeName\":" +
            " \"1-exit-EXIT\", \"variableMutations\": [ ] } ], " +
            "\"failureHandlers\": [ ], \"entrypoint\": { } }, " +
            "\"1-exit-EXIT\": { \"outgoingEdges\": [ ], \"failureHandlers\": [ ], " +
            "\"exit\": { } } }, \"variableDefs\": [ ], \"interruptDefs\": [ ] }";
        var expectedNumberOfNodes = 2;
        Assert.Equal(expectedNumberOfNodes, wfThreadCompiled.Nodes.Count);
        Assert.Equal(expectedResult, actualResult);
    }

    [Fact]
    public void WorkflowThread_InvokingAddVariables_ShouldBuildSpecWithNodesAndVariablesDefs()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void AddVariablesAction(WorkflowThread wf)
        {
            wf.AddVariable("str-test-variable", VariableType.Str);
            wf.AddVariable("int-test-variable", 5);
        }

        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, AddVariablesAction);
        
        var wfThreadCompiled = workflowThread.Compile();
        
        var actualResult = LHMappingHelper.ProtoToJson(wfThreadCompiled);
        var expectedResult = "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": " +
                             "[ { \"sinkNodeName\": \"1-exit-EXIT\", \"variableMutations\": [ ] } ], " +
                             "\"failureHandlers\": [ ], \"entrypoint\": { } }, \"1-exit-EXIT\": { \"outgoingEdges\": [ ], " +
                             "\"failureHandlers\": [ ], \"exit\": { } } }, \"variableDefs\": [ { \"varDef\": " +
                             "{ \"type\": \"STR\", \"name\": \"str-test-variable\", \"maskedValue\": false }, " +
                             "\"required\": false, \"searchable\": false, \"jsonIndexes\": [ ], \"accessLevel\": " +
                             "\"PRIVATE_VAR\" }, { \"varDef\": { \"type\": \"INT\", \"name\": \"int-test-variable\", " +
                             "\"defaultValue\": { \"int\": \"5\" }, \"maskedValue\": false }, " +
                             "\"required\": false, \"searchable\": false, \"jsonIndexes\": [ ], " +
                             "\"accessLevel\": \"PRIVATE_VAR\" } ], \"interruptDefs\": [ ] }";
        
        var expectedNumberOfNodes = 2;
        Assert.Equal(expectedNumberOfNodes, wfThreadCompiled.Nodes.Count);
        Assert.Equal(expectedResult, actualResult);
    }
    
    [Fact]
    public void WfThread_InvokingActionAfterItsInstantiation_ShouldThrowAnError()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var wfThread = new WorkflowThread(mockParentWorkflow.Object, ParentEntrypoint);
        
        var exception = Assert.Throws<InvalidOperationException>(() => wfThread.Execute("test-task-name"));
            
        Assert.Equal("Using an inactive thread", exception.Message);
    }

    [Fact]
    public void WfThread_InvokingExecuteTasksWithArgs_ShouldReturnATaskTypeNodeOutput()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void ExecuteAction(WorkflowThread wf)
        {
            var expectedTaskName = "test-task-name";
            var variable = wf.AddVariable("str-test-variable", VariableType.Str);
            var actualNodeOutput = wf.Execute(expectedTaskName, variable);
            
            Assert.Contains(expectedTaskName + "-TASK", actualNodeOutput.NodeName);
        }
        
        new WorkflowThread(mockParentWorkflow.Object, ExecuteAction);
    }
    
    [Fact]
    public void WfThread_InvokingExecuteTasksWithNoArgs_ShouldReturnATaskTypeNodeOutput()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void ExecuteAction(WorkflowThread wf)
        {
            var expectedTaskName = "test-task-name";
            var actualNodeOutput = wf.Execute(expectedTaskName);
            
            Assert.Contains(expectedTaskName + "-TASK", actualNodeOutput.NodeName);
        }
        
        new WorkflowThread(mockParentWorkflow.Object, ExecuteAction);
    }

    [Fact]
    public void WfThread_InvokingExecuteTasksWithArgs_ShouldBuildSpecWithTaskNodeAndVariablesDefs()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            var variableDef = wf.AddVariable("str-test-variable", VariableType.Str);
            wf.Execute("test-task-name", variableDef);
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var wfThreadCompiled = workflowThread.Compile();
        
        var actualResult = LHMappingHelper.ProtoToJson(wfThreadCompiled);
        var expectedResult =
            "{ \"nodes\": { \"0-entrypoint-ENTRYPOINT\": { \"outgoingEdges\": [ { \"sinkNodeName\": " +
            "\"1-test-task-name-TASK\", \"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"entrypoint\": " +
            "{ } }, \"1-test-task-name-TASK\": { \"outgoingEdges\": [ { \"sinkNodeName\": \"2-exit-EXIT\", " +
            "\"variableMutations\": [ ] } ], \"failureHandlers\": [ ], \"task\": { \"taskDefId\": { \"name\": " +
            "\"test-task-name\" }, \"timeoutSeconds\": 0, \"retries\": 0, \"variables\": [ { \"variableName\": " +
            "\"str-test-variable\" } ] } }, \"2-exit-EXIT\": { \"outgoingEdges\": [ ], \"failureHandlers\": [ ], " +
            "\"exit\": { } } }, \"variableDefs\": [ { \"varDef\": { \"type\": \"STR\", \"name\": \"str-test-variable\", " +
            "\"maskedValue\": false }, \"required\": false, \"searchable\": false, \"jsonIndexes\": [ ], " +
            "\"accessLevel\": \"PRIVATE_VAR\" } ], \"interruptDefs\": [ ] }";
        var expectedNumberOfNodes = 3;
        Assert.Equal(expectedNumberOfNodes, wfThreadCompiled.Nodes.Count);
        Assert.Equal(expectedResult, actualResult);
    }
    
    [Fact]
    public void WfThread_WithExternalEvent_ShouldCompile()
    {
        var numberOfExitNodes = 1;
        var numberOfEntrypointNodes = 1;
        var numberOfExternalEvents = 1;
        var numberOfTasks = 1;
        var workflowName = "TestWorkflow";
        var timeoutInSeconds = 30;
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        void EntryPointAction(WorkflowThread wf)
        {
            WfRunVariable name = wf.DeclareStr("name");
            name.Assign(wf.WaitForEvent("name-event").WithTimeout(timeoutInSeconds));
            wf.Execute("greet", name);
        }
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge { SinkNodeName = "1-name-event-EXTERNAL_EVENT" }
            }
        };

        var externalEvent = new Node
        {
            ExternalEvent = new ExternalEventNode
            {
                ExternalEventDefId = new ExternalEventDefId { Name = "name-event" },
                TimeoutSeconds = new VariableAssignment { LiteralValue = new VariableValue { Int = timeoutInSeconds }}
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-greet-TASK",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "name",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput =
                                    new VariableAssignment.Types.NodeOutputReference
                                        { NodeName = "1-name-event-EXTERNAL_EVENT" }
                            }
                        }
                    }
                }
            }
        };

        var greetTask = new Node
        {
            Task = new TaskNode
            {
                TaskDefId = new TaskDefId { Name = "greet" }, Variables =
                {
                    new VariableAssignment { VariableName = "name" }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "3-exit-EXIT" } }
        };

        var exitNode = new Node
        {
            Exit = new ExitNode()
        };
        
        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Name = "name",
                Type = VariableType.Str
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-name-event-EXTERNAL_EVENT", externalEvent);
        expectedSpec.Nodes.Add("2-greet-TASK", greetTask);
        expectedSpec.Nodes.Add("3-exit-EXIT", exitNode);
        expectedSpec.VariableDefs.Add(threadVarDef);

        var expectedNumberOfNodes = numberOfEntrypointNodes + numberOfExitNodes + numberOfExternalEvents + numberOfTasks;
        Assert.Equal(expectedNumberOfNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }
}