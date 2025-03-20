using System;
using LittleHorse.Sdk.Common.Proto;
using LittleHorse.Sdk.Workflow.Spec;
using Moq;
using Xunit;
using static LittleHorse.Sdk.Common.Proto.UTActionTrigger.Types;

namespace LittleHorse.Sdk.Tests.Workflow.Spec;

public class WorkflowThreadUserTasksTest
{
    private Action<WorkflowThread> _action;
    void ParentEntrypoint(WorkflowThread thread)
    {
    }
    
    public WorkflowThreadUserTasksTest()
    {
        LHLoggerFactoryProvider.Initialize(null);
        _action = ParentEntrypoint;
    }
    
    [Fact]
    public void WfThread_WithUserTaskAssignedToGroup_ShouldCompile()
    {
        var numberOfExpectedNodes = 3;
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            UserTaskOutput formOutput = wf.AssignUserTask(
                "user-task-def-name",
                null,
                "testGroup"
            );
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();

        var expectedSpec = new ThreadSpec();

        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges = { new Edge { SinkNodeName = "1-user-task-def-name-USER_TASK" } }
        };

        var userTask = new Node
        {
            UserTask = new UserTaskNode
            {
                UserTaskDefName = "user-task-def-name",
                UserGroup = new VariableAssignment { LiteralValue = new VariableValue { Str = "testGroup" } }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-exit-EXIT" } }
        };
        
        var exitNode = new Node { Exit = new ExitNode() };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-user-task-def-name-USER_TASK", userTask);
        expectedSpec.Nodes.Add("2-exit-EXIT", exitNode);
        
        Assert.Equal(numberOfExpectedNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }

    [Fact]
    public void WfThread_WithUserTaskAssignedToNobody_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            UserTaskOutput formOutput = wf.AssignUserTask(
                "user-task-def-name",
                null,
                null
            );
        }
        
        var exception = Assert.Throws<ArgumentException>(() => 
            new WorkflowThread(mockParentWorkflow.Object, EntryPointAction));
            
        Assert.Contains("userId or userGroup is required.", exception.Message);
    }

    [Fact]
    public void WfThread_WithDeadAssignedUserTask_ShouldCompileAReassignment()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        var numberOfExpectedNodes = 3;

        void EntryPointAction(WorkflowThread wf)
        {
            UserTaskOutput formOutput = wf.AssignUserTask(
                "user-task-def-name",
                "Patrick",
                "test-group"
            );
            
            wf.ReleaseToGroupOnDeadline(formOutput, 120);
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedSpec = new ThreadSpec();

        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges = { new Edge { SinkNodeName = "1-user-task-def-name-USER_TASK" } }
        };

        var userTask = new Node
        {
            UserTask = new UserTaskNode
            {
                UserTaskDefName = "user-task-def-name",
                UserGroup = new VariableAssignment { LiteralValue = new VariableValue { Str = "test-group" } },
                UserId = new VariableAssignment { LiteralValue = new VariableValue { Str = "Patrick" } },
                Actions =
                {
                    new UTActionTrigger
                    {
                        Hook = UTHook.OnTaskAssigned,
                        Reassign = new UTAReassign
                        {
                            UserGroup = new VariableAssignment
                                { LiteralValue = new VariableValue { Str = "test-group" } },
                        },
                        DelaySeconds = new VariableAssignment { LiteralValue = new VariableValue { Int = 120 } }
                    }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-exit-EXIT" } }
        };
        
        var exitNode = new Node { Exit = new ExitNode() };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-user-task-def-name-USER_TASK", userTask);
        expectedSpec.Nodes.Add("2-exit-EXIT", exitNode);
        
        Assert.Equal(numberOfExpectedNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }

    [Fact]
    public void WfThread_WithoutUserAssignedToUserTaskInDeadline_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void EntryPointAction(WorkflowThread wf)
        {
            UserTaskOutput formOutput = wf.AssignUserTask(
                "user-task-def-name",
                null,
                "test-group"
            );
            
            wf.ReleaseToGroupOnDeadline(formOutput, 120);
        }
        
        var exception = Assert.Throws<InvalidOperationException>(() => 
            new WorkflowThread(mockParentWorkflow.Object, EntryPointAction));
            
        Assert.Equal("The User Task is not assigned to any user.", exception.Message);
    }
    
    [Fact]
    public void WfThread_WithoutUserGroupAssignedToUserTaskInDeadline_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void EntryPointAction(WorkflowThread wf)
        {
            UserTaskOutput formOutput = wf.AssignUserTask(
                "user-task-def-name",
                "Patrick",
                null
            );
            
            wf.ReleaseToGroupOnDeadline(formOutput, 120);
        }
        
        var exception = Assert.Throws<InvalidOperationException>(() => 
            new WorkflowThread(mockParentWorkflow.Object, EntryPointAction));
            
        Assert.Equal("The User Task is assigned to a user without a group.", exception.Message);
    }

    [Fact]
    public void WfThread_WithUserTaskAssignedToUserAndGroup_ShouldCompileWithAReassignmentToOtherUserAndGroup()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);
        
        void EntryPointAction(WorkflowThread wf)
        {
            UserTaskOutput formOutput = wf.AssignUserTask(
                "user-task-def-name",
                "Patrick",
                "test-group"
            );
            
            wf.ReassignUserTask(formOutput, "any-user", "any-group", 120);
        }
        
        var workflowThread = new WorkflowThread(mockParentWorkflow.Object, EntryPointAction);
        
        var compiledWfThread = workflowThread.Compile();
        
        var expectedSpec = new ThreadSpec();
        
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges = { new Edge { SinkNodeName = "1-user-task-def-name-USER_TASK" } }
        };

        var userTask = new Node
        {
            UserTask = new UserTaskNode
            {
                UserTaskDefName = "user-task-def-name",
                UserGroup = new VariableAssignment { LiteralValue = new VariableValue { Str = "test-group" } },
                UserId = new VariableAssignment { LiteralValue = new VariableValue { Str = "Patrick" } },
                Actions =
                {
                    new UTActionTrigger
                    {
                        Hook = UTHook.OnTaskAssigned,
                        Reassign = new UTAReassign
                        {
                            UserGroup = new VariableAssignment
                                { LiteralValue = new VariableValue { Str = "any-group" } },
                            UserId = new VariableAssignment
                                { LiteralValue = new VariableValue { Str = "any-user" } }
                        },
                        DelaySeconds = new VariableAssignment { LiteralValue = new VariableValue { Int = 120 } }
                    }
                }
            },
            OutgoingEdges = { new Edge { SinkNodeName = "2-exit-EXIT" } }
        };
        
        var exitNode = new Node { Exit = new ExitNode() };
        
        expectedSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        expectedSpec.Nodes.Add("1-user-task-def-name-USER_TASK", userTask);
        expectedSpec.Nodes.Add("2-exit-EXIT", exitNode);

        const int numberOfExpectedNodes = 3;
        Assert.Equal(numberOfExpectedNodes, compiledWfThread.Nodes.Count);
        Assert.Equal(expectedSpec, compiledWfThread);
    }

    [Fact]
    public void WfThread_WithUserTaskReassignedToNobody_ShouldThrowAnException()
    {
        var workflowName = "TestWorkflow";
        var mockParentWorkflow = new Mock<Sdk.Workflow.Spec.Workflow>(workflowName, _action);

        void EntryPointAction(WorkflowThread wf)
        {
            UserTaskOutput formOutput = wf.AssignUserTask(
                "user-task-def-name",
                "Patrick",
                "test-group"
            );

            wf.ReassignUserTask(formOutput, null, null, 120);
        }
        
        var exception = Assert.Throws<ArgumentException>(() => 
            new WorkflowThread(mockParentWorkflow.Object, EntryPointAction));
            
        Assert.Equal("userId or userGroup is required.", exception.Message);
    }
}