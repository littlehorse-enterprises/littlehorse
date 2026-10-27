package littlehorse_test

import (
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/stretchr/testify/require"
	"google.golang.org/protobuf/proto"
)

func TestInlineWorkflowCompilesTask(t *testing.T) {
	request, err := littlehorse.NewInlineWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.Execute("hello")
	}).Compile()

	require.NoError(t, err)
	require.Equal(t, "entrypoint", request.WfSpec.EntrypointThreadName)
	thread := request.WfSpec.ThreadSpecs["entrypoint"]
	require.Len(t, thread.Nodes, 3)
	require.Equal(t, "hello", thread.Nodes["1-hello-TASK"].GetTask().GetTaskDefId().GetName())
	require.Nil(t, request.Id)
	require.Nil(t, request.WfSpec.Id)
	require.Nil(t, request.WfSpec.CreatedAt)
	require.Nil(t, request.WfSpec.RetentionPolicy)
	require.Empty(t, request.Variables)
}

func TestInlineWorkflowSupportsRunIDAndRetention(t *testing.T) {
	policy := &lhproto.WorkflowRetentionPolicy{
		WfGcPolicy: &lhproto.WorkflowRetentionPolicy_SecondsAfterWfTermination{SecondsAfterWfTermination: 60},
	}
	request, err := littlehorse.NewInlineWorkflow(func(thread *littlehorse.WorkflowThread) {
		thread.Execute("hello")
	}).WithWfRunID("my-run").WithRetentionPolicy(policy).Compile()

	require.NoError(t, err)
	require.NotNil(t, request.Id)
	require.Equal(t, "my-run", request.GetId())
	require.True(t, proto.Equal(policy, request.WfSpec.RetentionPolicy))
	require.Nil(t, request.WfSpec.Id)
	require.Nil(t, request.WfSpec.CreatedAt)
}

func TestInlineWorkflowUsesRegisteredWorkflowCompiler(t *testing.T) {
	workflow := func(thread *littlehorse.WorkflowThread) {
		input := thread.DeclareStr("input")
		task := thread.Execute("hello", input)
		thread.HandleException(task, nil, func(handler *littlehorse.WorkflowThread) {
			handler.Execute("recover")
		})
		thread.SpawnThread(func(child *littlehorse.WorkflowThread) {
			child.Execute("child-task")
		}, "child", nil)
	}
	registered, err := littlehorse.NewWorkflow(workflow, "registered").Compile()
	require.NoError(t, err)
	inline, err := littlehorse.NewInlineWorkflow(workflow).Compile()
	require.NoError(t, err)
	require.Greater(t, len(inline.WfSpec.ThreadSpecs), 1)
	require.True(t, proto.Equal(&lhproto.InlineWfSpec{
		ThreadSpecs:          registered.ThreadSpecs,
		EntrypointThreadName: registered.EntrypointThreadName,
	}, inline.WfSpec))
}
