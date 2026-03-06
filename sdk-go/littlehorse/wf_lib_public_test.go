package littlehorse_test

import (
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"google.golang.org/protobuf/proto"

	"github.com/stretchr/testify/assert"
)

func TestShouldCompileWorkflowWithContainsCondition(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myVar := thread.DeclareStr("my-var")
		thread.DoIf(myVar.DoesContain("this-value"), func(t *littlehorse.WorkflowThread) {
			t.Execute("task")
		})
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	actualNode := entrypoint.Nodes["1-nop-NOP"]

	expectedNode := lhproto.Node{
		Node: &lhproto.Node_Nop{},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-task-TASK",
				EdgeCondition: &lhproto.Edge_LegacyCondition{
					LegacyCondition: &lhproto.LegacyEdgeCondition{
						Comparator: lhproto.Comparator_IN,
						Left: &lhproto.VariableAssignment{
							Source: &lhproto.VariableAssignment_LiteralValue{
								LiteralValue: &lhproto.VariableValue{
									Value: &lhproto.VariableValue_Str{Str: "this-value"},
								},
							},
						},
						Right: &lhproto.VariableAssignment{
							Source: &lhproto.VariableAssignment_VariableName{
								VariableName: "my-var",
							},
						},
					},
				},
			},
			{
				SinkNodeName: "3-nop-NOP",
			},
		},
	}

	assert.Nil(t, err)
	assert.True(t, proto.Equal(&expectedNode, actualNode))
}

func TestShouldCompileWorkflowWithNotContainsCondition(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myVar := thread.DeclareStr("my-var")
		thread.DoIf(myVar.DoesNotContain("this-value"), func(t *littlehorse.WorkflowThread) {
			t.Execute("task")
		})
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	actualNode := entrypoint.Nodes["1-nop-NOP"]

	expectedNode := lhproto.Node{
		Node: &lhproto.Node_Nop{},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-task-TASK",
				EdgeCondition: &lhproto.Edge_LegacyCondition{
					LegacyCondition: &lhproto.LegacyEdgeCondition{
						Comparator: lhproto.Comparator_NOT_IN,
						Left: &lhproto.VariableAssignment{
							Source: &lhproto.VariableAssignment_LiteralValue{
								LiteralValue: &lhproto.VariableValue{
									Value: &lhproto.VariableValue_Str{Str: "this-value"},
								},
							},
						},
						Right: &lhproto.VariableAssignment{
							Source: &lhproto.VariableAssignment_VariableName{
								VariableName: "my-var",
							},
						},
					},
				},
			},
			{
				SinkNodeName: "3-nop-NOP",
			},
		},
	}

	assert.Nil(t, err)
	assert.True(t, proto.Equal(&expectedNode, actualNode))
}

func TestShouldCompileWorkflowWithInCondition(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myVar := thread.DeclareStr("my-var")
		thread.DoIf(myVar.IsIn([3]string{"A", "B", "C"}), func(t *littlehorse.WorkflowThread) {
			t.Execute("task")
		})
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	actualNode := entrypoint.Nodes["1-nop-NOP"]

	expectedNode := lhproto.Node{
		Node: &lhproto.Node_Nop{},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-task-TASK",
				EdgeCondition: &lhproto.Edge_LegacyCondition{
					LegacyCondition: &lhproto.LegacyEdgeCondition{
						Comparator: lhproto.Comparator_IN,
						Left: &lhproto.VariableAssignment{
							Source: &lhproto.VariableAssignment_VariableName{
								VariableName: "my-var",
							},
						},
						Right: &lhproto.VariableAssignment{
							Source: &lhproto.VariableAssignment_LiteralValue{
								LiteralValue: &lhproto.VariableValue{
									Value: &lhproto.VariableValue_JsonObj{JsonObj: "[\"A\",\"B\",\"C\"]"},
								},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "3-nop-NOP",
			},
		},
	}

	assert.Nil(t, err)
	assert.True(t, proto.Equal(&expectedNode, actualNode))
}

func TestShouldCompileWorkflowWithWaitForConditionNodes(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.WaitForCondition(thread.Condition("some-value", lhproto.Comparator_EQUALS, "some-other-value"))
	}, "my-workflow")
	compiledWorkflow, _ := wf.Compile()
	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	assert.Equal(t, 3, len(entrypoint.Nodes))
	wfcn := entrypoint.Nodes["1-wait-for-condition-WAIT_FOR_CONDITION"].Node.(*lhproto.Node_WaitForCondition).WaitForCondition
	assert.Equal(t, lhproto.Comparator_EQUALS, wfcn.GetLegacyCondition().Comparator)
	assert.Equal(t, "some-value", wfcn.GetLegacyCondition().Left.Source.(*lhproto.VariableAssignment_LiteralValue).LiteralValue.Value.(*lhproto.VariableValue_Str).Str)
	assert.Equal(t, "some-other-value", wfcn.GetLegacyCondition().Right.Source.(*lhproto.VariableAssignment_LiteralValue).LiteralValue.Value.(*lhproto.VariableValue_Str).Str)
}

func TestShouldCompileWorkflowWithNotInCondition(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myVar := thread.DeclareStr("my-var")
		thread.DoIf(myVar.IsNotIn([3]string{"A", "B", "C"}), func(t *littlehorse.WorkflowThread) {
			t.Execute("task")
		})
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	actualNode := entrypoint.Nodes["1-nop-NOP"]

	expectedNode := lhproto.Node{
		Node: &lhproto.Node_Nop{},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-task-TASK",
				EdgeCondition: &lhproto.Edge_LegacyCondition{
					LegacyCondition: &lhproto.LegacyEdgeCondition{
						Comparator: lhproto.Comparator_NOT_IN,
						Left: &lhproto.VariableAssignment{
							Source: &lhproto.VariableAssignment_VariableName{
								VariableName: "my-var",
							},
						},
						Right: &lhproto.VariableAssignment{
							Source: &lhproto.VariableAssignment_LiteralValue{
								LiteralValue: &lhproto.VariableValue{
									Value: &lhproto.VariableValue_JsonObj{JsonObj: "[\"A\",\"B\",\"C\"]"},
								},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "3-nop-NOP",
			},
		},
	}

	assert.Nil(t, err)
	assert.True(t, proto.Equal(&expectedNode, actualNode))
}

// LHPath tests

func TestGetOnStructVariableProducesLhPath(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myStruct := thread.DeclareStruct("my-struct", "person")
		thread.Execute("greet", myStruct.Get("firstName"))
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	taskNode := entrypoint.Nodes["1-greet-TASK"].GetTask()

	expectedAssignment := &lhproto.VariableAssignment{
		Source: &lhproto.VariableAssignment_VariableName{
			VariableName: "my-struct",
		},
		Path: &lhproto.VariableAssignment_LhPath{
			LhPath: &lhproto.LHPath{
				Path: []*lhproto.LHPath_Selector{
					{SelectorType: &lhproto.LHPath_Selector_Key{Key: "firstName"}},
				},
			},
		},
	}

	assert.True(t, proto.Equal(expectedAssignment, taskNode.Variables[0]))
}

func TestChainedGetProducesNestedLhPath(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myStruct := thread.DeclareStruct("my-struct", "person")
		thread.Execute("process-city", myStruct.Get("homeAddress").Get("city"))
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	taskNode := entrypoint.Nodes["1-process-city-TASK"].GetTask()

	expectedAssignment := &lhproto.VariableAssignment{
		Source: &lhproto.VariableAssignment_VariableName{
			VariableName: "my-struct",
		},
		Path: &lhproto.VariableAssignment_LhPath{
			LhPath: &lhproto.LHPath{
				Path: []*lhproto.LHPath_Selector{
					{SelectorType: &lhproto.LHPath_Selector_Key{Key: "homeAddress"}},
					{SelectorType: &lhproto.LHPath_Selector_Key{Key: "city"}},
				},
			},
		},
	}

	assert.True(t, proto.Equal(expectedAssignment, taskNode.Variables[0]))
}

func TestGetIndexProducesIndexSelector(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myArr := thread.DeclareJsonArr("my-arr")
		thread.Execute("process", myArr.GetIndex(0))
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	taskNode := entrypoint.Nodes["1-process-TASK"].GetTask()

	expectedAssignment := &lhproto.VariableAssignment{
		Source: &lhproto.VariableAssignment_VariableName{
			VariableName: "my-arr",
		},
		Path: &lhproto.VariableAssignment_LhPath{
			LhPath: &lhproto.LHPath{
				Path: []*lhproto.LHPath_Selector{
					{SelectorType: &lhproto.LHPath_Selector_Index{Index: 0}},
				},
			},
		},
	}

	assert.True(t, proto.Equal(expectedAssignment, taskNode.Variables[0]))
}

func TestGetAndGetIndexChaining(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myObj := thread.DeclareJsonObj("my-obj")
		thread.Execute("process", myObj.Get("items").GetIndex(0).Get("name"))
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	taskNode := entrypoint.Nodes["1-process-TASK"].GetTask()

	expectedAssignment := &lhproto.VariableAssignment{
		Source: &lhproto.VariableAssignment_VariableName{
			VariableName: "my-obj",
		},
		Path: &lhproto.VariableAssignment_LhPath{
			LhPath: &lhproto.LHPath{
				Path: []*lhproto.LHPath_Selector{
					{SelectorType: &lhproto.LHPath_Selector_Key{Key: "items"}},
					{SelectorType: &lhproto.LHPath_Selector_Index{Index: 0}},
					{SelectorType: &lhproto.LHPath_Selector_Key{Key: "name"}},
				},
			},
		},
	}

	assert.True(t, proto.Equal(expectedAssignment, taskNode.Variables[0]))
}

func TestGetOnTaskOutputProducesLhPath(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		result := thread.Execute("get-person")
		thread.Execute("process", result.Get("firstName"))
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	taskNode := entrypoint.Nodes["2-process-TASK"].GetTask()

	expectedAssignment := &lhproto.VariableAssignment{
		Source: &lhproto.VariableAssignment_NodeOutput{
			NodeOutput: &lhproto.VariableAssignment_NodeOutputReference{
				NodeName: "1-get-person-TASK",
			},
		},
		Path: &lhproto.VariableAssignment_LhPath{
			LhPath: &lhproto.LHPath{
				Path: []*lhproto.LHPath_Selector{
					{SelectorType: &lhproto.LHPath_Selector_Key{Key: "firstName"}},
				},
			},
		},
	}

	assert.True(t, proto.Equal(expectedAssignment, taskNode.Variables[0]))
}

func TestCannotMixJsonPathAndGet(t *testing.T) {
	assert.Panics(t, func() {
		wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
			myObj := thread.DeclareJsonObj("my-obj")
			thread.Execute("process", myObj.JsonPath("$.field").Get("name"))
		}, "my-workflow")
		wf.Compile()
	})
}

func TestCannotMixGetAndJsonPath(t *testing.T) {
	assert.Panics(t, func() {
		wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
			myObj := thread.DeclareJsonObj("my-obj")
			thread.Execute("process", myObj.Get("field").JsonPath("$.name"))
		}, "my-workflow")
		wf.Compile()
	})
}

func TestCannotGetOnPrimitiveType(t *testing.T) {
	assert.Panics(t, func() {
		wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
			myStr := thread.DeclareStr("my-str")
			thread.Execute("process", myStr.Get("field"))
		}, "my-workflow")
		wf.Compile()
	})
}

func TestGetDoesNotMutateOriginalVariable(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myStruct := thread.DeclareStruct("my-struct", "person")
		thread.Execute("task1", myStruct.Get("firstName"))
		thread.Execute("task2", myStruct)
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()
	assert.Nil(t, err)

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]

	task1 := entrypoint.Nodes["1-task1-TASK"].GetTask()
	assert.NotNil(t, task1.Variables[0].GetLhPath())

	task2 := entrypoint.Nodes["2-task2-TASK"].GetTask()
	assert.Nil(t, task2.Variables[0].GetLhPath())
	assert.Equal(t, "", task2.Variables[0].GetJsonPath())
}
