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
			requestContext(cmd),
			common.StrToWfRunId(args[0]),
		))
	},
}

var getScheduledWfRun = &cobra.Command{
	Use:   "scheduled <id>",
	Short: "Get a scheduled run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of ScheduledWfRun to get.")

		}

		common.PrintResp(getGlobalClient(cmd).GetScheduledWf(
			requestContext(cmd),
			&model.ScheduledWfRunId{
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
[wfSpecName, majorVersion, revision]
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
			getGlobalClient(cmd).SearchWfRun(requestContext(cmd), search),
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
			requestContext(cmd),
			&model.StopWfRunRequest{
				WfRunId:         common.StrToWfRunId(args[0]),
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
			requestContext(cmd),
			&model.ResumeWfRunRequest{
				WfRunId:         common.StrToWfRunId(args[0]),
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
			log.Fatal("You must provide one argument: the ID of WfRun to delete.")

		}

		common.PrintResp(getGlobalClient(cmd).DeleteWfRun(
			requestContext(cmd),
			&model.DeleteWfRunRequest{
				Id: &model.WfRunId{
					Id: args[0],
				},
			},
		))
	},
}

var deleteScheduledWfRun = &cobra.Command{
	Use:   "schedule <id>",
	Short: "Delete a Scheduled Workflow Run.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument: the ID of ScheduledWfRun to delete.")
		}

		common.PrintResp(getGlobalClient(cmd).DeleteScheduledWfRun(
			requestContext(cmd),
			&model.DeleteScheduledWfRunRequest{
				Id: &model.ScheduledWfRunId{
					Id: args[0],
				},
			},
		))
	},
}

var scheduleWfCmd = &cobra.Command{
	Use:   "run <cronExpression> <wfSpecName> <<var1 name>> <<var1 val>>...",
	Short: "Run an instance of a WfSpec with provided Name and Input Variables.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) < 2 {
			log.Fatal("You must provide at least 2 arg: Cron expression and WfSpec name.")
		}

		scheduleWfReq := &model.ScheduleWfRequest{}

		cronExpression := args[0]
		wfSpecName := args[1]
		scheduleWfReq.WfSpecName = wfSpecName

		var majorVersion *int32
		if raw, _ := cmd.Flags().GetInt32("majorVersion"); raw == -1 {
			majorVersion = nil
		} else {
			majorVersion = &raw
		}
		scheduleWfReq.MajorVersion = majorVersion
		scheduleWfReq.CronExpression = cronExpression

		var revision *int32
		if raw, _ := cmd.Flags().GetInt32("revision"); raw == -1 {
			revision = nil
		} else {
			revision = &raw
		}
		scheduleWfReq.Revision = revision

		id, _ := cmd.Flags().GetString("id")
		if id != "" {
			scheduleWfReq.Id = &id
		}

		parentWfRunId, _ := cmd.Flags().GetString("parentWfRunId")
		if parentWfRunId != "" {
			parentId := &model.WfRunId{}
			parentId.Id = parentWfRunId
			scheduleWfReq.ParentWfRunId = parentId
		}

		// Now parse variables
		if len(args) > 2 {
			if len(args)%2 != 1 {
				log.Fatal(`
If you provide variables, you must provide pairs of <name, val>.
Therefore, you must have an even number of args after the WfSpec Id, for an
odd total number of args. See 'lhctl run --help' for details.`)
			}

			// Now, we need to look up the wfSpec and serialize the variables.
			var wfSpec *model.WfSpec
			var err error

			if revision == nil {
				wfSpec, err = getGlobalClient(cmd).GetLatestWfSpec(
					requestContext(cmd),
					&model.GetLatestWfSpecRequest{
						Name:         args[0],
						MajorVersion: majorVersion,
					},
				)
			} else {
				wfSpec, err = getGlobalClient(cmd).GetWfSpec(
					requestContext(cmd),
					&model.WfSpecId{
						Name:         args[0],
						MajorVersion: *majorVersion,
						Revision:     *revision,
					})
			}

			if err != nil {
				log.Fatal("Unable to find WfSpec: " + err.Error())
			}

			scheduleWfReq.Variables = make(map[string]*model.VariableValue)
			varDefs := common.GetInputVarDefs(wfSpec)

			for i := 1; i+1 < len(args); i += 2 {
				varName := args[i]
				varValStr := args[i+1]

				varDef := varDefs[varName]
				if varDef == nil {
					log.Fatal("Variable name '" + varName + "' not found in WfSpec.")
				}

				scheduleWfReq.Variables[varName], err = common.StrToVarVal(
					varValStr, varDef.Type,
				)

				if err != nil {
					log.Fatal("Failed converting variable: " + err.Error())
				}
			}
		}

		// At this point, we've loaded everything up, time to fire away.
		common.PrintResp(getGlobalClient(cmd).ScheduleWf(requestContext(cmd), scheduleWfReq))
	},
}

var searchScheduledWfsCmd = &cobra.Command{
	Use:   "schedule <wfSpecName> <majorVersion> <revision>",
	Short: "List all scheduled wf runs for a given wf spec",
	Run: func(cmd *cobra.Command, args []string) {

		var majorVersion *int32
		if raw, _ := cmd.Flags().GetInt32("majorVersion"); raw == -1 {
			majorVersion = nil
		} else {
			majorVersion = &raw
		}

		var revision *int32
		if raw, _ := cmd.Flags().GetInt32("revision"); raw == -1 {
			revision = nil
		} else {
			revision = &raw
		}
		wfSpecName, _ := cmd.Flags().GetString("wfSpecName")
		req := &model.SearchScheduledWfRunsRequest{
			WfSpecName:   wfSpecName,
			MajorVersion: majorVersion,
			Revision:     revision,
		}

		common.PrintResp(getGlobalClient(cmd).SearchScheduledWf(
			requestContext(cmd),
			req,
		))
	},
}

func init() {
	getCmd.AddCommand(getWfRunCmd)
	getCmd.AddCommand(getScheduledWfRun)
	searchCmd.AddCommand(searchWfRunCmd)
	stopCmd.AddCommand(stopWfRunCmd)
	resumeCmd.AddCommand(resumeWfRunCmd)
	deleteCmd.AddCommand(deleteWfRunCmd)
	deleteCmd.AddCommand(deleteScheduledWfRun)
	scheduleCmd.AddCommand(scheduleWfCmd)
	searchCmd.AddCommand(searchScheduledWfsCmd)

	searchWfRunCmd.Flags().String("status", "", "Status of WfRuns to search for")
	searchWfRunCmd.Flags().String("wfSpecName", "", "wfSpecName to search for")
	searchWfRunCmd.Flags().Int32("majorVersion", -1, "WfSpec Major Version to search for")
	searchWfRunCmd.Flags().Int32("revision", -1, "WfSpec Revision to search for")
	searchWfRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for wfRuns that started no more than this number of minutes ago")
	searchWfRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for wfRuns that started at least this number of minutes ago")
	searchWfRunCmd.MarkFlagRequired("wfSpecName")
	searchWfRunCmd.MarkFlagsRequiredTogether("revision", "majorVersion")
	searchScheduledWfsCmd.Flags().String("wfSpecName", "", "wfSpecName to search for")
	searchScheduledWfsCmd.Flags().Int32("majorVersion", -1, "WfSpec Major Version to search for")
	searchScheduledWfsCmd.Flags().Int32("revision", -1, "WfSpec Revision to search for")

	scheduleWfCmd.Flags().String("wfSpecName", "", "wfSpecName to search for")
	scheduleWfCmd.Flags().Int32("majorVersion", -1, "WfSpec Major Version to search for")
	scheduleWfCmd.Flags().Int32("revision", -1, "WfSpec Revision to search for")
	scheduleWfCmd.Flags().String("id", "", "")

	stopWfRunCmd.Flags().Int32("threadRunNumber", 0, "Specific thread run to stop")
	resumeWfRunCmd.Flags().Int32("threadRunNumber", 0, "Specific thread run to stop")
}
