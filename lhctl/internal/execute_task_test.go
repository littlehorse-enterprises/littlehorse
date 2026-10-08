package internal

import (
	"context"
	"io"
	"strings"
	"testing"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/proto"
)

func TestExecuteTaskCommandRegistration(t *testing.T) {
	cmd, args, err := rootCmd.Find([]string{"execute", "task", "my-task", "Ada", "2"})
	if err != nil {
		t.Fatal(err)
	}
	if cmd.Name() != "task" || cmd.Parent() != executeCmd || len(args) != 3 {
		t.Fatalf("unexpected command resolution: %s %v", cmd.CommandPath(), args)
	}
	for _, child := range runCmd.Commands() {
		if child.Name() == "tasks" {
			t.Fatal("task execution should no longer be registered under run")
		}
	}
}

func TestTaskInlineWorkflowPreservesInputs(t *testing.T) {
	def := &lhproto.TaskDef{
		Id: &lhproto.TaskDefId{Name: "hello"},
		InputVars: []*lhproto.VariableDef{
			{Name: "name", TypeDef: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_STR},
				Masked:      true,
			}},
			{Name: "count", TypeDef: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_INT},
			}, DefaultValue: &lhproto.VariableValue{Value: &lhproto.VariableValue_Int{Int: 3}}},
		},
	}
	before := proto.Clone(def)
	req, err := taskInlineRequest(context.Background(), nil, def, []string{"Ada"}, nil, "chosen-id")
	if err != nil {
		t.Fatal(err)
	}
	if req.GetId() != "chosen-id" || len(req.Variables) != 0 {
		t.Fatalf("unexpected request: %v", req)
	}
	thread := req.WfSpec.ThreadSpecs[req.WfSpec.EntrypointThreadName]
	if len(thread.VariableDefs) != 0 || len(thread.Nodes) != 3 {
		t.Fatalf("unexpected compiled workflow: %v", thread)
	}
	var taskName string
	var exit *lhproto.ExitNode
	for nodeName, node := range thread.Nodes {
		if task := node.GetTask(); task != nil {
			taskName = nodeName
			if task.GetTaskDefId().GetName() != "hello" || len(task.Variables) != 2 ||
				task.Variables[0].GetLiteralValue().GetStr() != "Ada" ||
				!proto.Equal(task.Variables[1].GetLiteralValue(), def.InputVars[1].DefaultValue) {
				t.Fatalf("unexpected task arguments: %v", task)
			}
		}
		if node.GetExit() != nil {
			exit = node.GetExit()
		}
	}
	if taskName == "" || exit.GetReturnContent().GetNodeOutput().GetNodeName() != taskName {
		t.Fatal("workflow does not return the task output")
	}
	if !proto.Equal(def, before) {
		t.Fatal("TaskDef was modified")
	}
}

func TestExecuteTaskArgumentForms(t *testing.T) {
	def := &lhproto.TaskDef{
		Id: &lhproto.TaskDefId{Name: "hello"},
		InputVars: []*lhproto.VariableDef{
			{Name: "name", TypeDef: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_STR},
			}},
			{Name: "age", TypeDef: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_INT},
			}},
			{Name: "data", TypeDef: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_JSON_OBJ},
			}},
		},
	}
	for _, tc := range []struct {
		name     string
		args     []string
		wantName string
		wantAge  int64
	}{
		{"positional", []string{"hello", "Ada", "2", `{"a":1,"b":2}`}, "Ada", 2},
		{"named", []string{"hello", "--arg", "age:2", "--arg", `data:{"a":1,"b":2}`, "--arg", "name:Ada"}, "Ada", 2},
		{"colon in value", []string{"hello", "--arg", "name:https://example.com", "--arg", "age:2", "--arg", "data:{}"}, "https://example.com", 2},
		{"empty string", []string{"hello", "--arg", "name:", "--arg", "age:2", "--arg", "data:{}"}, "", 2},
		{"negative positional", []string{"hello", "--", "Ada", "-2", "{}"}, "Ada", -2},
	} {
		t.Run(tc.name, func(t *testing.T) {
			cmd := newExecuteTaskCommand()
			cmd.SetArgs(tc.args)
			var request *lhproto.RunInlineWfRequest
			cmd.RunE = func(cmd *cobra.Command, args []string) error {
				named, err := cmd.Flags().GetStringArray("arg")
				if err != nil {
					return err
				}
				request, err = taskInlineRequest(context.Background(), nil, def, args[1:], named, "")
				return err
			}
			if err := cmd.Execute(); err != nil {
				t.Fatal(err)
			}
			thread := request.WfSpec.ThreadSpecs[request.WfSpec.EntrypointThreadName]
			if len(thread.VariableDefs) != 0 || len(request.Variables) != 0 {
				t.Fatal("unexpected workflow variables")
			}
			for _, node := range thread.Nodes {
				if task := node.GetTask(); task != nil {
					if len(task.Variables) != 3 || task.Variables[0].GetLiteralValue().GetStr() != tc.wantName ||
						task.Variables[1].GetLiteralValue().GetInt() != tc.wantAge || task.Variables[2].GetLiteralValue().GetJsonObj() == "" {
						t.Fatalf("unexpected literals: %v", task)
					}
					return
				}
			}
			t.Fatal("missing task node")
		})
	}
}

func TestExecuteTaskRejectsInvalidArguments(t *testing.T) {
	def := &lhproto.TaskDef{Id: &lhproto.TaskDefId{Name: "hello"}, InputVars: []*lhproto.VariableDef{
		{Name: "age", TypeDef: &lhproto.TypeDefinition{
			DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_INT},
		}},
	}}
	for _, tc := range []struct {
		name    string
		args    []string
		message string
	}{
		{"missing task", nil, "at least 1 arg"},
		{"mixed", []string{"hello", "2", "--arg", "age:3"}, "cannot be combined"},
		{"too many", []string{"hello", "2", "3"}, "too many positional"},
		{"missing input", []string{"hello"}, `missing parameter "age"`},
		{"unknown", []string{"hello", "--arg", "name:Ada"}, "not found"},
		{"duplicate", []string{"hello", "--arg", "age:2", "--arg", "age:3"}, "more than once"},
		{"no colon", []string{"hello", "--arg", "age"}, "expected name:value"},
		{"empty name", []string{"hello", "--arg", ":2"}, "expected name:value"},
		{"invalid positional type", []string{"hello", "abc"}, `invalid parameter "age"`},
		{"invalid named type", []string{"hello", "--arg", "age:abc"}, `invalid parameter "age"`},
	} {
		t.Run(tc.name, func(t *testing.T) {
			cmd := newExecuteTaskCommand()
			cmd.SetOut(io.Discard)
			cmd.SetErr(io.Discard)
			cmd.SetArgs(tc.args)
			cmd.RunE = func(cmd *cobra.Command, args []string) error {
				named, _ := cmd.Flags().GetStringArray("arg")
				_, err := taskInlineRequest(context.Background(), nil, def, args[1:], named, "")
				return err
			}
			if err := cmd.Execute(); err == nil || !strings.Contains(err.Error(), tc.message) {
				t.Fatalf("expected %q, got %v", tc.message, err)
			}
		})
	}
}

func TestTaskInlineWorkflowWithVoidOutput(t *testing.T) {
	def := &lhproto.TaskDef{Id: &lhproto.TaskDefId{Name: "hello"}, ReturnType: &lhproto.ReturnType{}}
	req, err := taskInlineRequest(context.Background(), nil, def, nil, nil, "")
	if err != nil {
		t.Fatal(err)
	}
	if req.Id != nil || req.WfSpec.Id != nil || req.WfSpec.CreatedAt != nil {
		t.Fatal("unexpected run or server-owned metadata")
	}
	for _, node := range req.WfSpec.ThreadSpecs[req.WfSpec.EntrypointThreadName].Nodes {
		if exit := node.GetExit(); exit != nil {
			if exit.Result != nil {
				t.Fatal("void task must not supply a workflow output")
			}
			return
		}
	}
	t.Fatal("no exit node")
}

func TestTaskInlineWorkflowLiteralOverridesDefault(t *testing.T) {
	def := &lhproto.TaskDef{
		Id: &lhproto.TaskDefId{Name: "hello"},
		InputVars: []*lhproto.VariableDef{
			{Name: "name", TypeDef: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_STR},
			}},
			{Name: "count", TypeDef: &lhproto.TypeDefinition{
				DefinedType: &lhproto.TypeDefinition_PrimitiveType{PrimitiveType: lhproto.VariableType_INT},
			},
				DefaultValue: &lhproto.VariableValue{Value: &lhproto.VariableValue_Int{Int: 3}}},
		},
	}
	req, err := taskInlineRequest(context.Background(), nil, def, nil, []string{"count:0", "name:Ada"}, "")
	if err != nil {
		t.Fatal(err)
	}
	for _, node := range req.WfSpec.ThreadSpecs[req.WfSpec.EntrypointThreadName].Nodes {
		if task := node.GetTask(); task != nil {
			zero := &lhproto.VariableValue{Value: &lhproto.VariableValue_Int{Int: 0}}
			if len(task.Variables) != 2 || task.Variables[0].GetLiteralValue().GetStr() != "Ada" ||
				!proto.Equal(task.Variables[1].GetLiteralValue(), zero) {
				t.Fatalf("literal arguments must follow TaskDef order and override defaults: %v", task)
			}
		}
	}
	if _, err := taskInlineRequest(context.Background(), nil, def, nil, nil, ""); err == nil {
		t.Fatal("missing required input must be rejected")
	}
}
