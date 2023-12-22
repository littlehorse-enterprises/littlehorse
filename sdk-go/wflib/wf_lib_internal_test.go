package wflib_test

import (
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
	"github.com/stretchr/testify/assert"
)

func TestCanMakeSearcjableVariable(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		t.AddVariable(
			"my-var", model.VariableType_BOOL,
		).Searchable()
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	varDef := entrypoint.VariableDefs[0]
	assert.Equal(t, "my-var", varDef.VarDef.Name)
	assert.True(t, varDef.Searchable)
}

func TestUserTaskAssignToUser(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		t.AssignUserTask("my-task", "yoda", nil)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assert.NotNil(t, utNode.UserId)
	assert.Nil(t, utNode.UserGroup)
	assert.Equal(t, "yoda", utNode.UserId.GetLiteralValue().GetStr())
}

func TestUserTaskAssignToUserByVar(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		userVar := t.AddVariable("user", model.VariableType_STR)
		t.AssignUserTask("my-task", userVar, nil)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assert.NotNil(t, utNode.UserId)
	assert.Nil(t, utNode.UserGroup)
	assert.Equal(t, "user", utNode.UserId.GetVariableName())
}

func TestUserTaskAssignToUserWithGroup(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		t.AssignUserTask("my-task", "yoda", "jedi-council")
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assert.NotNil(t, utNode.UserId)
	assert.NotNil(t, utNode.UserGroup)
	assert.Equal(t, "yoda", utNode.UserId.GetLiteralValue().GetStr())
	assert.Equal(t, "jedi-council", utNode.UserGroup.GetLiteralValue().GetStr())
}

func TestAssignToGroup(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		t.AssignUserTask("my-task", nil, "jedi-council")
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assignment := utNode.GetUserGroup()
	assert.NotNil(t, assignment)
	assert.Equal(t, "jedi-council", assignment.GetLiteralValue().GetStr())
}

func TestReleaseToGroup(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		ut := t.AssignUserTask("my-task", "yoda", "jedi-council")
		t.ReleaseToGroupOnDeadline(ut, 10)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	action := utNode.Actions[0]
	reassign := action.GetReassign()
	assert.NotNil(t, reassign)
	assert.NotNil(t, reassign.GetUserGroup())

	group := reassign.GetUserGroup().GetLiteralValue().GetStr()
	assert.Equal(t, "jedi-council", group)
}

func TestReassignToGroup(t *testing.T) {
	group := "jedi-council"
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		ut := t.AssignUserTask("my-task", "yoda", nil)
		t.ReassignUserTaskOnDeadline(ut, nil, &group, 10)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	action := utNode.Actions[0]
	reassign := action.GetReassign()
	assert.NotNil(t, reassign)
	assert.NotNil(t, reassign.GetUserGroup())
	assert.Nil(t, reassign.GetUserId())

	assert.Equal(t, group, reassign.GetUserGroup().GetLiteralValue().GetStr())
}

func TestParallelSpawnThreads(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		myArr := t.AddVariable("my-arr", model.VariableType_JSON_ARR)

		spawnedThreads := t.SpawnThreadForEach(
			myArr,
			"some-threads",
			func(t *wflib.WorkflowThread) {},
			nil,
		)

		t.WaitForThreadsList(spawnedThreads)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	assert.Equal(t, len(putWf.ThreadSpecs), 2)

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	spawnNode := entrypoint.Nodes["1-some-threads-START_MULTIPLE_THREADS"]
	assert.Equal(t, len(spawnNode.OutgoingEdges[0].VariableMutations), 1)
	assert.NotNil(t, spawnNode.OutgoingEdges[0].VariableMutations[0].GetNodeOutput())

	_, ok := putWf.ThreadSpecs[spawnNode.GetStartMultipleThreads().ThreadSpecName]
	assert.True(t, ok)

	internalVarName := spawnNode.OutgoingEdges[0].VariableMutations[0].LhsName

	waitNode := entrypoint.Nodes["2-threads-WAIT_FOR_THREADS"].GetWaitForThreads()
	assert.Equal(t, waitNode.GetThreadList().GetVariableName(), internalVarName)
}

func TestParallelSpawnThreadsWithInput(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		myArr := t.AddVariable("my-arr", model.VariableType_JSON_ARR)

		inputs := map[string]interface{}{
			"asdf": 1234,
		}

		spawnedThreads := t.SpawnThreadForEach(
			myArr,
			"some-threads",
			func(t *wflib.WorkflowThread) {},
			&inputs,
		)

		t.WaitForThreadsList(spawnedThreads)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	spawnNode := entrypoint.Nodes["1-some-threads-START_MULTIPLE_THREADS"].GetStartMultipleThreads()
	assert.Equal(t, len(spawnNode.Variables), 1)

	assert.Equal(t, int64(1234), spawnNode.Variables["asdf"].GetLiteralValue().GetInt())
}

func TestFormatString(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		myArr := t.AddVariable("my-str", model.VariableType_STR)
		t.Execute("some-task", t.Format("input {0}", myArr))
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	spawnNode := entrypoint.Nodes["1-some-task-TASK"].GetTask()

	formatAssn := spawnNode.Variables[0].GetFormatString()

	assert.NotNil(t, formatAssn)
	assert.Equal(t, formatAssn.Format.GetLiteralValue().GetStr(), "input {0}")
	assert.Equal(t, len(formatAssn.GetArgs()), 1)
	assert.Equal(t, formatAssn.Args[0].GetVariableName(), "my-str")
}

// unimportant.
func someHandler(t *wflib.WorkflowThread) {}

func TestCatchSpecificException(t *testing.T) {
	exnName := "my-exn"
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		nodeOutput := t.Execute("some-task")
		t.HandleException(&nodeOutput, &exnName, someHandler)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]

	assert.Equal(t, 1, len(node.FailureHandlers))
	handler := node.FailureHandlers[0]

	_, ok := putWf.ThreadSpecs[handler.HandlerSpecName]
	assert.True(t, ok)
	assert.Equal(t, "my-exn", handler.GetSpecificFailure())
}

func TestCatchSpecificError(t *testing.T) {
	errorName := wflib.ChildFailure
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		nodeOutput := t.Execute("some-task")
		t.HandleError(&nodeOutput, &errorName, someHandler)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]

	assert.Equal(t, 1, len(node.FailureHandlers))
	handler := node.FailureHandlers[0]

	_, ok := putWf.ThreadSpecs[handler.HandlerSpecName]
	assert.True(t, ok)
	assert.Equal(t, string(errorName), handler.GetSpecificFailure())
}

func TestCatchAnyError(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		nodeOutput := t.Execute("some-task")
		t.HandleError(&nodeOutput, nil, someHandler)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]

	assert.Equal(t, 1, len(node.FailureHandlers))
	handler := node.FailureHandlers[0]

	_, ok := putWf.ThreadSpecs[handler.HandlerSpecName]
	assert.True(t, ok)
	assert.Equal(
		t,
		model.FailureHandlerDef_FAILURE_TYPE_ERROR,
		handler.GetAnyFailureOfType(),
	)
}

func TestCatchAnyException(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		nodeOutput := t.Execute("some-task")
		t.HandleException(&nodeOutput, nil, someHandler)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]

	assert.Equal(t, 1, len(node.FailureHandlers))
	handler := node.FailureHandlers[0]

	_, ok := putWf.ThreadSpecs[handler.HandlerSpecName]
	assert.True(t, ok)
	assert.Equal(
		t,
		model.FailureHandlerDef_FAILURE_TYPE_EXCEPTION,
		handler.GetAnyFailureOfType(),
	)
}

func TestCatchAnyFailure(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.WorkflowThread) {
		nodeOutput := t.Execute("some-task")
		t.HandleAnyFailure(&nodeOutput, someHandler)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]

	assert.Equal(t, 1, len(node.FailureHandlers))
	handler := node.FailureHandlers[0]

	_, ok := putWf.ThreadSpecs[handler.HandlerSpecName]
	assert.True(t, ok)
	assert.Nil(t, handler.GetFailureToCatch())
}
