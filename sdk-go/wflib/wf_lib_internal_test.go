package wflib_test

import (
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/wflib"
	"github.com/stretchr/testify/assert"
)

func TestCanMakePersistentVariable(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.ThreadBuilder) {
		t.AddVariable(
			"my-var", model.VariableType_BOOL,
		).WithIndex(
			model.IndexType_LOCAL_INDEX,
		).Persistent()
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	varDef := entrypoint.VariableDefs[0]
	assert.Equal(t, "my-var", varDef.Name)
	assert.True(t, varDef.Persistent)
}

func TestUserTaskAssignToUser(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.ThreadBuilder) {
		t.AssignTaskToUser("my-task", "yoda", nil)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assignment := utNode.GetUser()
	assert.NotNil(t, assignment)
	assert.Nil(t, assignment.UserGroup)
	assert.Equal(t, "yoda", *(assignment.UserId.GetLiteralValue().Str))
}

func TestUserTaskAssignToUserByVar(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.ThreadBuilder) {
		userVar := t.AddVariable("user", model.VariableType_STR)
		t.AssignTaskToUser("my-task", userVar, nil)
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assignment := utNode.GetUser()
	assert.NotNil(t, assignment)
	assert.Nil(t, assignment.UserGroup)
	assert.Equal(t, "user", assignment.UserId.GetVariableName())
}

func TestUserTaskAssignToUserWithGroup(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.ThreadBuilder) {
		t.AssignTaskToUser("my-task", "yoda", "jedi-council")
	}, "my-workflow")

	putWf, err := wf.Compile()
	if err != nil {
		t.Error(err)
	}

	entrypoint := putWf.ThreadSpecs[putWf.EntrypointThreadName]
	node := entrypoint.Nodes["1-my-task-USER_TASK"]

	utNode := node.GetUserTask()
	assert.NotNil(t, utNode)

	assignment := utNode.GetUser()
	assert.NotNil(t, assignment)
	assert.Equal(t, "yoda", *(assignment.UserId.GetLiteralValue().Str))
	assert.Equal(t, "jedi-council", *(assignment.UserGroup.GetLiteralValue().Str))
}

func TestAssignToGroup(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.ThreadBuilder) {
		t.AssignTaskToUserGroup("my-task", "jedi-council")
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
	assert.Equal(t, "jedi-council", *(assignment.GetLiteralValue().Str))
}

func TestReleaseToGroup(t *testing.T) {
	wf := wflib.NewWorkflow(func(t *wflib.ThreadBuilder) {
		ut := t.AssignTaskToUser("my-task", "yoda", "jedi-council")
		t.ReassignToGroupOnDeadline(ut, nil, 10)
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

	group := reassign.GetUserGroup().GetLiteralValue().Str
	assert.Equal(t, "jedi-council", *group)
}

func TestReassignToGroup(t *testing.T) {
	group := "jedi-council"
	wf := wflib.NewWorkflow(func(t *wflib.ThreadBuilder) {
		ut := t.AssignTaskToUser("my-task", "yoda", nil)
		t.ReassignToGroupOnDeadline(ut, &group, 10)
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

	assert.Equal(t, group, *(reassign.GetUserGroup().GetLiteralValue().Str))
}
