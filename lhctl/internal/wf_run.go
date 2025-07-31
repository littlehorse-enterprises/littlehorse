package internal

import (
	"log"
	"strconv"
	"strings"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

var getWfRunCmd = &cobra.Command{
	Use:   "wfRun <id>",
	Short: "Get a Workflow Run.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).GetWfRun(
			requestContext(cmd),
			littlehorse.StrToWfRunId(args[0]),
		))
	},
}

var getScheduledWfRun = &cobra.Command{
	Use:   "scheduled <id>",
	Short: "Get a scheduled run.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).GetScheduledWfRun(
			requestContext(cmd),
			&lhproto.ScheduledWfRunId{
				Id: args[0],
			},
		))
	},
}

var searchWfRunCmd = &cobra.Command{
	Use:   "wfRun [<wfSpecName>] [<majorVersion>] [<revision>]",
	Short: "Search for WfRuns",
	Long: `
Search for WfRuns. You may provide the optional arguments:
- [<wfSpecName>] (required unless --parentWfRunId is specified)
- [<majorVersion>]
- [<revision>]

And the optional flags:
- [--status]
- [--parentWfRunId]

  * Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
		  flags to put a time bound on WfRun's which are returned.
		  The time bound applies to the time that the WfRun was created.

Returns a list of ObjectId's that can be passed into 'lhctl get wfRun'.
	`,
	Args: cobra.RangeArgs(0, 3),
	Run: func(cmd *cobra.Command, args []string) {
		var wfSpecName string
		if len(args) > 0 {
			wfSpecName = args[0]
		}

		statusRaw, _ := cmd.Flags().GetString("status")
		var status *lhproto.LHStatus
		var majorVersion *int32 = nil
		var revision *int32 = nil

		if len(args) > 1 {
			majorVersionInt, err := strconv.Atoi(args[1])
			if err != nil {
				log.Fatal("Couldn't convert majorVersion to int:\n", err)
			}
			val := int32(majorVersionInt)
			majorVersion = &val
		}

		if len(args) > 2 {
			revisionInt, err := strconv.Atoi(args[2])
			if err != nil {
				log.Fatal("Couldn't convert revision to int:\n", err)
			}
			val := int32(revisionInt)
			revision = &val
		}

		if statusRaw != "" {
			statusTmp := lhproto.LHStatus(lhproto.LHStatus_value[statusRaw])
			status = &statusTmp
		}

		earliest, latest := loadEarliestAndLatestStart(cmd)

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		parentId, _ := cmd.Flags().GetString("parentWfRunId")
		showFullTree, _ := cmd.Flags().GetBool("show-full-tree")
		levels, _ := cmd.Flags().GetInt("levels")

		if levels != -1 {
			if !showFullTree {
				log.Fatal("--levels flag can only be used with --show-full-tree")
			}
			if levels <= 0 {
				log.Fatal("--levels flag must be positive")
			}
		}

		if wfSpecName == "" && parentId == "" {
			log.Fatal("Must specify either wfSpecName or --parentWfRunId")
		}

		search := &lhproto.SearchWfRunRequest{
			Bookmark:           bookmark,
			Limit:              &limit,
			EarliestStart:      earliest,
			LatestStart:        latest,
			Status:             status,
			WfSpecMajorVersion: majorVersion,
			WfSpecRevision:     revision,
		}

		if wfSpecName != "" {
			search.WfSpecName = wfSpecName
		}

		if parentId != "" {
			search.ParentWfRunId = littlehorse.StrToWfRunId(parentId)
			if showFullTree {
				search.ShowFullTree = &showFullTree
			}
		}

		resp, err := getGlobalClient(cmd).SearchWfRun(requestContext(cmd), search)
		if err != nil {
			log.Fatal("Failed to search WfRuns:", err)
		}

		if parentId != "" && showFullTree && levels != -1 {
			resp = filterWfRunsByLevels(resp, parentId, levels)
		}

		littlehorse.PrintResp(resp, err)
	},
}

var stopWfRunCmd = &cobra.Command{
	Use:   "wfRun <id>",
	Short: "Stop a Workflow Run.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId := args[0]
		trn, _ := cmd.Flags().GetInt32("threadRunNumber")

		littlehorse.PrintResp(getGlobalClient(cmd).StopWfRun(
			requestContext(cmd),
			&lhproto.StopWfRunRequest{
				WfRunId:         littlehorse.StrToWfRunId(wfRunId),
				ThreadRunNumber: trn,
			},
		))
	},
}

var resumeWfRunCmd = &cobra.Command{
	Use:   "wfRun <id>",
	Short: "Stop a Workflow Run.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId := args[0]
		trn, _ := cmd.Flags().GetInt32("threadRunNumber")

		littlehorse.PrintResp(getGlobalClient(cmd).ResumeWfRun(
			requestContext(cmd),
			&lhproto.ResumeWfRunRequest{
				WfRunId:         littlehorse.StrToWfRunId(wfRunId),
				ThreadRunNumber: trn,
			},
		))
	},
}

var deleteWfRunCmd = &cobra.Command{
	Use:   "wfRun <id>",
	Short: "Delete a Workflow Run.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId := args[0]
		littlehorse.PrintResp(getGlobalClient(cmd).DeleteWfRun(
			requestContext(cmd),
			&lhproto.DeleteWfRunRequest{
				Id: &lhproto.WfRunId{
					Id: wfRunId,
				},
			},
		))
	},
}

var deleteScheduledWfRun = &cobra.Command{
	Use:   "schedule <id>",
	Short: "Delete a Scheduled Workflow Run.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId := args[0]
		littlehorse.PrintResp(getGlobalClient(cmd).DeleteScheduledWfRun(
			requestContext(cmd),
			&lhproto.DeleteScheduledWfRunRequest{
				Id: &lhproto.ScheduledWfRunId{
					Id: wfRunId,
				},
			},
		))
	},
}

var scheduleWfCmd = &cobra.Command{
	Use:   "run <cronExpression> <wfSpecName> <<var1 name>> <<var1 val>>...",
	Short: "Run an instance of a WfSpec with provided Name and Input Variables.",
	Args:  cobra.MinimumNArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		scheduleWfReq := &lhproto.ScheduleWfRequest{}

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
			parentId := &lhproto.WfRunId{}
			parentId.Id = parentWfRunId
			scheduleWfReq.ParentWfRunId = parentId
		}

		// Now parse variables
		if len(args) > 2 {
			if len(args)%2 == 1 {
				log.Fatal(`
If you provide variables, you must provide pairs of <name, val>.
Therefore, you must have an even number of args after the WfSpec Id, for an
odd total number of args. See 'lhctl run --help' for details.`)
			}

			// Now, we need to look up the wfSpec and serialize the variables.
			var wfSpec *lhproto.WfSpec
			var err error

			if revision == nil {
				wfSpec, err = getGlobalClient(cmd).GetLatestWfSpec(
					requestContext(cmd),
					&lhproto.GetLatestWfSpecRequest{
						Name:         wfSpecName,
						MajorVersion: majorVersion,
					},
				)
			} else {
				wfSpec, err = getGlobalClient(cmd).GetWfSpec(
					requestContext(cmd),
					&lhproto.WfSpecId{
						Name:         wfSpecName,
						MajorVersion: *majorVersion,
						Revision:     *revision,
					})
			}

			if err != nil {
				log.Fatal("Unable to find WfSpec: " + err.Error())
			}

			scheduleWfReq.Variables = make(map[string]*lhproto.VariableValue)
			varDefs := littlehorse.GetInputVarDefs(wfSpec)

			for i := 2; i+1 < len(args); i += 2 {
				varName := args[i]
				varValStr := args[i+1]

				varDef := varDefs[varName]
				if varDef == nil {
					log.Fatal("Variable name '" + varName + "' not found in WfSpec.")
				}

				scheduleWfReq.Variables[varName], err = littlehorse.StrToVarVal(
					varValStr, varDef.TypeDef.Type,
				)

				if err != nil {
					log.Fatal("Failed converting variable: " + err.Error())
				}
			}
		}

		// At this point, we've loaded everything up, time to fire away.
		littlehorse.PrintResp(getGlobalClient(cmd).ScheduleWf(requestContext(cmd), scheduleWfReq))
	},
}

var searchScheduledWfsCmd = &cobra.Command{
	Use:   "schedule <wfSpecName> [<majorVersion>] [<revision>]",
	Short: "List all scheduled wf runs for a given wf spec",
	Args:  cobra.MinimumNArgs(1),
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
		wfSpecName := args[0]
		req := &lhproto.SearchScheduledWfRunRequest{
			WfSpecName:   wfSpecName,
			MajorVersion: majorVersion,
			Revision:     revision,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).SearchScheduledWfRun(
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
	searchWfRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for wfRuns that started no more than this number of minutes ago")
	searchWfRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for wfRuns that started at least this number of minutes ago")
	searchWfRunCmd.Flags().String("parentWfRunId", "", "Filter child WfRuns by parent WfRun ID")
	searchWfRunCmd.Flags().Bool("show-full-tree", false, "Use full object scan instead of index-based search for complete tree discovery")
	searchWfRunCmd.Flags().Int("levels", -1, "Limit search depth when using --show-full-tree (only applicable with --show-full-tree)")

	scheduleWfCmd.Flags().Int32("majorVersion", -1, "WfSpec Major Version to search for")
	scheduleWfCmd.Flags().Int32("revision", -1, "WfSpec Revision to search for")
	scheduleWfCmd.Flags().String("id", "", "")

	stopWfRunCmd.Flags().Int32("threadRunNumber", 0, "Specific thread run to stop")
	resumeWfRunCmd.Flags().Int32("threadRunNumber", 0, "Specific thread run to stop")
}

func filterWfRunsByLevels(resp *lhproto.WfRunIdList, rootParentId string, maxLevels int) *lhproto.WfRunIdList {
	if maxLevels <= 0 {
		return &lhproto.WfRunIdList{Results: []*lhproto.WfRunId{}}
	}

	filteredTree := &lhproto.WfRunIdList{Results: []*lhproto.WfRunId{}}

	for _, wfRun := range resp.Results {
		depth := calculateDepthFromRoot(wfRun, rootParentId)
		if depth > 0 && depth <= maxLevels {
			filteredTree.Results = append(filteredTree.Results, wfRun)
		}
	}

	return filteredTree
}

func calculateDepthFromRoot(wfRun *lhproto.WfRunId, rootParentId string) int {
	if wfRun.ParentWfRunId == nil {
		return 0
	}

	currentParentId := rootParentId
	if separatorIndex := strings.LastIndex(rootParentId, "_"); separatorIndex != -1 {
		currentParentId = rootParentId[separatorIndex+1:]
	}

	depth := 1
	current := wfRun.ParentWfRunId

	for current != nil {
		if current.Id == currentParentId {
			return depth
		}
		depth++
		current = current.ParentWfRunId
	}

	return 0
}
