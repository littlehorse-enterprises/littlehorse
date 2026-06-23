package internal

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

var putWorkflowMigrationPlanCmd = &cobra.Command{
	Use:   "workflowMigrationPlan",
	Short: "Interactively create a WorkflowMigrationPlan.",
	Long: `Interactively create a WorkflowMigrationPlan.

You will be prompted for:
  - The name of the WorkflowMigrationPlan
  - The source (old) WfSpec name, major version, and revision
  - The destination (new) major version and revision
  - One or more thread migrations (old threadSpec name -> new threadSpec name)
  - For each thread migration, zero or more node migrations (old node name -> new node name)

Leave the "old threadSpec name" or "old node name" prompt empty to finish that section.
`,
	Args: cobra.NoArgs,
	Run: func(cmd *cobra.Command, args []string) {
		reader := bufio.NewReader(os.Stdin)

		name := mustPromptLine(reader, "Name of the WorkflowMigrationPlan")

		oldWfSpecName := mustPromptLine(reader, "Old WfSpec name")
		oldMajorVersion := mustPromptInt32(reader, "Old WfSpec major version")
		oldRevision := mustPromptInt32(reader, "Old WfSpec revision")

		newMajorVersion := mustPromptInt32(reader, "New (destination) major version")
		newRevision := mustPromptInt32(reader, "New (destination) revision")

		threadMigrations := make(map[string]*lhproto.ThreadMigrationPlanRequest)
		fmt.Println("\nThread migrations (leave the old threadSpec name empty to finish):")
		for {
			oldThreadName := promptLine(reader, "  Old threadSpec name")
			if oldThreadName == "" {
				break
			}

			newThreadName := mustPromptLine(reader, "  New threadSpec name")

			nodeMigrations := make(map[string]*lhproto.NodeMigrationPlan)
			fmt.Println("    Node migrations (leave the old node name empty to finish):")
			for {
				oldNodeName := promptLine(reader, "      Old node name")
				if oldNodeName == "" {
					break
				}
				newNodeName := mustPromptLine(reader, "      New node name")
				nodeMigrations[oldNodeName] = &lhproto.NodeMigrationPlan{
					NewNodeName: newNodeName,
				}
			}

			threadMigrations[oldThreadName] = &lhproto.ThreadMigrationPlanRequest{
				NewThreadName:  newThreadName,
				NodeMigrations: nodeMigrations,
			}
		}

		if len(threadMigrations) == 0 {
			log.Fatal("At least one thread migration is required.")
		}

		req := &lhproto.PutWorkflowMigrationPlanRequest{
			Name: name,
			OldWfSpec: &lhproto.WfSpecId{
				Name:         oldWfSpecName,
				MajorVersion: oldMajorVersion,
				Revision:     oldRevision,
			},
			MajorVersion:     newMajorVersion,
			Revision:         newRevision,
			ThreadMigrations: threadMigrations,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).PutWorkflowMigrationPlan(requestContext(cmd), req))
	},
}

var getWorkflowMigrationPlanCmd = &cobra.Command{
	Use:   "workflowMigrationPlan <name>",
	Short: "Get a WorkflowMigrationPlan by name.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(
			getGlobalClient(cmd).GetWorkflowMigrationPlan(
				requestContext(cmd),
				&lhproto.WorkflowMigrationPlanId{
					Name: args[0],
				},
			),
		)
	},
}

var deleteWorkflowMigrationPlanCmd = &cobra.Command{
	Use:   "workflowMigrationPlan <name>",
	Short: "Delete a WorkflowMigrationPlan.",
	Long: `Delete a WorkflowMigrationPlan. You must provide the name of the
WorkflowMigrationPlan to delete.
	`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(
			getGlobalClient(cmd).DeleteWorkflowMigrationPlan(
				requestContext(cmd),
				&lhproto.DeleteWorkflowMigrationPlanRequest{
					Id: &lhproto.WorkflowMigrationPlanId{
						Name: args[0],
					},
				}),
		)
	},
}

var applyWorkflowMigrationPlanCmd = &cobra.Command{
	Use:   "workflowMigrationPlan <planName> <wfRunId>",
	Short: "Apply a WorkflowMigrationPlan to a running WfRun.",
	Long: `Apply a WorkflowMigrationPlan to a running WfRun.

Stamps the given WfRun with the WorkflowMigrationPlan so that its ThreadRuns
migrate lazily to the new WfSpec version.

You will then be prompted to optionally provide migration variables. These let you
reassign variable values per thread as part of the migration. Only literal values
are supported in lhctl. Leave the thread name (or variable name) empty to finish.
`,
	Args: cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		planName := args[0]
		wfRunId := args[1]

		reader := bufio.NewReader(os.Stdin)

		migrationVarsByThread := make(map[string]*lhproto.MigrationVars)
		fmt.Println("\nMigration variables (leave the thread name empty to finish):")
		for {
			threadName := promptLine(reader, "  Thread name")
			if threadName == "" {
				break
			}

			varAssignments := make(map[string]*lhproto.VariableAssignment)
			fmt.Println("    Variable assignments (leave the variable name empty to finish):")
			for {
				varName := promptLine(reader, "      Variable name")
				if varName == "" {
					break
				}
				varAssignments[varName] = &lhproto.VariableAssignment{
					Source: &lhproto.VariableAssignment_LiteralValue{
						LiteralValue: mustPromptLiteralValue(reader),
					},
				}
			}

			if len(varAssignments) > 0 {
				migrationVarsByThread[threadName] = &lhproto.MigrationVars{
					VarAssignmentByVarName: varAssignments,
				}
			}
		}

		req := &lhproto.ApplyWorkflowMigrationPlanRequest{
			Id: &lhproto.WorkflowMigrationPlanId{
				Name: planName,
			},
			WfRunId:               littlehorse.StrToWfRunId(wfRunId),
			MigrationVarsByThread: migrationVarsByThread,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ApplyWorkflowMigrationPlan(requestContext(cmd), req))
	},
}

// promptLine prints a prompt and returns the trimmed line entered by the user.
func promptLine(reader *bufio.Reader, prompt string) string {
	fmt.Print(prompt + ": ")
	input, err := reader.ReadString('\n')
	if err != nil && input == "" {
		log.Fatal("Failed reading input: ", err)
	}
	return strings.TrimSpace(input)
}

// mustPromptLine prompts until a non-empty value is entered.
func mustPromptLine(reader *bufio.Reader, prompt string) string {
	for {
		value := promptLine(reader, prompt)
		if value != "" {
			return value
		}
		fmt.Println("  A value is required.")
	}
}

// mustPromptInt32 prompts until a valid int32 is entered.
func mustPromptInt32(reader *bufio.Reader, prompt string) int32 {
	for {
		value := promptLine(reader, prompt)
		parsed, err := strconv.ParseInt(value, 10, 32)
		if err != nil {
			fmt.Println("  Please enter a valid integer.")
			continue
		}
		return int32(parsed)
	}
}

// mustPromptLiteralValue prompts for a variable type and payload and returns the
// resulting literal VariableValue.
func mustPromptLiteralValue(reader *bufio.Reader) *lhproto.VariableValue {
	for {
		varTypeStr := mustPromptLine(reader, "      Variable type (INT, STR, BYTES, BOOL, JSON_OBJ, JSON_ARR, DOUBLE)")
		varType, validVarType := lhproto.VariableType_value[varTypeStr]
		if !validVarType {
			fmt.Println("  Unrecognized varType. Valid options: INT, STR, BYTES, BOOL, JSON_OBJ, JSON_ARR or DOUBLE.")
			continue
		}

		payloadStr := promptLine(reader, "      Value")
		content, err := littlehorse.StrToVarVal(payloadStr, lhproto.VariableType(varType))
		if err != nil {
			fmt.Println("  Failed deserializing value: " + err.Error())
			continue
		}
		return content
	}
}

func init() {
	putCmd.AddCommand(putWorkflowMigrationPlanCmd)
	getCmd.AddCommand(getWorkflowMigrationPlanCmd)
	deleteCmd.AddCommand(deleteWorkflowMigrationPlanCmd)
	applyCmd.AddCommand(applyWorkflowMigrationPlanCmd)
}
