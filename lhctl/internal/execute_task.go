package internal

import (
	"context"
	"fmt"
	"strconv"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/encoding/protojson"
)

func newExecuteTaskCommand() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "task <taskDefName> [<value>...]",
		Short: "Run a registered TaskDef inside an inline workflow.",
		Long: `Run a registered TaskDef without registering a WfSpec. Supply values in
TaskDef input order, or use repeated --arg name:value flags in any order.
Both forms use the TaskDef's input types. Do not mix the two forms.
Omitted inputs use their TaskDef defaults; positional values can only omit trailing inputs.
Quote JSON values. Use -- before positional values that begin with a dash.
Prints the created WfRun immediately; does not wait for task completion.
A compatible server and a worker for the TaskDef are required.`,
		Example: `  lhctl execute task my-task Ada 2
  lhctl execute task my-task --arg name:Ada --arg age:2`,
		Args: func(cmd *cobra.Command, args []string) error {
			if err := cobra.MinimumNArgs(1)(cmd, args); err != nil {
				return err
			}
			if len(args) > 1 && cmd.Flags().Changed("arg") {
				return fmt.Errorf("positional values cannot be combined with --arg")
			}
			return nil
		},
		RunE: func(cmd *cobra.Command, args []string) error {
			id, _ := cmd.Flags().GetString("wfRunId")
			namedArgs, _ := cmd.Flags().GetStringArray("arg")
			ctx := requestContext(cmd)
			client := getGlobalClient(cmd)
			def, err := client.GetTaskDef(ctx, &lhproto.TaskDefId{Name: args[0]})
			if err != nil {
				return fmt.Errorf("get TaskDef %q: %w", args[0], err)
			}
			req, err := taskInlineRequest(ctx, client, def, args[1:], namedArgs, id)
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
	cmd.Flags().StringArray("arg", nil, "Task input as name:value (repeat for each input)")
	return cmd
}

func taskInlineRequest(ctx context.Context, client lhproto.LittleHorseClient, def *lhproto.TaskDef, positional, named []string, id string) (*lhproto.RunInlineWfRequest, error) {
	if len(positional) > 0 && len(named) > 0 {
		return nil, fmt.Errorf("positional values cannot be combined with --arg")
	}
	if len(positional) > len(def.GetInputVars()) {
		return nil, fmt.Errorf("too many positional values: TaskDef accepts %d inputs, got %d", len(def.GetInputVars()), len(positional))
	}
	inputVariables := make(map[string]*lhproto.VariableDef)
	for _, input := range def.GetInputVars() {
		inputVariables[input.GetName()] = input
	}
	clientValues := make(map[string]string)
	for i, value := range positional {
		clientValues[def.InputVars[i].GetName()] = value
	}
	for _, arg := range named {
		name, value, ok := strings.Cut(arg, ":")
		if !ok || name == "" {
			return nil, fmt.Errorf("invalid --arg %q: expected name:value", arg)
		}
		if _, ok := inputVariables[name]; !ok {
			return nil, fmt.Errorf("parameter %q not found in TaskDef", name)
		}
		if _, ok := clientValues[name]; ok {
			return nil, fmt.Errorf("parameter %q provided more than once", name)
		}
		clientValues[name] = value
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
	for name, rawValue := range clientValues {
		input := inputVariables[name]
		if input.TypeDef == nil {
			return nil, fmt.Errorf("parameter %q has no type information in TaskDef", name)
		}
		value, err := littlehorse.TypeDefToVarValWithResolver(rawValue, input.TypeDef, resolve)
		if err != nil {
			return nil, fmt.Errorf("invalid parameter %q: %w", name, err)
		}
		values[name] = value
	}

	arguments := make([]interface{}, 0, len(def.GetInputVars()))
	for _, input := range def.GetInputVars() {
		value, ok := values[input.Name]
		if !ok {
			value = input.DefaultValue
		}
		if value == nil {
			return nil, fmt.Errorf("missing parameter %q", input.Name)
		}
		arguments = append(arguments, value)
	}
	workflow := littlehorse.NewInlineWorkflow(func(thread *littlehorse.WorkflowThread) {
		output := thread.Execute(def.GetId().GetName(), arguments...)
		// A missing ReturnType is a legacy TaskDef with an unknown output type.
		if def.ReturnType == nil || def.ReturnType.ReturnType != nil {
			thread.Complete(output)
		}
	})
	if id != "" {
		workflow.WithWfRunID(id)
	}
	return workflow.Compile()
}

func init() { executeCmd.AddCommand(newExecuteTaskCommand()) }
