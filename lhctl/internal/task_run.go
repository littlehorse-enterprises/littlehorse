package internal

import (
	"errors"
	"log"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

// getTaskRunCmd represents the nodeRun command
var getTaskRunCmd = &cobra.Command{
	Use:   "taskRun <wfRunId> <taskRunGuid>",
	Short: "Get a TaskRun by WfRunId and Guid",
	Long: `TaskRun's are identified uniquely by the combination of the following:
	- Associated WfRun Id
	- A unique guid

	You may provide both identifiers as two separate arguments or you may provide
	them delimited by the '/' character.`,
	Args: func(cmd *cobra.Command, args []string) error {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 2 {
			needsHelp = true
		}

		if needsHelp {
			return errors.New("must provide 1 or 2 arguments. See 'lhctl get taskRun -h'")
		}

		return nil
	},
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		littlehorse.PrintResp(getGlobalClient(cmd).GetTaskRun(
			requestContext(cmd),
			&lhproto.TaskRunId{
				WfRunId:  littlehorse.StrToWfRunId(args[0]),
				TaskGuid: args[1],
			},
		))
	},
}

var searchTaskRunCmd = &cobra.Command{
	Use:   "taskRun [<taskDefName>]",
	Short: "Search for TaskRun's.",
	Long: `
Search for TaskRun's by their taskDefName and/or status. Returns a list of TaskRunId's.

Choose one of the following option groups:
// Returns all TaskRun's from a specified WfRun.
[wfRunId]

// For user task search. Use any combination of the following, except note
// that userId and userGroup are mutually exclusive.
[userTaskDefName, userTaskStatus, userId, userGroup]

* Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
	options with any group except [--wfRunId] to put a time bound on WfRun's
	which are returned. The time bound applies to the time that the WfRun was
	created.
* Note: Valid options for --status are:
  - TASK_SCHEDULED
  - TASK_RUNNING
  - TASK_SUCCESS
  - TASK_FAILED
  - TASK_TIMEOUT
  - TASK_OUTPUT_SERDE_ERROR
  - TASK_INPUT_VAR_SUB_ERROR
	`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		taskDefName := args[0]

		statusStr, _ := cmd.Flags().GetString("status")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		var status *lhproto.TaskStatus = nil
		earliest, latest := loadEarliestAndLatestStart(cmd)

		if statusStr != "" {
			statusInt, ok := lhproto.TaskStatus_value[statusStr]
			if !ok {
				log.Fatal("Invalid status provided. See --help.")
			}
			status = (*lhproto.TaskStatus)(&statusInt)
		}

		search := &lhproto.SearchTaskRunRequest{
			Status:        status,
			TaskDefName:   taskDefName,
			EarliestStart: earliest,
			LatestStart:   latest,
			Bookmark:      bookmark,
			Limit:         &limit,
		}
		littlehorse.PrintResp(getGlobalClient(cmd).SearchTaskRun(requestContext(cmd), search))
	},
}

var listTaskRunCmd = &cobra.Command{
	Use:   "taskRun <wfRunId>",
	Short: "List all TaskRun's for a given WfRun Id.",
	Long: `
Lists all TaskRun's for a given WfRun Id.
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId := args[0]

		req := &lhproto.ListTaskRunsRequest{
			WfRunId: littlehorse.StrToWfRunId(wfRunId),
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListTaskRuns(
			requestContext(cmd),
			req,
		))
	},
}

func init() {
	getCmd.AddCommand(getTaskRunCmd)
	searchCmd.AddCommand(searchTaskRunCmd)
	listCmd.AddCommand(listTaskRunCmd)

	searchTaskRunCmd.Flags().String("status", "", "Status of TaskRun's to search for.")
	searchTaskRunCmd.MarkFlagRequired("taskDefName")
	searchTaskRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for TaskRuns that started no more than this number of minutes ago")
	searchTaskRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for TaskRuns that started at least this number of minutes ago")

}
