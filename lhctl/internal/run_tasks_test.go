package internal

import (
	"bytes"
	"context"
	"errors"
	"strings"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"google.golang.org/grpc"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

type taskClient struct {
	lhproto.LittleHorseClient
	def         *lhproto.TaskDef
	request     *lhproto.RunInlineWfRequest
	lookupErr   error
	runErr      error
	structDef   *lhproto.StructDef
	structCalls int
}

func (c *taskClient) GetStructDef(_ context.Context, _ *lhproto.StructDefId, _ ...grpc.CallOption) (*lhproto.StructDef, error) {
	c.structCalls++
	return c.structDef, nil
}

func (c *taskClient) GetTaskDef(_ context.Context, id *lhproto.TaskDefId, _ ...grpc.CallOption) (*lhproto.TaskDef, error) {
	if id.Name != "my-task" {
		return nil, errors.New("unexpected task name")
	}
	return c.def, c.lookupErr
}

func (c *taskClient) RunInlineWf(_ context.Context, req *lhproto.RunInlineWfRequest, _ ...grpc.CallOption) (*lhproto.WfRun, error) {
	c.request = req
	return &lhproto.WfRun{Id: &lhproto.WfRunId{Id: "created-run"}, Status: lhproto.LHStatus_RUNNING}, c.runErr
}

func taskDefinition() *lhproto.TaskDef {
	return &lhproto.TaskDef{Id: &lhproto.TaskDefId{Name: "my-task"}, InputVars: []*lhproto.VariableDef{
		{Name: "name", Type: lhproto.VariableType_STR.Enum()},
		{Name: "count", TypeDef: &lhproto.TypeDefinition{DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_INT}}},
	}}
}

func TestTaskInlineRequest(t *testing.T) {
	def := taskDefinition()
	before := proto.Clone(def)
	req, err := taskInlineRequest(context.Background(), nil, def, []string{"count", "42", "name", "Ada"}, "chosen-id")
	if err != nil {
		t.Fatal(err)
	}
	if req.GetId() != "chosen-id" || req.Variables["count"].GetInt() != 42 || req.Variables["name"].GetStr() != "Ada" {
		t.Fatalf("unexpected request: %v", req)
	}
	thread := req.WfSpec.ThreadSpecs[req.WfSpec.EntrypointThreadName]
	if len(thread.Nodes) != 3 || len(thread.VariableDefs) != 2 {
		t.Fatalf("unexpected thread: %v", thread)
	}
	task := thread.Nodes["task"].GetTask()
	if task.GetTaskDefId().Name != "my-task" || task.Variables[0].GetVariableName() != "name" || task.Variables[1].GetVariableName() != "count" {
		t.Fatalf("task arguments not in definition order: %v", task)
	}
	if thread.Nodes["start"].OutgoingEdges[0].SinkNodeName != "task" || thread.Nodes["task"].OutgoingEdges[0].SinkNodeName != "done" {
		t.Fatal("incorrect graph edges")
	}
	if thread.Nodes["done"].GetExit().GetReturnContent().GetNodeOutput().GetNodeName() != "task" {
		t.Fatal("task output not returned")
	}
	if !proto.Equal(def, before) {
		t.Fatal("TaskDef was modified")
	}
}

func TestTaskInlineRequestRejectsBadParameters(t *testing.T) {
	for _, tc := range []struct {
		name    string
		args    []string
		message string
	}{
		{"unpaired", []string{"name"}, "pairs"},
		{"unknown", []string{"other", "x"}, "not found"},
		{"duplicate", []string{"name", "x", "name", "y"}, "more than once"},
		{"missing", []string{"name", "x"}, "missing parameter"},
		{"bad type", []string{"name", "x", "count", "NaN"}, "invalid parameter"},
	} {
		t.Run(tc.name, func(t *testing.T) {
			_, err := taskInlineRequest(context.Background(), nil, taskDefinition(), tc.args, "")
			if err == nil || !strings.Contains(err.Error(), tc.message) {
				t.Fatalf("got %v, want %s", err, tc.message)
			}
		})
	}
}

func TestTaskInlineRequestDefaultsAndVoid(t *testing.T) {
	def := &lhproto.TaskDef{Id: &lhproto.TaskDefId{Name: "my-task"}, ReturnType: &lhproto.ReturnType{}}
	req, err := taskInlineRequest(context.Background(), nil, def, nil, "")
	if err != nil {
		t.Fatal(err)
	}
	if req.Id != nil || req.WfSpec.ThreadSpecs["main"].Nodes["done"].GetExit().Result != nil {
		t.Fatal("void task should have no output assignment or explicit ID")
	}
	def.InputVars = []*lhproto.VariableDef{{Name: "count", Type: lhproto.VariableType_INT.Enum(), DefaultValue: &lhproto.VariableValue{Value: &lhproto.VariableValue_Int{Int: 3}}}}
	req, err = taskInlineRequest(context.Background(), nil, def, nil, "")
	if err != nil {
		t.Fatal(err)
	}
	input := req.WfSpec.ThreadSpecs["main"].VariableDefs[0]
	if input.Required || input.VarDef.DefaultValue.GetInt() != 3 {
		t.Fatal("default not preserved")
	}
}

func TestTaskInlineRequestStructuredInputs(t *testing.T) {
	typeDef := &lhproto.TypeDefinition{
		DefinedType: &lhproto.TypeDefinition_StructDefId{StructDefId: &lhproto.StructDefId{Name: "person", Version: 1}},
		Masked:      true,
	}
	fake := &taskClient{structDef: &lhproto.StructDef{
		Id: typeDef.GetStructDefId(),
		StructDef: &lhproto.InlineStructDef{Fields: map[string]*lhproto.StructFieldDef{
			"name": {FieldType: &lhproto.TypeDefinition{DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_STR}}},
		}},
	}}
	def := &lhproto.TaskDef{Id: &lhproto.TaskDefId{Name: "my-task"}, InputVars: []*lhproto.VariableDef{
		{Name: "first", TypeDef: typeDef}, {Name: "second", TypeDef: typeDef},
	}}
	req, err := taskInlineRequest(context.Background(), fake, def, []string{"first", `{"name":"Ada"}`, "second", `{"name":"Grace"}`}, "")
	if err != nil {
		t.Fatal(err)
	}
	if fake.structCalls != 1 {
		t.Fatalf("expected cached StructDef lookup, got %d calls", fake.structCalls)
	}
	if req.Variables["first"].GetStruct().GetStruct().GetFields()["name"].GetValue().GetStr() != "Ada" {
		t.Fatal("struct not parsed")
	}
	if !req.WfSpec.ThreadSpecs["main"].VariableDefs[0].VarDef.TypeDef.Masked {
		t.Fatal("masking information lost")
	}
}

func TestRunTasksCommand(t *testing.T) {
	for _, tc := range []struct {
		name              string
		args              []string
		lookupErr, runErr error
		wantErr           bool
	}{
		{name: "success", args: []string{"my-task", "count", "2", "name", "Ada", "--wfRunId", "chosen-id"}},
		{name: "no task", wantErr: true},
		{name: "unpaired", args: []string{"my-task", "name"}, wantErr: true},
		{name: "missing TaskDef", args: []string{"my-task"}, lookupErr: errors.New("not found"), wantErr: true},
		{name: "RPC error", args: []string{"my-task", "name", "Ada", "count", "2"}, runErr: errors.New("permission denied"), wantErr: true},
	} {
		t.Run(tc.name, func(t *testing.T) {
			fake := &taskClient{def: taskDefinition(), lookupErr: tc.lookupErr, runErr: tc.runErr}
			var client lhproto.LittleHorseClient = fake
			oldClient, oldConfig := globalClient, globalConfig
			globalClient, globalConfig = &client, &littlehorse.LHConfig{}
			t.Cleanup(func() { globalClient, globalConfig = oldClient, oldConfig })
			cmd := newRunTasksCommand()
			var output bytes.Buffer
			cmd.SetOut(&output)
			cmd.SetErr(&output)
			cmd.SetArgs(tc.args)
			err := cmd.Execute()
			if (err != nil) != tc.wantErr {
				t.Fatalf("unexpected error: %v", err)
			}
			if !tc.wantErr {
				run := &lhproto.WfRun{}
				if err := protojson.Unmarshal(output.Bytes(), run); err != nil {
					t.Fatal(err)
				}
				if run.GetId().GetId() != "created-run" || run.Status != lhproto.LHStatus_RUNNING {
					t.Fatalf("not the returned WfRun: %v", run)
				}
				if fake.request.GetId() != "chosen-id" {
					t.Fatal("run ID flag ignored")
				}
			}
		})
	}
}

func TestRunTasksRouting(t *testing.T) {
	for _, tc := range []struct {
		args []string
		want string
	}{
		{[]string{"run", "tasks", "my-task"}, "tasks"},
		{[]string{"run", "my-workflow"}, "run"},
		{[]string{"run", "--", "tasks"}, "run"},
	} {
		cmd, _, err := rootCmd.Find(tc.args)
		if err != nil || cmd.Name() != tc.want {
			t.Fatalf("routing %v: command=%v error=%v", tc.args, cmd, err)
		}
	}
}
