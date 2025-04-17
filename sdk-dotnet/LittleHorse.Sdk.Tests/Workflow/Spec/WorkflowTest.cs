using System;
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
            wf.DeclareStr("input-name");
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

    [Fact]
    public void WorkflowCompiled_WithJsonFormat_ShouldSaveFileInDirectoryLocatedInBaseFolder()
    {
        void Entrypoint(WorkflowThread wf)
        {
        }
        var wfName = "example-basic";
        string fileName = wfName + LHConstants.SuffixCompiledWfFileName;
        var workflow = new Sdk.Workflow.Spec.Workflow(wfName, Entrypoint);
        
        string directory = "Test";
        workflow.CompileAndSaveToDisk(directory);
        string actualContent = TestUtils.GetContentFromFilePath(directory, fileName);
        
        Assert.False(string.IsNullOrWhiteSpace(actualContent));
        
        TestUtils.RemoveDirectory(directory);
    }
    
    [Fact]
    public void WorkflowCompiled_WithNullDirectory_ShouldThrowAnException()
    {
        var wfName = "example-basic";
        string fileName = wfName + LHConstants.SuffixCompiledWfFileName;
        void Entrypoint(WorkflowThread wf)
        {
        }
        var workflow = new Sdk.Workflow.Spec.Workflow(wfName, Entrypoint);
        
        var exception = Assert.Throws<Exception>(() => workflow.CompileAndSaveToDisk(directory: null!));
            
        Assert.Contains($"Something occurred trying to save file {fileName} to disk", exception.Message);
    }
    
    [Fact]
    public void WorkflowCompiled_WithEmptyDirectoryName_ShouldSaveFileInBaseFolder()
    {
        void Entrypoint(WorkflowThread wf)
        {
        }
        var wfName = "example-basic";
        string fileName = wfName + LHConstants.SuffixCompiledWfFileName;
        var workflow = new Sdk.Workflow.Spec.Workflow(wfName, Entrypoint);
        
        workflow.CompileAndSaveToDisk(directory: string.Empty);
        string actualContent = TestUtils.GetContentFromFilePath(directory: string.Empty, fileName);
        
        Assert.False(string.IsNullOrWhiteSpace(actualContent));
        
        TestUtils.RemoveFile(fileName);
    }
    
    [Fact]
    public void Workflow_WithChildVariableAssignedToParentVariable_ShouldCompileSuccessfully()
    {
        var workflowName = "TestWorkflow";

        void EntryPointAction(WorkflowThread grandParentWfThread)
        {
            var grandParentVar = grandParentWfThread.DeclareStr("grand-parent-var");
            grandParentWfThread.SpawnThread("son-thread", son =>
            {
                grandParentVar.Assign("son-value");
                son.SpawnThread("grandchild-thread", grandchild =>
                {
                    grandParentVar.Assign("grandchild-value");
                });
            });                                                                                                                                                                                                                                                                                                                                       
        }
        
        var workflow = new Sdk.Workflow.Spec.Workflow(workflowName, EntryPointAction);
        var actualResult = workflow.Compile();

        var expectedResult = new PutWfSpecRequest
        {
            Name = workflowName,
            EntrypointThreadName = "entrypoint",
        };
        
        var entryPointThreadSpec = new ThreadSpec();
        var entrypoint = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "1-son-thread-START_THREAD"
                }
            }
        };

        var sonThreadNode = new Node
        {
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "son-thread"
            },
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-exit-EXIT",
                    VariableMutations =
                    {
                        new VariableMutation
                        {
                            LhsName = "1-son-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "1-son-thread-START_THREAD"
                                }
                            }
                        }
                    }
                }
            }
        };
        
        var exit = new Node { Exit = new ExitNode() };
        var threadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Type = VariableType.Str,
                Name = "grand-parent-var"
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        var threadVar2Def = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Type = VariableType.Int,
                Name = "1-son-thread-START_THREAD"
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        
        entryPointThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypoint);
        entryPointThreadSpec.Nodes.Add("1-son-thread-START_THREAD", sonThreadNode);
        entryPointThreadSpec.Nodes.Add("2-exit-EXIT", exit);
        entryPointThreadSpec.VariableDefs.Add(threadVarDef);
        entryPointThreadSpec.VariableDefs.Add(threadVar2Def);
        
        var sonThreadSpec = new ThreadSpec();
        var entrypointSonNode = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "1-grandchild-thread-START_THREAD",
                    VariableMutations =
                    {
                        new VariableMutation
                        { 
                            LhsName = "grand-parent-var",
                            RhsAssignment = new VariableAssignment
                            {
                                LiteralValue = new VariableValue
                                {
                                    Str = "son-value"
                                }
                            }
                        }
                    }
                }
            }
        };
        
        var sonNode = new Node
        {
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "2-exit-EXIT",
                    VariableMutations =
                    {
                        new VariableMutation
                        { 
                            LhsName = "1-grandchild-thread-START_THREAD",
                            RhsAssignment = new VariableAssignment
                            {
                                NodeOutput = new VariableAssignment.Types.NodeOutputReference
                                {
                                    NodeName = "1-grandchild-thread-START_THREAD"
                                }
                            }
                        }
                    }
                }
            },
            StartThread = new StartThreadNode
            {
                ThreadSpecName = "grandchild-thread"
            }
        };
        
        var sonThreadVarDef = new ThreadVarDef
        {
            VarDef = new VariableDef
            {
                Type = VariableType.Int,
                Name = "1-grandchild-thread-START_THREAD"
            },
            AccessLevel = WfRunVariableAccessLevel.PrivateVar
        };
        sonThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypointSonNode);
        sonThreadSpec.Nodes.Add("1-grandchild-thread-START_THREAD", sonNode);
        sonThreadSpec.Nodes.Add("2-exit-EXIT", exit);
        sonThreadSpec.VariableDefs.Add(sonThreadVarDef);
        
        var grandChildThreadSpec = new ThreadSpec();
        
        var entrypointGrandChildNode = new Node
        {
            Entrypoint = new EntrypointNode(),
            OutgoingEdges =
            {
                new Edge
                {
                    SinkNodeName = "1-exit-EXIT",
                    VariableMutations =
                    {
                        new VariableMutation
                        { 
                            LhsName = "grand-parent-var",
                            RhsAssignment = new VariableAssignment
                            {
                                LiteralValue = new VariableValue
                                {
                                    Str = "grandchild-value"
                                }
                            }
                        }
                    }
                }
            }
        };
        grandChildThreadSpec.Nodes.Add("0-entrypoint-ENTRYPOINT", entrypointGrandChildNode);
        grandChildThreadSpec.Nodes.Add("1-exit-EXIT", exit);
        
        expectedResult.ThreadSpecs.Add("entrypoint", entryPointThreadSpec);
        expectedResult.ThreadSpecs.Add("son-thread", sonThreadSpec);
        expectedResult.ThreadSpecs.Add("grandchild-thread", grandChildThreadSpec);
        
        Assert.Equal(expectedResult, actualResult);
    }
}