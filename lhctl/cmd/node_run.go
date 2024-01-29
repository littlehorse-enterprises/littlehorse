package cmd

import (
	"log"
	"strconv"
	"strings"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
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
	- NodeRun Number (i.e. chronological position within the ThreadRun)

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
			requestContext(),
			&model.NodeRunId{
				WfRunId:         common.StrToWfRunId(args[0]),
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
		wfRunIdStr := args[0]

		req := &model.ListNodeRunsRequest{
			WfRunId: common.StrToWfRunId(wfRunIdStr),
		}

		common.PrintResp(getGlobalClient(cmd).ListNodeRuns(
			requestContext(),
			req,
		))
	},
}

var searchNodeRunCmd = &cobra.Command{
	Use:   "nodeRun <node_type> <status>",
	Short: "Search for NodeRun's by providing Node Type and Status",
	Long: `
Search for NodeRun's by providing the type of the Node and the status of the NodeRun.

Returns a list of ObjectId's that can be passed into 'lhctl get nodeRun'. Optionally
provde --earliestMinutesAgo and --latestMinutesAgo to filter by NodeRun creation time.

Valid options for the Node Type:
- TASK
- EXTERNAL_EVENT
- ENTRYPOINT
- EXIT
- START_THREAD
- WAIT_THREADS
- SLEEP
- USER_TASK
- START_MULTIPLE_THREADS

Valid options for Status:
- STARTING
- RUNNING
- COMPLETED
- HALTING
- HALTED
- ERROR
- EXCEPTION

* Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
  options to filter returned NodeRun ID's based on the NodeRun creation
  time.
`,
	Run: func(cmd *cobra.Command, args []string) {

		if len(args) != 2 {
			log.Fatal("Must provide two arguments: Node Type and NodeRun Status")
		}

		nodeTypeStr, statusStr := args[0], args[1]

		nodeTypeInt, ok := model.SearchNodeRunRequest_NodeType_value[nodeTypeStr]
		if !ok {
			log.Fatal("Invalid value for nodeType: " + nodeTypeStr + ". See lhctl search nodeRun --help")
		}

		statusInt, ok := model.TaskStatus_value[statusStr]
		if !ok {
			log.Fatal("Invalid value for status: " + statusStr + ". See lhctl search nodeRun --help")
		}

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		earliest, latest := loadEarliestAndLatestStart(cmd)

		search := &model.SearchNodeRunRequest{
			EarliestStart: earliest,
			LatestStart:   latest,
			Bookmark:      bookmark,
			Limit:         &limit,
			NodeType:      model.SearchNodeRunRequest_NodeType(nodeTypeInt),
			Status:        model.LHStatus(statusInt),
		}

		common.PrintResp(getGlobalClient(cmd).SearchNodeRun(requestContext(), search))

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

	searchNodeRunCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for nodeRuns that started no more than this number of minutes ago")
	searchNodeRunCmd.Flags().Int("latestMinutesAgo", -1, "Search only for nodeRuns that started at least this number of minutes ago")
}
