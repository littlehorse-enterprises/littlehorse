package littlehorse_test

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

func TestCastExpressionSetsTargetType(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareBool("my-var")
		myVar.Assign(t.Execute("string-method").CastToBool())
	}, "cast-test-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-string-method-TASK"]
	assert.NotNil(t, taskNode)

	// The outgoing edge from the task should contain the variable mutation
	// whose RHS assignment is the node output assignment we created above.
	assert.Greater(t, len(taskNode.OutgoingEdges), 0)
	vmuts := taskNode.OutgoingEdges[0].VariableMutations
	assert.Greater(t, len(vmuts), 0)

	rhs := vmuts[0].GetRhsAssignment()
	assert.NotNil(t, rhs)
	assert.NotNil(t, rhs.GetTargetType())
	assert.Equal(t, lhproto.VariableType_BOOL, rhs.GetTargetType().GetPrimitiveType())
}

func TestCastNodeOutputToInt(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareInt("my-var")
		myVar.Assign(t.Execute("double-method").CastToInt())
	}, "cast-test-nodeoutput-int")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-double-method-TASK"]
	assert.NotNil(t, taskNode)

	vmuts := taskNode.OutgoingEdges[0].VariableMutations
	assert.Greater(t, len(vmuts), 0)
	rhs := vmuts[0].GetRhsAssignment()
	assert.NotNil(t, rhs.GetTargetType())
	assert.Equal(t, lhproto.VariableType_INT, rhs.GetTargetType().GetPrimitiveType())
}

func TestCastStringToDouble(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareDouble("my-var")
		myVar.Assign(t.Execute("string-method").CastToDouble())
	}, "cast-test-str-to-double")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-string-method-TASK"]
	assert.NotNil(t, taskNode)

	vmuts := taskNode.OutgoingEdges[0].VariableMutations
	assert.Greater(t, len(vmuts), 0)
	rhs := vmuts[0].GetRhsAssignment()
	assert.NotNil(t, rhs.GetTargetType())
	assert.Equal(t, lhproto.VariableType_DOUBLE, rhs.GetTargetType().GetPrimitiveType())
}

func TestCastStringToBool(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareBool("my-var")
		myVar.Assign(t.Execute("string-method").CastToBool())
	}, "cast-test-str-to-bool")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-string-method-TASK"]
	assert.NotNil(t, taskNode)

	vmuts := taskNode.OutgoingEdges[0].VariableMutations
	assert.Greater(t, len(vmuts), 0)
	rhs := vmuts[0].GetRhsAssignment()
	assert.NotNil(t, rhs.GetTargetType())
	assert.Equal(t, lhproto.VariableType_BOOL, rhs.GetTargetType().GetPrimitiveType())
}

func TestJsonPathCastToInt(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareInt("my-var")
		myVar.Assign(t.Execute("int-method").JsonPath("$.int").CastToInt())
	}, "cast-test-jsonpath-to-int")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-int-method-TASK"]
	assert.NotNil(t, taskNode)

	vmuts := taskNode.OutgoingEdges[0].VariableMutations
	assert.Greater(t, len(vmuts), 0)
	rhs := vmuts[0].GetRhsAssignment()
	assert.NotNil(t, rhs.GetTargetType())
	assert.Equal(t, lhproto.VariableType_INT, rhs.GetTargetType().GetPrimitiveType())
	assert.Equal(t, "$.int", rhs.GetJsonPath())
}
