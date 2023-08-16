package cmd

import (
	"context"
	"log"
	"strings"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// getTaskRunCmd represents the nodeRunModel command
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
			context.Background(),
			&model.TaskRunIdPb{
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

		var search *model.SearchTaskRunPb

		if statusStr != "" {
			if taskDefName == "" {
				log.Fatal("Must provide taskDefName along with status!")
			}
			earliest, latest := loadEarliestAndLatestStart(cmd)

			statusInt, ok := model.TaskStatusPb_value[statusStr]
			if !ok {
				log.Fatal("Invalid status provided. See --help.")
			}
			status := model.TaskStatusPb(statusInt)

			search = &model.SearchTaskRunPb{
				TaskRunCriteria: &model.SearchTaskRunPb_StatusAndTaskDef{
					StatusAndTaskDef: &model.SearchTaskRunPb_StatusAndTaskDefPb{
						Status:        status,
						TaskDefName:   taskDefName,
						EarliestStart: earliest,
						LatestStart:   latest,
					},
				},
			}
		} else if taskDefName != "" {
			earliestMinutesAgo, _ := cmd.Flags().GetInt("earliestMinutesAgo")
			latestMinutesAgo, _ := cmd.Flags().GetInt32("latestMinutesAgo")
			earliestStartTime := &timestamppb.Timestamp{}
			latestStartTime := &timestamppb.Timestamp{}

			if earliestMinutesAgo == -1 {
				earliestStartTime = nil
			} else {
				earliestStartTime = timestamppb.New(
					time.Now().Add(-1 * time.Duration(earliestMinutesAgo) * time.Minute),
				)
			}

			if latestMinutesAgo == -1 {
				latestStartTime = nil
			} else {
				latestStartTime = timestamppb.New(
					time.Now().Add(-1 * time.Duration(latestMinutesAgo) * time.Minute),
				)
			}

			search = &model.SearchTaskRunPb{
				TaskRunCriteria: &model.SearchTaskRunPb_TaskDef{
					TaskDef: &model.SearchTaskRunPb_ByTaskDefPb{
						TaskDefName:   taskDefName,
						EarliestStart: earliestStartTime,
						LatestStart:   latestStartTime,
					},
				},
			}
		} else {
			log.Fatal("must at least provide taskDefName")
		}
		common.PrintResp(getGlobalClient(cmd).SearchTaskRun(context.Background(), search))
	},
}

func init() {
	getCmd.AddCommand(getTaskRunCmd)
	searchCmd.AddCommand(searchTaskRunCmd)

	searchTaskRunCmd.Flags().String("status", "", "Status of TaskRun's to search for.")
	searchTaskRunCmd.Flags().String("taskDefName", "", "TaskDef ID of TaskRun's to search for.")
	searchTaskRunCmd.MarkFlagRequired("taskDefName")
}
