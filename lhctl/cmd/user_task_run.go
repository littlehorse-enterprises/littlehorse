package cmd

import (
	"context"
	"log"
	"strings"

	"github.com/littlehorse-eng/littlehorse/sdk-go/common"
	"github.com/littlehorse-eng/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

// getUserTaskRunCmd represents the nodeRun command
var getUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun <wfRunId> <userTaskGuid>",
	Short: "Get a UserTaskRun by WfRun, ThreadRun, and UserTask Run Position",
	Long: `UserTaskRun's are identified uniquely by the combination of the following:
	- Associated WfRun Id
	- ThreadRun Number
	- UserTaskRun Number (i.e. chronological position within the ThreadRun)

	You may provide all three identifiers as three separate arguments or you may provide
	them delimited by the '/' character, as returned in all 'search' command queries.`,
	Run: func(cmd *cobra.Command, args []string) {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 2 {
			needsHelp = true
		}

		if needsHelp {
			log.Fatal("Must provide exactly 1 or 2 arguments. See 'lhctl get userTaskRun -h'")

		}

		wfRunId, userTaskGuid := args[0], args[1]

		common.PrintResp(getGlobalClient(cmd).GetUserTaskRun(
			context.Background(),
			&model.UserTaskRunIdPb{
				WfRunId:      wfRunId,
				UserTaskGuid: userTaskGuid,
			},
		))
	},
}

var searchUserTaskRunCmd = &cobra.Command{
	Use:   "userTaskRun",
	Short: "Search for UserTaskRun's either by WfRunId or by {takDefId,Status}",
	Long: `
Search for UserTaskRun's by either the WfRunId or by providing the taskDefName and the Status.

Returns a list of ObjectId's that can be passed into 'lhctl get nodeRun'.

Choose one of the following option groups:
// Returns all UserTaskRun's from a specified WfRun.
[wfRunId]

// For user task search. Use any combination of the following, except note
// that userId and userGroup are mutually exclusive.
[userTaskDefName, userTaskStatus, userId, userGroup]

* Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
  options with any group except [--wfRunId] to put a time bound on WfRun's
  which are returned. The time bound applies to the time that the WfRun was
  created.
`,
	Run: func(cmd *cobra.Command, args []string) {

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		earliest, latest := loadEarliestAndLatestStart(cmd)

		search := &model.SearchUserTaskRunPb{
			EarliestStart: earliest,
			LatestStart:   latest,
			Bookmark:      bookmark,
			Limit:         &limit,
		}

		statusStr, _ := cmd.Flags().GetString("userTaskStatus")
		if statusStr != "" {
			statusInt, ok := model.UserTaskRunStatusPb_value[statusStr]
			if !ok {
				log.Fatal("Invalid value provided for userTaskStatus")
			}
			statusVal := model.UserTaskRunStatusPb(statusInt)
			search.Status = &statusVal
		}

		userIdStr, _ := cmd.Flags().GetString("userId")
		if userIdStr != "" {
			search.TaskOwner = &model.SearchUserTaskRunPb_UserId{
				UserId: userIdStr,
			}
		}

		userGroupStr, _ := cmd.Flags().GetString("userGroup")
		if userGroupStr != "" {
			search.TaskOwner = &model.SearchUserTaskRunPb_UserGroup{
				UserGroup: userGroupStr,
			}
		}

		userTaskDefNameStr, _ := cmd.Flags().GetString("userTaskDefName")
		if userTaskDefNameStr != "" {
			search.UserTaskDefName = &userTaskDefNameStr
		}

		search.Bookmark = bookmark
		search.Limit = &limit

		common.PrintResp(getGlobalClient(cmd).SearchUserTaskRun(context.Background(), search))

	},
}

func init() {
	getCmd.AddCommand(getUserTaskRunCmd)
	searchCmd.AddCommand(searchUserTaskRunCmd)

	searchUserTaskRunCmd.Flags().String("userTaskDefName", "", "UserTaskDef ID of User Task Run's to search for.")
	searchUserTaskRunCmd.Flags().String("userId", "", "Search for User Task Runs assigned to this User ID.")
	searchUserTaskRunCmd.Flags().String("userGroup", "", "Search for User Task Runs assigned to this User Group.")
	searchUserTaskRunCmd.Flags().String("userTaskStatus", "", "Status of User Task Runs to search for.")
	searchUserTaskRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for User Task Runs that started no more than this number of minutes ago")
	searchUserTaskRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for User Task Runs that started at least this number of minutes ago")

	searchUserTaskRunCmd.MarkFlagsMutuallyExclusive("userId", "userGroup")
}
