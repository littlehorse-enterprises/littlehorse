package cmd

import (
	"context"
	"log"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var getWfRunCmd = &cobra.Command{
	Use:   "wfRunModel <id>",
	Short: "Get a Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfRun to get.")

		}

		common.PrintResp(getGlobalClient(cmd).GetWfRun(
			context.Background(),
			&model.WfRunIdPb{
				Id: args[0],
			},
		))
	},
}

var searchWfRunCmd = &cobra.Command{
	Use:   "wfRunModel",
	Short: "Search for WfRuns",
	Long: `
Search for WfRuns. You may provide any of the following option groups:

[wfSpecName, wfSpecVersion, status]
[wfSpecName, status]
[wfSpecName]

  * Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
    options with this group to put a time bound on WfRun's which are returned.
	The time bound applies to the time that the WfRun was created.

Returns a list of ObjectId's that can be passed into 'lhctl get wfRunModel'.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		wfSpecName, _ := cmd.Flags().GetString("wfSpecName")
		status, _ := cmd.Flags().GetString("status")
		version, _ := cmd.Flags().GetInt32("wfSpecVersion")

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

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		if wfSpecName == "" {
			log.Fatal("Must specify wfSpecName!")
		}

		search := &model.SearchWfRunPb{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if version != -1 && status != "" {
			search.WfrunCriteria = &model.SearchWfRunPb_StatusAndSpec{
				StatusAndSpec: &model.SearchWfRunPb_StatusAndSpecPb{
					Status:        model.LHStatusPb(model.LHStatusPb_value[status]),
					WfSpecName:    wfSpecName,
					WfSpecVersion: version,
					LatestStart:   latestStartTime,
					EarliestStart: earliestStartTime,
				},
			}
		} else if status != "" {
			// TODO: Eventually we need to validate the status
			search.WfrunCriteria = &model.SearchWfRunPb_StatusAndName{
				StatusAndName: &model.SearchWfRunPb_StatusAndNamePb{
					WfSpecName: wfSpecName,
					Status:     model.LHStatusPb(model.LHStatusPb_value[status]),
				},
			}
		} else {
			if version != -1 {
				log.Fatal("--wfSpecVersion provided without --status")
			}
			search.WfrunCriteria = &model.SearchWfRunPb_Name{
				Name: &model.SearchWfRunPb_NamePb{
					WfSpecName: wfSpecName,
				},
			}
		}

		common.PrintResp(
			getGlobalClient(cmd).SearchWfRun(context.Background(), search),
		)
	},
}

var stopWfRunCmd = &cobra.Command{
	Use:   "wfRunModel <id>",
	Short: "Stop a Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfRun to stop.")

		}
		trn, _ := cmd.Flags().GetInt32("threadRunNumber")

		common.PrintResp(getGlobalClient(cmd).StopWfRun(
			context.Background(),
			&model.StopWfRunPb{
				WfRunId:         args[0],
				ThreadRunNumber: trn,
			},
		))
	},
}

var resumeWfRunCmd = &cobra.Command{
	Use:   "wfRunModel <id>",
	Short: "Stop a Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfRun to resume.")

		}
		trn, _ := cmd.Flags().GetInt32("threadRunNumber")

		common.PrintResp(getGlobalClient(cmd).ResumeWfRun(
			context.Background(),
			&model.ResumeWfRunPb{
				WfRunId:         args[0],
				ThreadRunNumber: trn,
			},
		))
	},
}

var deleteWfRunCmd = &cobra.Command{
	Use:   "wfRunModel <id>",
	Short: "Delete a Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfRun to stop.")

		}

		common.PrintResp(getGlobalClient(cmd).DeleteWfRun(
			context.Background(),
			&model.DeleteWfRunPb{
				WfRunId: args[0],
			},
		))
	},
}

func init() {
	getCmd.AddCommand(getWfRunCmd)
	searchCmd.AddCommand(searchWfRunCmd)
	stopCmd.AddCommand(stopWfRunCmd)
	resumeCmd.AddCommand(resumeWfRunCmd)
	deleteCmd.AddCommand(deleteWfRunCmd)

	searchWfRunCmd.Flags().String("status", "", "Status of WfRuns to search for")
	searchWfRunCmd.Flags().String("wfSpecName", "", "wfSpecName to search for")
	searchWfRunCmd.Flags().Int32("wfSpecVersion", -1, "wfSpecVersion to search for")
	searchWfRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for wfRuns that started no more than this number of minutes ago")
	searchWfRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for wfRuns that started at least this number of minutes ago")
	searchWfRunCmd.MarkFlagRequired("wfSpecName")

	stopWfRunCmd.Flags().Int32("threadRunNumber", 0, "Specific thread run to stop")
	resumeWfRunCmd.Flags().Int32("threadRunNumber", 0, "Specific thread run to stop")
}
