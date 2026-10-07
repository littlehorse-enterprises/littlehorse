package internal

import (
	"context"
	"fmt"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

func newRunTasksCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "tasks <taskDefName> [<parameter> <value>]...",
		Short: "Run a registered TaskDef inside an inline workflow.",
		Long: `Run a registered TaskDef without registering a WfSpec. Parameters are
name/value pairs, parsed using the TaskDef's input types. Quote JSON values.
Prints the created WfRun immediately; does not wait for task completion.
A compatible server and a worker for the TaskDef are required.`,
		Args: func(cmd *cobra.Command, args []string) error {
			if err := cobra.MinimumNArgs(1)(cmd, args); err != nil {
				return err
			}
			if len(args)%2 != 1 {
				return fmt.Errorf("parameters must be pairs of <name> <value>")
			}
			return nil
		},
		RunE: func(cmd *cobra.Command, args []string) error {
			id, _ := cmd.Flags().GetString("wfRunId")
			ctx := requestContext(cmd)
			client := getGlobalClient(cmd)
			def, err := client.GetTaskDef(ctx, &lhproto.TaskDefId{Name: args[0]})
			if err != nil {
				return fmt.Errorf("get TaskDef %q: %w", args[0], err)
			}
			req, err := taskInlineRequest(ctx, client, def, args[1:], id)
			if err != nil {
				return err
			}
			run, err := client.RunInlineWf(ctx, req)
			if err != nil {
				return err
			}
			out, err := (protojson.MarshalOptions{Indent: "  ", EmitUnpopulated: true}).Marshal(run)
			if err != nil {
				return err
			}
			_, err = fmt.Fprintln(cmd.OutOrStdout(), string(out))
			return err
		},
	}
	cmd.Flags().String("wfRunId", "", "Set the WfRun ID (an existing ID returns ALREADY_EXISTS)")
	return cmd
}

func taskInlineRequest(ctx context.Context, client lhproto.LittleHorseClient, def *lhproto.TaskDef, args []string, id string) (*lhproto.RunInlineWfRequest, error) {
	if len(args)%2 != 0 {
		return nil, fmt.Errorf("parameters must be pairs of <name> <value>")
	}
	defs := make(map[string]*lhproto.VariableDef)
	for _, input := range def.GetInputVars() {
		defs[input.GetName()] = input
	}
	values := make(map[string]*lhproto.VariableValue)
	cache := make(map[string]*lhproto.StructDef)
	resolve := func(id *lhproto.StructDefId) (*lhproto.StructDef, error) {
		key := id.GetName() + ":" + strconv.Itoa(int(id.GetVersion()))
		if cached, ok := cache[key]; ok {
			return cached, nil
		}
		result, err := client.GetStructDef(ctx, id)
		if err == nil {
			cache[key] = result
		}
		return result, err
	}
	for i := 0; i < len(args); i += 2 {
		name := args[i]
		input, ok := defs[name]
		if !ok {
			return nil, fmt.Errorf("parameter %q not found in TaskDef", name)
		}
		if _, ok := values[name]; ok {
			return nil, fmt.Errorf("parameter %q provided more than once", name)
		}
		var value *lhproto.VariableValue
		var err error
		if input.TypeDef != nil {
			value, err = littlehorse.TypeDefToVarValWithResolver(args[i+1], input.TypeDef, resolve)
		} else if input.Type != nil {
			value, err = littlehorse.StrToVarVal(args[i+1], *input.Type)
		} else {
			return nil, fmt.Errorf("parameter %q has no type information in TaskDef", name)
		}
		if err != nil {
			return nil, fmt.Errorf("invalid parameter %q: %w", name, err)
		}
		values[name] = value
	}

	task := &lhproto.TaskNode{TaskToExecute: &lhproto.TaskNode_TaskDefId{TaskDefId: def.GetId()}}
	thread := &lhproto.ThreadSpec{}
	for _, input := range def.GetInputVars() {
		if _, ok := values[input.Name]; !ok && input.DefaultValue == nil {
			return nil, fmt.Errorf("missing parameter %q", input.Name)
		}
		thread.VariableDefs = append(thread.VariableDefs, &lhproto.ThreadVarDef{
			VarDef: proto.Clone(input).(*lhproto.VariableDef), Required: input.DefaultValue == nil,
		})
		task.Variables = append(task.Variables, &lhproto.VariableAssignment{
			Source: &lhproto.VariableAssignment_VariableName{VariableName: input.Name},
		})
	}
	exit := &lhproto.ExitNode{}
	// A missing ReturnType is a legacy TaskDef with an unknown output type.
	if def.ReturnType == nil || def.ReturnType.ReturnType != nil {
		exit.Result = &lhproto.ExitNode_ReturnContent{ReturnContent: &lhproto.VariableAssignment{
			Source: &lhproto.VariableAssignment_NodeOutput{NodeOutput: &lhproto.VariableAssignment_NodeOutputReference{NodeName: "task"}},
		}}
	}
	thread.Nodes = map[string]*lhproto.Node{
		"start": {Node: &lhproto.Node_Entrypoint{Entrypoint: &lhproto.EntrypointNode{}}, OutgoingEdges: []*lhproto.Edge{{SinkNodeName: "task"}}},
		"task":  {Node: &lhproto.Node_Task{Task: task}, OutgoingEdges: []*lhproto.Edge{{SinkNodeName: "done"}}},
		"done":  {Node: &lhproto.Node_Exit{Exit: exit}},
	}
	req := &lhproto.RunInlineWfRequest{
		WfSpec:    &lhproto.InlineWfSpec{EntrypointThreadName: "main", ThreadSpecs: map[string]*lhproto.ThreadSpec{"main": thread}},
		Variables: values,
	}
	if id != "" {
		req.Id = &id
	}
	return req, nil
}

func init() { runCmd.AddCommand(newRunTasksCommand()) }
