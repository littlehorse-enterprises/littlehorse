package cmd

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

var getWfRunCmd = &cobra.Command{
	Use:   "wfRun <id>",
	Short: "Get a Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfRun to get.")

		}

		common.PrintResp(getGlobalClient(cmd).GetWfRun(
			requestContext(),
			&model.WfRunId{
				Id: args[0],
			},
		))
	},
}

var searchWfRunCmd = &cobra.Command{
	Use:   "wfRun",
	Short: "Search for WfRuns",
	Long: `
Search for WfRuns. You may provide any of the following option groups:

[wfSpecName, majorVersion, revision, status]
[wfSpecName, status]
[wfSpecName]

  * Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
    options with this group to put a time bound on WfRun's which are returned.
	The time bound applies to the time that the WfRun was created.

Returns a list of ObjectId's that can be passed into 'lhctl get wfRun'.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		wfSpecName, _ := cmd.Flags().GetString("wfSpecName")
		statusRaw, _ := cmd.Flags().GetString("status")
		majorVersionRaw, _ := cmd.Flags().GetInt32("majorVersion")
		revisionRaw, _ := cmd.Flags().GetInt32("revision")

		var majorVersion, revision *int32 = nil, nil
		var status *model.LHStatus

		if majorVersionRaw != -1 {
			majorVersion = &majorVersionRaw
		}
		if revisionRaw != -1 {
			revision = &revisionRaw
		}
		if statusRaw != "" {
			statusTmp := model.LHStatus(model.LHStatus_value[statusRaw])
			status = &statusTmp
		}

		earliest, latest := loadEarliestAndLatestStart(cmd)

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		if wfSpecName == "" {
			log.Fatal("Must specify wfSpecName!")
		}

		search := &model.SearchWfRunRequest{
			Bookmark:      bookmark,
			Limit:         &limit,
			EarliestStart: earliest,
			LatestStart:   latest,
			WfSpecName:    wfSpecName,
			Status:        status,

			WfSpecMajorVersion: majorVersion,
			WfSpecRevision:     revision,
		}

		common.PrintResp(
			getGlobalClient(cmd).SearchWfRun(requestContext(), search),
		)
	},
}

var stopWfRunCmd = &cobra.Command{
	Use:   "wfRun <id>",
	Short: "Stop a Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfRun to stop.")

		}
		trn, _ := cmd.Flags().GetInt32("threadRunNumber")

		common.PrintResp(getGlobalClient(cmd).StopWfRun(
			requestContext(),
			&model.StopWfRunRequest{
				WfRunId:         &model.WfRunId{Id: args[0]},
				ThreadRunNumber: trn,
			},
		))
	},
}

var resumeWfRunCmd = &cobra.Command{
	Use:   "wfRun <id>",
	Short: "Stop a Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfRun to resume.")

		}
		trn, _ := cmd.Flags().GetInt32("threadRunNumber")

		common.PrintResp(getGlobalClient(cmd).ResumeWfRun(
			requestContext(),
			&model.ResumeWfRunRequest{
				WfRunId:         &model.WfRunId{Id: args[0]},
				ThreadRunNumber: trn,
			},
		))
	},
}

var deleteWfRunCmd = &cobra.Command{
	Use:   "wfRun <id>",
	Short: "Delete a Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of WfRun to stop.")

		}

		common.PrintResp(getGlobalClient(cmd).DeleteWfRun(
			requestContext(),
			&model.DeleteWfRunRequest{
				Id: &model.WfRunId{
					Id: args[0],
				},
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
	searchWfRunCmd.Flags().Int32("majorVersion", -1, "WfSpec Major Version to search for")
	searchWfRunCmd.Flags().Int32("revision", -1, "WfSpec Revision to search for")
	searchWfRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for wfRuns that started no more than this number of minutes ago")
	searchWfRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for wfRuns that started at least this number of minutes ago")
	searchWfRunCmd.MarkFlagRequired("wfSpecName")
	searchWfRunCmd.MarkFlagsRequiredTogether("revision", "majorVersion")

	stopWfRunCmd.Flags().Int32("threadRunNumber", 0, "Specific thread run to stop")
	resumeWfRunCmd.Flags().Int32("threadRunNumber", 0, "Specific thread run to stop")
}
