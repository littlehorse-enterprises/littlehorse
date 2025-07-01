package littlehorse_test

import (
	"strings"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"google.golang.org/protobuf/proto"

	"github.com/stretchr/testify/assert"
)

func TestEarlyReturnOnExitNode(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.Execute("some-task")
		t.Fail("failure message", "my-failure", nil)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]

	exitNodeCount := 0

	for _, value := range entrypoint.Nodes {
		if value.GetExit() != nil {
			exitNodeCount += 1
		}
	}

	assert.Equal(t, exitNodeCount, 1)
}

func TestCanMakeSearchableVariable(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.AddVariable(
			"my-var", lhproto.VariableType_BOOL,
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
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
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
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		userVar := t.AddVariable("user", lhproto.VariableType_STR)
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

func TestUserTaskWithNotes(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.AssignUserTask("sample-user-task", nil, "group").WithNotes("sample notes")
	}, "my-workflow")

	putWf, _ := wf.Compile()
	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-sample-user-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assert.Equal(t, "sample notes", utNode.GetNotes().GetLiteralValue().GetStr())
}

func TestUserTaskWithOnCancellationExceptionName(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.AssignUserTask("sample-user-task", nil, "group").WithOnCancellationException("no-response")
	}, "my-workflow")

	putWf, _ := wf.Compile()
	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-sample-user-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assert.Equal(t, "no-response", utNode.GetOnCancellationExceptionName().GetLiteralValue().GetStr())
}

func TestReminderTaskArgs(t *testing.T) {
	argForReminderTask := "some-arg-that-is-string"
	wfObj := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		uto := t.AssignUserTask("some-user-task", "some-user", "some-group")

		t.ScheduleReminderTask(uto, 20, "some-task", argForReminderTask)
	}, "somem-workflow")

	putWf, err := wfObj.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-user-task-USER_TASK"]
	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)
	reminderAction := utNode.Actions[0]
	assert.Equal(t, argForReminderTask, reminderAction.GetTask().Task.Variables[0].GetLiteralValue().GetStr())
}

func TestReminderTask(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		userVar := t.AddVariable("user", lhproto.VariableType_STR)
		uto := t.AssignUserTask("my-task", userVar, nil)
		t.ScheduleReminderTaskOnAssignment(uto, 20, "my-task", "my-arg")
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)
	reminderAction := utNode.Actions[0]
	assert.NotNil(t, reminderAction)
	assert.Equal(t, lhproto.UTActionTrigger_ON_TASK_ASSIGNED, reminderAction.Hook)

}

func TestCancelUserTaskAfterDeadline(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		userVar := t.AddVariable("user", lhproto.VariableType_STR)
		uto := t.AssignUserTask("my-task", userVar, nil)
		t.CancelUserTaskAfter(uto, 20)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)
	cancelUserTask := utNode.Actions[0]
	assert.NotNil(t, cancelUserTask)
	assert.Equal(t, lhproto.UTActionTrigger_ON_ARRIVAL, cancelUserTask.Hook)
}

func TestCancelUserTaskAfterAssignment(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		userVar := t.AddVariable("user", lhproto.VariableType_STR)
		uto := t.AssignUserTask("my-task", userVar, nil)
		t.CancelUserTaskAfterAssignment(uto, 20)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)
	cancelUserTask := utNode.Actions[0]
	assert.NotNil(t, cancelUserTask)
	assert.Equal(t, lhproto.UTActionTrigger_ON_TASK_ASSIGNED, cancelUserTask.Hook)
}

func TestUserTaskAssignToUserWithGroup(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
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
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
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
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
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
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
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
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myArr := t.AddVariable("my-arr", lhproto.VariableType_JSON_ARR)

		spawnedThreads := t.SpawnThreadForEach(
			myArr,
			"some-threads",
			func(t *littlehorse.WorkflowThread) {},
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
	assert.NotNil(t, spawnNode.OutgoingEdges[0].VariableMutations[0].GetRhsAssignment())

	_, ok := putWf.ThreadSpecs[spawnNode.GetStartMultipleThreads().ThreadSpecName]
	assert.True(t, ok)

	internalVarName := spawnNode.OutgoingEdges[0].VariableMutations[0].LhsName

	waitNode := entrypoint.Nodes["2-threads-WAIT_FOR_THREADS"].GetWaitForThreads()
	assert.Equal(t, waitNode.GetThreadList().GetVariableName(), internalVarName)
}

func TestAssignNodeOutput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.Execute("task-two", t.Execute("task-one"))
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["2-task-two-TASK"]
	assert.Equal(t, taskNode.GetTask().Variables[0].GetNodeOutput().NodeName, "1-task-one-TASK")
}

func TestMutationsShouldUseVariableAssignment(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareStr("my-var")
		myVar.Assign("some-value")
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["0-entrypoint-ENTRYPOINT"]
	assert.Equal(t, taskNode.OutgoingEdges[0].VariableMutations[0].GetRhsAssignment().GetLiteralValue().GetStr(), "some-value")
}

func TestNodeOutputMutationsShouldUseVariableAssignment(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareStr("my-var")
		myVar.Assign(t.Execute("use-the-force"))
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-use-the-force-TASK"]
	assert.Equal(t, taskNode.OutgoingEdges[0].VariableMutations[0].GetRhsAssignment().GetNodeOutput().NodeName, "1-use-the-force-TASK")
}

func TestNodeOutputMutationsShouldCarryJsonPath(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareStr("my-var")
		myVar.Assign(t.Execute("use-the-force").JsonPath("$.hello.there"))
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-use-the-force-TASK"]
	assert.Equal(t, taskNode.OutgoingEdges[0].VariableMutations[0].GetRhsAssignment().GetJsonPath(), "$.hello.there")
}

func TestAssigningVariablesToOtherVariablesShouldUseVariableAssignment(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareStr("my-var")
		otherVar := t.DeclareStr("other-var")
		myVar.Assign(otherVar)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["0-entrypoint-ENTRYPOINT"]
	assert.Equal(t, taskNode.OutgoingEdges[0].VariableMutations[0].GetRhsAssignment().GetVariableName(), "other-var")
}

func TestAssigningVariablesToOtherVariablesShouldCarryJsonPath(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.DeclareStr("my-var")
		otherVar := t.DeclareJsonObj("other-var")
		myVar.Assign(otherVar.JsonPath("$.hello.there"))
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["0-entrypoint-ENTRYPOINT"]

	assert.Equal(t, taskNode.OutgoingEdges[0].VariableMutations[0].GetRhsAssignment().GetVariableName(), "other-var")
	assert.Equal(t, taskNode.OutgoingEdges[0].VariableMutations[0].GetRhsAssignment().GetJsonPath(), "$.hello.there")
}

func TestAddRetriesToTaskNodeOutput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.Execute("task-one").WithRetries(5)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-task-one-TASK"]
	assert.Equal(t, taskNode.GetTask().Retries, int32(5))
}

func TestExponentialBackoffRetryPolicyToTaskNodeOutput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.Execute("task-one").WithExponentialBackoff(&lhproto.ExponentialBackoffRetryPolicy{
			BaseIntervalMs: 500,
			MaxDelayMs:     2000,
			Multiplier:     2,
		})
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	taskNode := entrypoint.Nodes["1-task-one-TASK"]
	assert.Equal(t, taskNode.GetTask().ExponentialBackoff.BaseIntervalMs, int32(500))
	assert.Equal(t, taskNode.GetTask().ExponentialBackoff.MaxDelayMs, int64(2000))
	assert.Equal(t, taskNode.GetTask().ExponentialBackoff.Multiplier, float32(2))
}

func TestParallelSpawnThreadsWithInput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myArr := t.AddVariable("my-arr", lhproto.VariableType_JSON_ARR)

		inputs := map[string]interface{}{
			"asdf": 1234,
		}

		spawnedThreads := t.SpawnThreadForEach(
			myArr,
			"some-threads",
			func(t *littlehorse.WorkflowThread) {},
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
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myArr := t.AddVariable("my-str", lhproto.VariableType_STR)
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
func someHandler(t *littlehorse.WorkflowThread) {}

func TestCatchSpecificException(t *testing.T) {
	exnName := "my-exn"
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		taskNodeOutput := t.Execute("some-task")
		t.HandleException(taskNodeOutput, &exnName, someHandler)
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
	errorName := littlehorse.ChildFailure
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		taskNodeOutput := t.Execute("some-task")
		t.HandleError(taskNodeOutput, &errorName, someHandler)
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
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		taskNodeOutput := t.Execute("some-task")
		t.HandleError(taskNodeOutput, nil, someHandler)
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
		lhproto.FailureHandlerDef_FAILURE_TYPE_ERROR,
		handler.GetAnyFailureOfType(),
	)
}

func TestCatchAnyException(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		taskNodeOutput := t.Execute("some-task")
		t.HandleException(taskNodeOutput, nil, someHandler)
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
		lhproto.FailureHandlerDef_FAILURE_TYPE_EXCEPTION,
		handler.GetAnyFailureOfType(),
	)
}

func TestCatchAnyFailure(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		taskNodeOutput := t.Execute("some-task")
		t.HandleAnyFailure(taskNodeOutput, someHandler)
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

type someObject struct {
	Foo int32
	Bar string
}

func TestVarValToVarType(t *testing.T) {
	// Int
	varVal, err := littlehorse.InterfaceToVarVal(123)
	assert.Nil(t, err)
	varType := littlehorse.VarValToVarType(varVal)
	assert.Equal(t, *varType, lhproto.VariableType_INT)

	// Str
	varVal, err = littlehorse.InterfaceToVarVal("hello there")
	assert.Nil(t, err)
	varType = littlehorse.VarValToVarType(varVal)
	assert.Equal(t, *varType, lhproto.VariableType_STR)

	// Str pointer
	mystr := "hello there"
	varVal, err = littlehorse.InterfaceToVarVal(&mystr)
	assert.Nil(t, err)
	varType = littlehorse.VarValToVarType(varVal)
	assert.Equal(t, *varType, lhproto.VariableType_STR)
	assert.Equal(t, varVal.GetStr(), mystr)

	// struct/JSON_OBJ
	varVal, err = littlehorse.InterfaceToVarVal(someObject{
		Foo: 137,
		Bar: "meaningoflife",
	})
	assert.Nil(t, err)
	varType = littlehorse.VarValToVarType(varVal)
	assert.Equal(t, *varType, lhproto.VariableType_JSON_OBJ)

	// Nil varval
	varVal = &lhproto.VariableValue{}
	varType = littlehorse.VarValToVarType(varVal)
	assert.Nil(t, varType)
}

func TestUpdateType(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		taskNodeOutput := t.Execute("some-task")
		t.HandleAnyFailure(taskNodeOutput, someHandler)
	}, "my-workflow").WithUpdateType(lhproto.AllowedUpdateType_NO_UPDATES)

	putWf, _ := wf.Compile()

	assert.Equal(t, putWf.AllowedUpdates, lhproto.AllowedUpdateType_NO_UPDATES)
}

func TestJsonPath(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		myVar := t.AddVariable("my-var", lhproto.VariableType_JSON_OBJ)
		t.Execute("some-task", myVar.JsonPath("$.foo"))
	}, "my-workflow").WithUpdateType(lhproto.AllowedUpdateType_NO_UPDATES)

	putWf, _ := wf.Compile()
	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]
	assert.Equal(t, *(node.GetTask().Variables[0].JsonPath), "$.foo")
}

func TestVariableAccessLevel(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		publicVar := t.AddVariable("my-var", lhproto.VariableType_BOOL)
		publicVar.AsPublic()

		// Test that default is PRIVATE_VAR
		t.AddVariable("default-access", lhproto.VariableType_INT)

		t.Execute("some-task")
	}, "my-workflow").WithUpdateType(lhproto.AllowedUpdateType_NO_UPDATES)

	putWf, _ := wf.Compile()
	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	varDef := entrypoint.VariableDefs[0]
	assert.Equal(t, varDef.AccessLevel, lhproto.WfRunVariableAccessLevel_PUBLIC_VAR)
	assert.Equal(t, varDef.VarDef.Name, "my-var")

	varDef = entrypoint.VariableDefs[1]
	assert.Equal(t, varDef.AccessLevel, lhproto.WfRunVariableAccessLevel_PRIVATE_VAR)
	assert.Equal(t, varDef.VarDef.Name, "default-access")
}

func TestRetentionPolicy(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {

		t.WithRetentionPolicy(&lhproto.ThreadRetentionPolicy{
			ThreadGcPolicy: &lhproto.ThreadRetentionPolicy_SecondsAfterThreadTermination{
				SecondsAfterThreadTermination: 137,
			},
		})

		t.Execute("some-task")
	}, "my-workflow").WithRetentionPolicy(&lhproto.WorkflowRetentionPolicy{
		WfGcPolicy: &lhproto.WorkflowRetentionPolicy_SecondsAfterWfTermination{
			SecondsAfterWfTermination: 10,
		},
	})

	putWf, _ := wf.Compile()
	assert.Equal(t, int(putWf.RetentionPolicy.GetSecondsAfterWfTermination()), int(10))

	thread := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	assert.Equal(t, int(thread.RetentionPolicy.GetSecondsAfterThreadTermination()), int(137))
}

func TestThrowEvent(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(wf *littlehorse.WorkflowThread) {
		myVar := wf.AddVariable("my-var", lhproto.VariableType_STR)
		wf.ThrowEvent("my-event", myVar)
		wf.ThrowEvent("another-event", "my-content")
	}, "throw-event")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-throw-my-event-THROW_EVENT"]
	assert.Equal(t, node.GetThrowEvent().Content.GetVariableName(), "my-var")
	assert.Equal(t, node.GetThrowEvent().EventDefId.Name, "my-event")

	node = entrypoint.Nodes["2-throw-another-event-THROW_EVENT"]
	assert.Equal(t, node.GetThrowEvent().Content.GetLiteralValue().GetStr(), "my-content")
	assert.Equal(t, node.GetThrowEvent().EventDefId.Name, "another-event")
}

func TestExternalEventCorrelaation(t *testing.T) {
	putWf, _ := littlehorse.NewWorkflow(func(wf *littlehorse.WorkflowThread) {
		myVar := wf.DeclareStr("my-var")
		wf.WaitForEvent("some-event").SetCorrelationId(myVar)
	}, "obiwan").Compile()

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-event-EXTERNAL_EVENT"]
	assert.Equal(
		t,
		node.GetExternalEvent().CorrelationKey.GetVariableName(),
		"my-var",
	)
}

func TestExceptionHandlerOnTaskOutput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(wf *littlehorse.WorkflowThread) {
		taskNodeOutput := wf.Execute("some-task")
		exnName := "my-exception"
		wf.HandleException(taskNodeOutput, &exnName, func(wf *littlehorse.WorkflowThread) {
			wf.Execute("some-other-task")
		})
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]

	assert.Equal(t, 1, len(node.FailureHandlers))
	handler := node.FailureHandlers[0]

	assert.Equal(t, "my-exception", handler.GetSpecificFailure())
	assert.Equal(t, "exn-handler-my-exception-1-some-task-TASK", handler.HandlerSpecName)
}

func TestExceptionHandlerOnExternalEvent(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(wf *littlehorse.WorkflowThread) {
		eventNodeOutput := wf.WaitForEvent("some-event")
		exnName := "my-exception"
		wf.HandleException(eventNodeOutput, &exnName, func(wf *littlehorse.WorkflowThread) {
			wf.Execute("some-other-task")
		})
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-event-EXTERNAL_EVENT"]

	assert.Equal(t, 1, len(node.FailureHandlers))
	handler := node.FailureHandlers[0]

	assert.Equal(t, "my-exception", handler.GetSpecificFailure())
	assert.Equal(t, "exn-handler-my-exception-1-some-event-EXTERNAL_EVENT", handler.HandlerSpecName)
}

func TestAddOnEventNodeOutput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(wf *littlehorse.WorkflowThread) {
		myVar := wf.DeclareInt("my-var")
		eventNodeOutput := wf.WaitForEvent("some-event")
		myVar.Assign(eventNodeOutput.Add(3))
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-event-EXTERNAL_EVENT"]

	assert.Equal(t, 1, len(node.OutgoingEdges[0].VariableMutations))
	mutation := node.OutgoingEdges[0].VariableMutations[0]
	assert.Equal(t, mutation.LhsName, "my-var")
	assert.NotNil(t, mutation.GetRhsAssignment())

	expr := mutation.GetRhsAssignment().GetExpression()
	assert.NotNil(t, expr)
	assert.Equal(t, expr.GetLhs().GetNodeOutput().NodeName, "1-some-event-EXTERNAL_EVENT")
	assert.Equal(t, expr.GetRhs().GetLiteralValue().GetInt(), int64(3))
}

func TestAddOnTaskOutput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(wf *littlehorse.WorkflowThread) {
		myVar := wf.DeclareInt("my-var")
		taskNodeOutput := wf.Execute("some-task")
		myVar.Assign(taskNodeOutput.Add(3))
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]

	assert.Equal(t, 1, len(node.OutgoingEdges[0].VariableMutations))
	mutation := node.OutgoingEdges[0].VariableMutations[0]
	assert.Equal(t, mutation.LhsName, "my-var")
	assert.NotNil(t, mutation.GetRhsAssignment())

	expr := mutation.GetRhsAssignment().GetExpression()
	assert.NotNil(t, expr)
	assert.Equal(t, expr.GetLhs().GetNodeOutput().NodeName, "1-some-task-TASK")
	assert.Equal(t, expr.GetRhs().GetLiteralValue().GetInt(), int64(3))
}

func TestNestedExpressions(t *testing.T) {
	// price.times(quantity).multiply(discount)
	wf := littlehorse.NewWorkflow(func(wf *littlehorse.WorkflowThread) {
		price := wf.DeclareInt("price")
		quantity := wf.DeclareInt("quantity")
		discount := wf.DeclareDouble("discount")

		total := price.Multiply(quantity).Divide(discount)
		wf.Execute("some-task", total)
	}, "my-workflow")

	putWf, err := wf.Compile()

	littlehorse.PrintProto(putWf)

	if err != nil {
		t.Error(err)
	}
	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-some-task-TASK"]
	assert.Equal(t, 1, len(node.GetTask().Variables))
	varDef := node.GetTask().Variables[0]

	outerExpression := varDef.GetExpression()

	lhsExpression := outerExpression.GetLhs().GetExpression()
	assert.NotNil(t, lhsExpression)
	assert.Equal(t, lhsExpression.GetLhs().GetVariableName(), "price")
	assert.Equal(t, lhsExpression.GetRhs().GetVariableName(), "quantity")
	assert.Equal(t, lhsExpression.GetOperation(), lhproto.VariableMutationType_MULTIPLY)

	outerRhs := outerExpression.GetRhs()
	assert.NotNil(t, outerRhs)
	assert.Equal(t, outerRhs.GetVariableName(), "discount")
	assert.Equal(t, outerExpression.GetOperation(), lhproto.VariableMutationType_DIVIDE)
}

func TestDynamicTask(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(wf *littlehorse.WorkflowThread) {
		myVar := wf.AddVariable("my-var", lhproto.VariableType_STR)
		wf.Execute("some-static-task")

		formatStr := wf.Format("some-dynamic-task-{0}")
		wf.Execute(formatStr, myVar)
		wf.Execute(myVar)
	}, "obiwan")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	staticNode := entrypoint.Nodes["1-some-static-task-TASK"]
	assert.Equal(t, staticNode.GetTask().GetTaskDefId().Name, "some-static-task")

	formatStrNode := entrypoint.Nodes["2-some-dynamic-task-{0}-TASK"]
	assert.Equal(
		t,
		formatStrNode.GetTask().GetDynamicTask().GetFormatString().GetFormat().GetLiteralValue().GetStr(),
		"some-dynamic-task-{0}",
	)

	varNode := entrypoint.Nodes["3-my-var-TASK"]
	assert.Equal(t, varNode.GetTask().GetDynamicTask().GetVariableName(), "my-var")
}

func TestWaitForThreadsHandleExceptionOnChild(t *testing.T) {
	failureHandler := func(wf *littlehorse.WorkflowThread) {
		wf.Execute("some-task")
	}

	childThread := func(wf *littlehorse.WorkflowThread) {
		wf.Execute("some-task")
	}

	wfFunc := func(t *littlehorse.WorkflowThread) {
		child := t.SpawnThread(childThread, "child", nil)
		result := t.WaitForThreads(child)

		exceptionName := "my-exception"
		result.HandleExceptionOnChild(failureHandler, &exceptionName)
		result.HandleExceptionOnChild(failureHandler, nil)
	}

	wf, err := littlehorse.NewWorkflow(wfFunc, "some-wf").Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := wf.ThreadSpecs[wf.EntrypointThreadName]
	node := entrypoint.Nodes["2-threads-WAIT_FOR_THREADS"]

	wftn := node.GetWaitForThreads()
	assert.Equal(t, 2, len(wftn.PerThreadFailureHandlers))

	specificHandler := wftn.PerThreadFailureHandlers[0]
	anyHandler := wftn.PerThreadFailureHandlers[1]

	assert.Equal(t, "my-exception", specificHandler.GetSpecificFailure())
	assert.Equal(t, "exn-handler-2-threads-WAIT_FOR_THREADS-my-exception", specificHandler.HandlerSpecName)

	assert.Equal(t, lhproto.FailureHandlerDef_FAILURE_TYPE_EXCEPTION, anyHandler.GetAnyFailureOfType())
	assert.Equal(t, "exn-handler-2-threads-WAIT_FOR_THREADS", anyHandler.HandlerSpecName)
}

func TestWaitForThreadsHandleAnyFailureOnChild(t *testing.T) {
	failureHandler := func(wf *littlehorse.WorkflowThread) {
		wf.Execute("some-task")
	}

	childThread := func(wf *littlehorse.WorkflowThread) {
		wf.Execute("another-task")
	}

	wfFunc := func(t *littlehorse.WorkflowThread) {
		child := t.SpawnThread(childThread, "child", nil)
		result := t.WaitForThreads(child)

		result.HandleAnyFailureOnChild(failureHandler)
	}

	wf, err := littlehorse.NewWorkflow(wfFunc, "some-wf").Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := wf.ThreadSpecs[wf.EntrypointThreadName]
	node := entrypoint.Nodes["2-threads-WAIT_FOR_THREADS"]
	wftn := node.GetWaitForThreads()

	assert.Equal(t, 1, len(wftn.PerThreadFailureHandlers))

	anyFailureHandler := wftn.PerThreadFailureHandlers[0]

	assert.Equal(t, "failure-handler-2-threads-WAIT_FOR_THREADS-ANY_FAILURE", anyFailureHandler.HandlerSpecName)
}

func TestWaitForThreadsHandleErrorOnChild(t *testing.T) {
	errorHandler := func(wf *littlehorse.WorkflowThread) {
		wf.Execute("some-task")
	}

	childThread := func(wf *littlehorse.WorkflowThread) {
		wf.Execute("some-task")
	}

	wfFunc := func(t *littlehorse.WorkflowThread) {
		child := t.SpawnThread(childThread, "child", nil)
		result := t.WaitForThreads(child)

		timeout := "TIMEOUT"
		result.HandleErrorOnChild(errorHandler, &timeout)
		result.HandleErrorOnChild(errorHandler, nil)
	}

	wf, err := littlehorse.NewWorkflow(wfFunc, "some-wf").Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := wf.ThreadSpecs[wf.EntrypointThreadName]
	node := entrypoint.Nodes["2-threads-WAIT_FOR_THREADS"]
	wftn := node.GetWaitForThreads()

	assert.Equal(t, 4, len(wf.ThreadSpecs))

	assert.Equal(t, 2, len(wftn.PerThreadFailureHandlers))

	timeoutHandler := wftn.PerThreadFailureHandlers[0]
	anyErrorHandler := wftn.PerThreadFailureHandlers[1]

	assert.Equal(t, "TIMEOUT", timeoutHandler.GetSpecificFailure())
	assert.Equal(t, "error-handler-2-threads-WAIT_FOR_THREADS-TIMEOUT", timeoutHandler.HandlerSpecName)

	assert.Equal(t, lhproto.FailureHandlerDef_FAILURE_TYPE_ERROR, anyErrorHandler.GetAnyFailureOfType())
	assert.Equal(t, "error-handler-2-threads-WAIT_FOR_THREADS", anyErrorHandler.HandlerSpecName)
}

func TestShouldAssignAParentVarFromChildNestedThreadsWhenWorkflowIsCompiled(t *testing.T) {
	expectedParentVarName := "grand-parent-var"
	expectedSonThreadVarValue := "son-value"
	expectedGrandChildThreadVarValue := "grandchild-value"

	wf := littlehorse.NewWorkflow(func(grandParentThread *littlehorse.WorkflowThread) {
		grandParentVar := grandParentThread.DeclareStr(expectedParentVarName)
		grandParentThread.SpawnThread(
			func(sonThread *littlehorse.WorkflowThread) {
				grandParentVar.Assign(expectedSonThreadVarValue)
				sonThread.SpawnThread(
					func(grandChildThread *littlehorse.WorkflowThread) {
						grandParentVar.Assign(expectedGrandChildThreadVarValue)
					},
					"grandchild-thread",
					nil,
				)
			},
			"son-thread",
			nil,
		)
	}, "my-workflow")
	compiledWorkflow, error := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	sonThread := compiledWorkflow.ThreadSpecs["son-thread"]
	grandChildThread := compiledWorkflow.ThreadSpecs["grandchild-thread"]

	assert.Nil(t, error)
	assert.NotNil(t, compiledWorkflow)
	assert.Equal(t, 3, len(entrypoint.GetNodes()))
	assert.Equal(t, 3, len(sonThread.GetNodes()))
	assert.Equal(t, 2, len(grandChildThread.GetNodes()))
	assert.Equal(t, expectedParentVarName, entrypoint.GetVariableDefs()[0].VarDef.Name)
	assert.Equal(t, expectedParentVarName, sonThread.Nodes["0-entrypoint-ENTRYPOINT"].GetOutgoingEdges()[0].VariableMutations[0].LhsName)
	assert.Equal(t, expectedSonThreadVarValue, sonThread.Nodes["0-entrypoint-ENTRYPOINT"].GetOutgoingEdges()[0].VariableMutations[0].GetRhsAssignment().GetLiteralValue().GetStr())
	assert.Equal(t, expectedParentVarName, grandChildThread.Nodes["0-entrypoint-ENTRYPOINT"].GetOutgoingEdges()[0].VariableMutations[0].LhsName)
	assert.Equal(t, expectedGrandChildThreadVarValue, grandChildThread.Nodes["0-entrypoint-ENTRYPOINT"].GetOutgoingEdges()[0].VariableMutations[0].GetRhsAssignment().GetLiteralValue().GetStr())
}

func TestShouldValidateUserIdIsNotEmptyInUserTask(t *testing.T) {
	defer func() {
		if r := recover(); r != nil {
			err, ok := r.(error)
			if !ok {
				t.Fatalf("expected error, got: %v", r)
			}
			if !strings.Contains(err.Error(), "userId can't be blank when assigning usertask") {
				t.Errorf("expected error about blank userId, got: %v", err)
			}
		} else {
			t.Errorf("expected panic, but function completed normally")
		}
	}()

	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.AssignUserTask("my-task", "  ", nil)
	}, "my-workflow")

	wf.Compile()
}

func TestShouldValidateGroupIdIsNotEmptyInUserTask(t *testing.T) {
	defer func() {
		if r := recover(); r != nil {
			err, ok := r.(error)
			if !ok {
				t.Fatalf("expected error, got: %v", r)
			}
			if !strings.Contains(err.Error(), "userGroup can't be blank when assigning usertask") {
				t.Errorf("expected error about blank userGroup, got: %v", err)
			}
		} else {
			t.Errorf("expected panic, but function completed normally")
		}
	}()

	wf := littlehorse.NewWorkflow(func(t *littlehorse.WorkflowThread) {
		t.AssignUserTask("my-task", nil, "  ")
	}, "my-workflow")

	wf.Compile()
}

func TestShouldCompileWorkflowWithDefaultEdgeWhenDoElseIfIsNotUsed(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.DoIf(thread.Condition(5, lhproto.Comparator_GREATER_THAN_EQ, 9), func(t *littlehorse.WorkflowThread) {
		})
	}, "my-workflow")

	compiledWorkflow, error := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	firstNopNode := entrypoint.Nodes["1-nop-NOP"]

	assert.Nil(t, error)
	assert.True(t, proto.Equal(firstNopNode, &lhproto.Node{
		Node: &lhproto.Node_Nop{},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-nop-NOP",
				Condition: &lhproto.EdgeCondition{
					Comparator: lhproto.Comparator_GREATER_THAN_EQ,
					Left: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 5},
							},
						},
					},
					Right: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 9},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "2-nop-NOP",
			},
		},
	}))
}

func TestShouldCompileWorkflowWithMultipleDoElseIfStatementsInWorkflowThread(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myInt := thread.DeclareInt("my-int")
		thread.DoIf(thread.Condition(5, lhproto.Comparator_GREATER_THAN_EQ, 9), func(t *littlehorse.WorkflowThread) {
			t.Execute("task-a")
			myInt.Assign(9)
		}).DoElseIf(thread.Condition(7, lhproto.Comparator_LESS_THAN, 4), func(t *littlehorse.WorkflowThread) {
			myInt.Assign(10)
			t.Execute("task-b")
		}).DoElseIf(thread.Condition(5, lhproto.Comparator_EQUALS, 5), func(t *littlehorse.WorkflowThread) {
			t.Execute("task-c")
		})
		myInt.Assign(0)
		thread.Execute("task-d")
	}, "my-workflow")

	compiledWorkflow, error := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	firstNopNode := entrypoint.Nodes["1-nop-NOP"]
	taskNodeA := entrypoint.Nodes["2-task-a-TASK"]
	taskNodeB := entrypoint.Nodes["4-task-b-TASK"]
	taskNodeC := entrypoint.Nodes["5-task-c-TASK"]
	taskNodeD := entrypoint.Nodes["6-task-d-TASK"]
	lastNopNode := entrypoint.Nodes["3-nop-NOP"]

	expectedNumberOutgoingEdgesFromFirstNopNode := 4
	expectedLastSinkNopNodeName := "3-nop-NOP"
	expectedExitSinkNodeName := "7-exit-EXIT"
	expectedTaskDSinkNodeName := "6-task-d-TASK"
	expectedFirstNopeNode := lhproto.Node{
		Node: &lhproto.Node_Nop{},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-task-a-TASK",
				Condition: &lhproto.EdgeCondition{
					Comparator: lhproto.Comparator_GREATER_THAN_EQ,
					Left: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 5},
							},
						},
					},
					Right: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 9},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "4-task-b-TASK",
				Condition: &lhproto.EdgeCondition{
					Comparator: lhproto.Comparator_LESS_THAN,
					Left: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 7},
							},
						},
					},
					Right: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 4},
							},
						},
					},
				},
				VariableMutations: []*lhproto.VariableMutation{
					{
						LhsName: "my-int",
						RhsValue: &lhproto.VariableMutation_RhsAssignment{
							RhsAssignment: &lhproto.VariableAssignment{
								Source: &lhproto.VariableAssignment_LiteralValue{
									LiteralValue: &lhproto.VariableValue{
										Value: &lhproto.VariableValue_Int{Int: 10},
									},
								},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "5-task-c-TASK",
				Condition: &lhproto.EdgeCondition{
					Comparator: lhproto.Comparator_EQUALS,
					Left: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 5},
							},
						},
					},
					Right: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 5},
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
	assert.Nil(t, error)
	assert.Equal(t, expectedNumberOutgoingEdgesFromFirstNopNode, len(firstNopNode.GetOutgoingEdges()))
	assert.True(t, proto.Equal(firstNopNode, &expectedFirstNopeNode))
	assert.True(t, proto.Equal(lastNopNode.OutgoingEdges[0].VariableMutations[0],
		&lhproto.VariableMutation{
			LhsName: "my-int",
			RhsValue: &lhproto.VariableMutation_RhsAssignment{
				RhsAssignment: &lhproto.VariableAssignment{
					Source: &lhproto.VariableAssignment_LiteralValue{
						LiteralValue: &lhproto.VariableValue{
							Value: &lhproto.VariableValue_Int{Int: 0},
						},
					},
				},
			},
		}))
	assert.Equal(t, expectedNumberOutgoingEdgesFromFirstNopNode, len(firstNopNode.GetOutgoingEdges()))
	assert.Equal(t, expectedLastSinkNopNodeName, taskNodeA.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, expectedLastSinkNopNodeName, taskNodeB.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, expectedLastSinkNopNodeName, taskNodeC.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, expectedTaskDSinkNodeName, lastNopNode.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, expectedExitSinkNodeName, taskNodeD.GetOutgoingEdges()[0].GetSinkNodeName())
}

func TestShouldCompileWorkflowWithDoIfElseAndElseStatements(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		myInt := thread.DeclareInt("my-int")
		thread.DoIf(thread.Condition(5, lhproto.Comparator_GREATER_THAN_EQ, 9), func(t *littlehorse.WorkflowThread) {
			t.Execute("task-a")
			myInt.Assign(9)
		}).DoElseIf(thread.Condition(7, lhproto.Comparator_LESS_THAN, 4), func(t *littlehorse.WorkflowThread) {
			myInt.Assign(10)
			t.Execute("task-b")
		}).DoElse(func(t *littlehorse.WorkflowThread) {
			t.Execute("task-c")
		})
	}, "my-workflow")

	compiledWorkflow, error := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	firstNopNode := entrypoint.Nodes["1-nop-NOP"]
	taskNodeA := entrypoint.Nodes["2-task-a-TASK"]
	taskNodeB := entrypoint.Nodes["4-task-b-TASK"]
	taskNodeC := entrypoint.Nodes["5-task-c-TASK"]
	lastNopNode := entrypoint.Nodes["3-nop-NOP"]

	expectedNumberOutgoingEdgesFromFirstNopNode := 3
	expectedLastSinkNopNodeName := "3-nop-NOP"
	expectedExitSinkNodeName := "6-exit-EXIT"
	expectedFirstNopeNode := lhproto.Node{
		Node: &lhproto.Node_Nop{},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-task-a-TASK",
				Condition: &lhproto.EdgeCondition{
					Comparator: lhproto.Comparator_GREATER_THAN_EQ,
					Left: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 5},
							},
						},
					},
					Right: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 9},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "4-task-b-TASK",
				Condition: &lhproto.EdgeCondition{
					Comparator: lhproto.Comparator_LESS_THAN,
					Left: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 7},
							},
						},
					},
					Right: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 4},
							},
						},
					},
				},
				VariableMutations: []*lhproto.VariableMutation{
					{
						LhsName: "my-int",
						RhsValue: &lhproto.VariableMutation_RhsAssignment{
							RhsAssignment: &lhproto.VariableAssignment{
								Source: &lhproto.VariableAssignment_LiteralValue{
									LiteralValue: &lhproto.VariableValue{
										Value: &lhproto.VariableValue_Int{Int: 10},
									},
								},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "5-task-c-TASK",
			},
		},
	}

	assert.Nil(t, error)
	assert.Equal(t, expectedNumberOutgoingEdgesFromFirstNopNode, len(firstNopNode.GetOutgoingEdges()))
	assert.True(t, proto.Equal(firstNopNode, &expectedFirstNopeNode))
	assert.True(t, proto.Equal(taskNodeA.OutgoingEdges[0].VariableMutations[0],
		&lhproto.VariableMutation{
			LhsName: "my-int",
			RhsValue: &lhproto.VariableMutation_RhsAssignment{
				RhsAssignment: &lhproto.VariableAssignment{
					Source: &lhproto.VariableAssignment_LiteralValue{
						LiteralValue: &lhproto.VariableValue{
							Value: &lhproto.VariableValue_Int{Int: 9},
						},
					},
				},
			},
		}))
	assert.Equal(t, expectedNumberOutgoingEdgesFromFirstNopNode, len(firstNopNode.GetOutgoingEdges()))
	assert.Equal(t, expectedLastSinkNopNodeName, taskNodeA.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, expectedLastSinkNopNodeName, taskNodeB.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, expectedLastSinkNopNodeName, taskNodeC.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, expectedExitSinkNodeName, lastNopNode.GetOutgoingEdges()[0].GetSinkNodeName())
}

func TestShouldPanicAnErrorIfDoElseIsCalledMoreThanOnce(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		ifStatement := thread.DoIf(thread.Condition(5, lhproto.Comparator_GREATER_THAN_EQ, 9),
			func(t *littlehorse.WorkflowThread) {
				t.Execute("task-a")
			})
		ifStatement.DoElse(func(t *littlehorse.WorkflowThread) {
			t.Execute("task-b")
		})

		defer func() {
			if r := recover(); r != nil {
				err, ok := r.(error)
				if !ok {
					t.Fatalf("expected error, got: %v", r)
				}
				if !strings.Contains(err.Error(), "else block has already been executed") {
					t.Errorf("expected error about already executed else block, got: %v", err)
				}
			} else {
				t.Errorf("expected panic, but function completed normally")
			}
		}()

		ifStatement.DoElse(func(t *littlehorse.WorkflowThread) {
			t.Execute("task-c")
		})
	}, "my-workflow")

	_, err := wf.Compile()

	assert.Nil(t, err)
}

func TestShouldCompileWorkflowWhenDoElseIfIsCalledAfterATask(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		ifStatement := thread.DoIf(thread.Condition(5, lhproto.Comparator_GREATER_THAN_EQ, 9),
			func(t *littlehorse.WorkflowThread) {})
		thread.Execute("task-a")
		ifStatement.DoElseIf(thread.Condition(5, lhproto.Comparator_GREATER_THAN_EQ, 3), func(t *littlehorse.WorkflowThread) {
			t.Execute("task-b")
		})
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	firstNopNode := entrypoint.Nodes["1-nop-NOP"]
	lastNopNode := entrypoint.Nodes["2-nop-NOP"]
	taskNodeA := entrypoint.Nodes["3-task-a-TASK"]
	taskNodeB := entrypoint.Nodes["4-task-b-TASK"]

	expectedNumberOutgoingEdgesFromFirstNopNode := 3
	expectedLastSinkNopNodeName := "2-nop-NOP"
	expectedExitSinkNodeName := "5-exit-EXIT"

	expectedFirstNopeNode := lhproto.Node{
		Node: &lhproto.Node_Nop{},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-nop-NOP",
				Condition: &lhproto.EdgeCondition{
					Comparator: lhproto.Comparator_GREATER_THAN_EQ,
					Left: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 5},
							},
						},
					},
					Right: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 9},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "4-task-b-TASK",
				Condition: &lhproto.EdgeCondition{
					Comparator: lhproto.Comparator_GREATER_THAN_EQ,
					Left: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 5},
							},
						},
					},
					Right: &lhproto.VariableAssignment{
						Source: &lhproto.VariableAssignment_LiteralValue{
							LiteralValue: &lhproto.VariableValue{
								Value: &lhproto.VariableValue_Int{Int: 3},
							},
						},
					},
				},
			},
			{
				SinkNodeName: "2-nop-NOP",
			},
		},
	}

	assert.Nil(t, err)
	assert.Equal(t, expectedNumberOutgoingEdgesFromFirstNopNode, len(firstNopNode.GetOutgoingEdges()))
	assert.True(t, proto.Equal(&expectedFirstNopeNode, firstNopNode))
	assert.Equal(t, expectedLastSinkNopNodeName, taskNodeB.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, expectedExitSinkNodeName, taskNodeA.GetOutgoingEdges()[0].GetSinkNodeName())
	assert.Equal(t, "3-task-a-TASK", lastNopNode.GetOutgoingEdges()[0].GetSinkNodeName())
}

func TestShouldCompileWorkflowUsingWaitForEnventWithTimeOutAndMutatingItsExternalEventNodeOutput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		address := thread.DeclareStr("address")
		extEventOutput := thread.WaitForEvent("verify-address").Timeout(10)
		address.Assign(extEventOutput)
		thread.Execute("task-a", address)
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	externalEventNode := entrypoint.Nodes["1-verify-address-EXTERNAL_EVENT"]

	expectedExternalEventNode := lhproto.Node{
		Node: &lhproto.Node_ExternalEvent{
			ExternalEvent: &lhproto.ExternalEventNode{
				ExternalEventDefId: &lhproto.ExternalEventDefId{
					Name: "verify-address",
				},
				TimeoutSeconds: &lhproto.VariableAssignment{
					Source: &lhproto.VariableAssignment_LiteralValue{
						LiteralValue: &lhproto.VariableValue{
							Value: &lhproto.VariableValue_Int{Int: 10},
						},
					},
				},
			},
		},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-task-a-TASK",
				VariableMutations: []*lhproto.VariableMutation{
					{
						LhsName:   "address",
						Operation: lhproto.VariableMutationType_ASSIGN,
						RhsValue: &lhproto.VariableMutation_RhsAssignment{
							RhsAssignment: &lhproto.VariableAssignment{
								Source: &lhproto.VariableAssignment_NodeOutput{
									NodeOutput: &lhproto.VariableAssignment_NodeOutputReference{
										NodeName: "1-verify-address-EXTERNAL_EVENT",
									},
								},
							},
						},
					},
				},
			},
		},
	}

	assert.Nil(t, err)
	assert.True(t, proto.Equal(&expectedExternalEventNode, externalEventNode))
}

func TestShouldCompileWorkflowUsingTasktWithTimeOutAndMutatingItsTaskNodeOutput(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		address := thread.DeclareStr("address")
		taskOutput := thread.Execute("task-a", address).Timeout(15)
		address.Assign(taskOutput)
	}, "my-workflow")

	compiledWorkflow, err := wf.Compile()

	entrypoint := compiledWorkflow.ThreadSpecs["entrypoint"]
	taskNode := entrypoint.Nodes["1-task-a-TASK"]

	expectedTaskNode := lhproto.Node{
		Node: &lhproto.Node_Task{
			Task: &lhproto.TaskNode{
				TaskToExecute: &lhproto.TaskNode_TaskDefId{
					TaskDefId: &lhproto.TaskDefId{
						Name: "task-a",
					},
				},
				TimeoutSeconds: 15,
				Variables: []*lhproto.VariableAssignment{
					{
						Source: &lhproto.VariableAssignment_VariableName{
							VariableName: "address",
						},
					},
				},
			},
		},
		OutgoingEdges: []*lhproto.Edge{
			{
				SinkNodeName: "2-exit-EXIT",
				VariableMutations: []*lhproto.VariableMutation{
					{
						LhsName:   "address",
						Operation: lhproto.VariableMutationType_ASSIGN,
						RhsValue: &lhproto.VariableMutation_RhsAssignment{
							RhsAssignment: &lhproto.VariableAssignment{
								Source: &lhproto.VariableAssignment_NodeOutput{
									NodeOutput: &lhproto.VariableAssignment_NodeOutputReference{
										NodeName: "1-task-a-TASK",
									},
								},
							},
						},
					},
				},
			},
		},
	}

	assert.Nil(t, err)
	assert.True(t, proto.Equal(&expectedTaskNode, taskNode))
}
