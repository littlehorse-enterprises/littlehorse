package cmd

import (
	"log"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
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
	Run: func(cmd *cobra.Command, args []string) {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 2 {
			needsHelp = true
		}

		if needsHelp {
			log.Fatal("Must provide 1 or 2 arguments. See 'lhctl get taskRun -h'")
		}

		common.PrintResp(getGlobalClient(cmd).GetTaskRun(
			requestContext(),
			&model.TaskRunId{
				WfRunId:  args[0],
				TaskGuid: args[1],
			},
		))
	},
}

var searchTaskRunCmd = &cobra.Command{
	Use:   "taskRun",
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
  - TASK_OUTPUT_SERIALIZING_ERROR
  - TASK_INPUT_VAR_SUB_ERROR
	`,
	Run: func(cmd *cobra.Command, args []string) {

		statusStr, _ := cmd.Flags().GetString("status")
		taskDefName, _ := cmd.Flags().GetString("taskDefName")

		var search *model.SearchTaskRunRequest

		if statusStr != "" {
			if taskDefName == "" {
				log.Fatal("Must provide taskDefName along with status!")
			}
			earliest, latest := loadEarliestAndLatestStart(cmd)

			statusInt, ok := model.TaskStatus_value[statusStr]
			if !ok {
				log.Fatal("Invalid status provided. See --help.")
			}
			status := model.TaskStatus(statusInt)

			search = &model.SearchTaskRunRequest{
				TaskRunCriteria: &model.SearchTaskRunRequest_StatusAndTaskDef{
					StatusAndTaskDef: &model.SearchTaskRunRequest_StatusAndTaskDefRequest{
						Status:        status,
						TaskDefName:   taskDefName,
						EarliestStart: earliest,
						LatestStart:   latest,
					},
				},
			}
		} else if taskDefName != "" {
			earliest, latest := loadEarliestAndLatestStart(cmd)

			search = &model.SearchTaskRunRequest{
				TaskRunCriteria: &model.SearchTaskRunRequest_TaskDef{
					TaskDef: &model.SearchTaskRunRequest_ByTaskDefRequest{
						TaskDefName:   taskDefName,
						EarliestStart: earliest,
						LatestStart:   latest,
					},
				},
			}
		} else {
			log.Fatal("must at least provide taskDefName")
		}
		common.PrintResp(getGlobalClient(cmd).SearchTaskRun(requestContext(), search))
	},
}

var listTaskRunCmd = &cobra.Command{
	Use:   "taskRun <wfRunId>",
	Short: "List all TaskRun's for a given WfRun Id.",
	Long: `
Lists all TaskRun's for a given WfRun Id.
`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("Must provide one arg: the WfRun ID!")
		}
		wfRunId := args[0]

		req := &model.ListTaskRunsRequest{
			WfRunId: wfRunId,
		}

		common.PrintResp(getGlobalClient(cmd).ListTaskRuns(
			requestContext(),
			req,
		))
	},
}

func init() {
	getCmd.AddCommand(getTaskRunCmd)
	searchCmd.AddCommand(searchTaskRunCmd)
	listCmd.AddCommand(listTaskRunCmd)

	searchTaskRunCmd.Flags().String("status", "", "Status of TaskRun's to search for.")
	searchTaskRunCmd.Flags().String("taskDefName", "", "TaskDef ID of TaskRun's to search for.")
	searchTaskRunCmd.MarkFlagRequired("taskDefName")
	searchTaskRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for TaskRuns that started no more than this number of minutes ago")
	searchTaskRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for TaskRuns that started at least this number of minutes ago")

}
