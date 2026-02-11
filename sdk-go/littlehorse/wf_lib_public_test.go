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
				Condition: &lhproto.EdgeCondition{
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
				Condition: &lhproto.EdgeCondition{
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
				Condition: &lhproto.EdgeCondition{
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
	assert.Equal(t, lhproto.Comparator_EQUALS, wfcn.Condition.Comparator)
	assert.Equal(t, "some-value", wfcn.Condition.Left.Source.(*lhproto.VariableAssignment_LiteralValue).LiteralValue.Value.(*lhproto.VariableValue_Str).Str)
	assert.Equal(t, "some-other-value", wfcn.Condition.Right.Source.(*lhproto.VariableAssignment_LiteralValue).LiteralValue.Value.(*lhproto.VariableValue_Str).Str)
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
				Condition: &lhproto.EdgeCondition{
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
			{
				SinkNodeName: "3-nop-NOP",
			},
		},
	}

	assert.Nil(t, err)
	assert.True(t, proto.Equal(&expectedNode, actualNode))
}
