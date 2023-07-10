/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"context"
	"log"
	"strconv"
	"strings"
	"time"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/timestamppb"
)

// getNodeRunCmd represents the nodeRun command
var getNodeRunCmd = &cobra.Command{
	Use:   "nodeRun <wfRunId> <threadRunNumber> <nodeRunPosition>",
	Short: "Get a NodeRun by WfRun, ThreadRun, and Node Run Position",
	Long: `NodeRun's are identified uniquely by the combination of the following:
	- Associated WfRun Id
	- ThreadRun Number
	- NodeRun Number (i.e. chronological position within the threadrun)

	You may provide all three identifiers as three separate arguments or you may provide
	them delimited by the '/' character, as returned in all 'search' command queries.`,
	Run: func(cmd *cobra.Command, args []string) {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 1 && len(args) != 3 {
			needsHelp = true
		}

		if needsHelp {
			log.Fatal("Must provide 1 or 3 arguments. See 'lhctl get nodeRun -h'")

		}

		trn, err := strconv.Atoi(args[1])
		if err != nil {
			log.Fatal("Couldn't convert threadRunNumber to int.")

		}

		pos, err := strconv.Atoi(args[2])
		if err != nil {
			log.Fatal("Couldn't convert nodeRunPosition to int.")

		}

		common.PrintResp(getGlobalClient(cmd).GetNodeRun(
			context.Background(),
			&model.NodeRunIdPb{
				WfRunId:         args[0],
				ThreadRunNumber: int32(trn),
				Position:        int32(pos),
			},
		))
	},
}

var listNodeRunCmd = &cobra.Command{
	Use:   "nodeRun <wfRunId>",
	Short: "List all NodeRun's for a given WfRun Id.",
	Long: `
Lists all NodeRun's for a given WfRun Id.
`,
	Run: func(cmd *cobra.Command, args []string) {
		// bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		// limit, _ := cmd.Flags().GetInt32("limit")

		if len(args) != 1 {
			log.Fatal("Must provide one arg: the WfRun ID!")
		}
		wfRunId := args[0]

		req := &model.ListNodeRunsPb{
			WfRunId: wfRunId,
		}

		common.PrintResp(getGlobalClient(cmd).ListNodeRuns(
			context.Background(),
			req,
		))
	},
}

var searchNodeRunCmd = &cobra.Command{
	Use:   "nodeRun",
	Short: "Search for NodeRun's either by WfRunId or by {takDefId,Status}",
	Long: `
Search for NodeRun's by either the WfRunId or by providing the taskDefName and the Status.

Returns a list of ObjectId's that can be passed into 'lhctl get nodeRun'.

Choose one of the following option groups:
[wfRunId]
[taskDefName, status]

// For user task search. Use any combination of the following, except note
// that userId and userGroup are mutually exclusive.
[userTaskDefName, userTaskStatus, userId, userGroup]
 
* Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
  options with any group except [--wfRunId] to put a time bound on WfRun's
  which are returned. The time bound applies to the time that the WfRun was
  created.
`,
	Run: func(cmd *cobra.Command, args []string) {

		status, _ := cmd.Flags().GetString("status")
		taskDefName, _ := cmd.Flags().GetString("taskDefName")
		wfRunId, _ := cmd.Flags().GetString("wfRunId")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		var search *model.SearchNodeRunPb

		if status != "" {
			if taskDefName == "" {
				log.Fatal("Must provide taskDefName along with status!")
			}
			earliest, latest := loadEarliestAndLatestStart(cmd)

			search = &model.SearchNodeRunPb{
				NoderunCriteria: &model.SearchNodeRunPb_StatusAndTaskdef{
					StatusAndTaskdef: &model.SearchNodeRunPb_StatusAndTaskDefPb{
						Status:        model.LHStatusPb(model.LHStatusPb_value[status]),
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

			search = &model.SearchNodeRunPb{
				NoderunCriteria: &model.SearchNodeRunPb_TaskDef{
					TaskDef: &model.SearchNodeRunPb_ByTaskDefPb{
						TaskDefName:   taskDefName,
						EarliestStart: earliestStartTime,
						LatestStart:   latestStartTime,
					},
				},
			}
		} else if wfRunId != "" {
			search = &model.SearchNodeRunPb{
				NoderunCriteria: &model.SearchNodeRunPb_WfRunId{
					WfRunId: wfRunId,
				},
			}
		} else {
			// then it's a userTask search.
			earliest, latest := loadEarliestAndLatestStart(cmd)

			var status *model.UserTaskRunStatusPb = nil

			statusStr, _ := cmd.Flags().GetString("userTaskStatus")
			if statusStr != "" {
				statusVal := model.UserTaskRunStatusPb(model.UserTaskRunStatusPb_value[statusStr])
				status = &statusVal
			}

			var userId *string = nil
			userIdStr, _ := cmd.Flags().GetString("userId")
			if userIdStr != "" {
				userId = &userIdStr
			}

			var userGroup *string = nil
			userGroupStr, _ := cmd.Flags().GetString("userGroup")
			if userGroupStr != "" {
				userGroup = &userGroupStr
			}

			var userTaskDefName *string = nil
			userTaskDefNameStr, _ := cmd.Flags().GetString("userTaskDefName")
			if userTaskDefNameStr != "" {
				userTaskDefName = &userTaskDefNameStr
			}

			if status == nil && userTaskDefName == nil && userGroup == nil && userId == nil {
				log.Fatal("must provide at least one searchable attribute")
			}

			search = &model.SearchNodeRunPb{
				NoderunCriteria: &model.SearchNodeRunPb_UserTaskRun{
					UserTaskRun: &model.SearchNodeRunPb_UserTaskRunSearchPb{
						Status:      status,
						UserId:      userId,
						UserTaskDef: userTaskDefName,
						UserGroup:   userGroup,

						EarliestStart: earliest,
						LatestStart:   latest,
					},
				},
			}
		}
		search.Bookmark = bookmark
		search.Limit = &limit

		common.PrintResp(getGlobalClient(cmd).SearchNodeRun(context.Background(), search))

	},
}

func loadEarliestAndLatestStart(cmd *cobra.Command) (*timestamppb.Timestamp, *timestamppb.Timestamp) {
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

	return earliestStartTime, latestStartTime
}

func init() {
	getCmd.AddCommand(getNodeRunCmd)
	searchCmd.AddCommand(searchNodeRunCmd)
	listCmd.AddCommand(listNodeRunCmd)

	searchNodeRunCmd.Flags().String("wfRunId", "", "WfRunId for which to return all NodeRun id's.")
	searchNodeRunCmd.Flags().String("status", "", "Status of NodeRun's to search for.")
	searchNodeRunCmd.Flags().String("taskDefName", "", "TaskDef ID of NodeRun's to search for.")
	searchNodeRunCmd.Flags().String("userTaskDefName", "", "UserTaskDef ID of User Task Run's to search for.")
	searchNodeRunCmd.Flags().String("userId", "", "Search for User Task Runs assigned to this User ID.")
	searchNodeRunCmd.Flags().String("userGroup", "", "Search for User Task Runs assigned to this User Group.")
	searchNodeRunCmd.Flags().String("userTaskStatus", "", "Status of User Task Runs to search for.")
	searchNodeRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for nodeRuns that started no more than this number of minutes ago")
	searchNodeRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for nodeRuns that started at least this number of minutes ago")

	searchNodeRunCmd.MarkFlagsMutuallyExclusive("status", "wfRunId")
	searchNodeRunCmd.MarkFlagsMutuallyExclusive("wfRunId", "userTaskDefName")
	searchNodeRunCmd.MarkFlagsMutuallyExclusive("wfRunId", "userId")
	searchNodeRunCmd.MarkFlagsMutuallyExclusive("wfRunId", "userGroup")
}
