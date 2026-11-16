package littlehorse_test

import (
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/stretchr/testify/require"
	"google.golang.org/protobuf/proto"
)

func TestUserTaskSchemaCompile(t *testing.T) {
	id := &lhproto.StructDefId{Name: "approval-result", Version: 2}
	schema := littlehorse.NewUserTaskSchema(id, "approve-request")
	request, err := schema.Compile()
	require.NoError(t, err)
	require.Equal(t, "approve-request", request.GetName())
	require.True(t, proto.Equal(id, request.GetResultStructDefId()))
	require.Empty(t, request.GetFields())

	// Neither caller-owned IDs nor returned requests can change the schema's contract.
	id.Version = 3
	request.ResultStructDefId.Version = 4
	compiledAgain, err := schema.Compile()
	require.NoError(t, err)
	require.EqualValues(t, 2, compiledAgain.GetResultStructDefId().GetVersion())
}

func TestUserTaskSchemaRejectsInvalidReferences(t *testing.T) {
	tests := []struct {
		name     string
		taskName string
		id       *lhproto.StructDefId
	}{
		{name: "missing task name", id: &lhproto.StructDefId{Name: "approval"}},
		{name: "missing schema", taskName: "approve"},
		{name: "missing schema name", taskName: "approve", id: &lhproto.StructDefId{}},
		{name: "unresolved version", taskName: "approve", id: &lhproto.StructDefId{Name: "approval", Version: -1}},
	}
	for _, test := range tests {
		t.Run(test.name, func(t *testing.T) {
			request, err := littlehorse.NewUserTaskSchema(test.id, test.taskName).Compile()
			require.Error(t, err)
			require.Nil(t, request)
		})
	}
}

func TestUserTaskStructOutputCanBePassedToWorker(t *testing.T) {
	wf := littlehorse.NewWorkflow(func(thread *littlehorse.WorkflowThread) {
		output := thread.AssignUserTask("approve-request", "obiwan", nil)
		thread.Execute("process-approval", output, output.Get("approved"))
	}, "approval-workflow")
	compiled, err := wf.Compile()
	require.NoError(t, err)
	nodes := compiled.GetThreadSpecs()[compiled.GetEntrypointThreadName()].GetNodes()
	args := nodes["2-process-approval-TASK"].GetTask().GetVariables()
	require.Len(t, args, 2)
	require.Equal(t, "1-approve-request-USER_TASK", args[0].GetNodeOutput().GetNodeName())
	require.Nil(t, args[0].GetLhPath())
	require.Equal(t, "1-approve-request-USER_TASK", args[1].GetNodeOutput().GetNodeName())
	require.Equal(t, "approved", args[1].GetLhPath().GetPath()[0].GetKey())
}
