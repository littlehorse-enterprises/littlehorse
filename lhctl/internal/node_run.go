package internal

import (
	"errors"
	"log"
	"strconv"
	"strings"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

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
	Args: func(cmd *cobra.Command, args []string) error {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 3 {
			needsHelp = true
		}

		if needsHelp {
			return errors.New("must provide 1 or 3 arguments. See 'lhctl get nodeRun -h'")
		}

		return nil
	},
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		trn, err := strconv.Atoi(args[1])
		if err != nil {
			log.Fatal("Couldn't convert threadRunNumber to int.")
		}

		pos, err := strconv.Atoi(args[2])
		if err != nil {
			log.Fatal("Couldn't convert nodeRunPosition to int.")
		}

		littlehorse.PrintResp(getGlobalClient(cmd).GetNodeRun(
			requestContext(cmd),
			&lhproto.NodeRunId{
				WfRunId:         littlehorse.StrToWfRunId(args[0]),
				ThreadRunNumber: int32(trn),
				Position:        int32(pos),
			},
		))
	},
}

var listNodeRunCmd = &cobra.Command{
	Use:   "nodeRun <wfRunId>",
	Short: "List all NodeRun's for a given WfRun Id.",
	Args:  cobra.ExactArgs(1),
	Long: `
Lists all NodeRun's for a given WfRun Id.
`,
	Run: func(cmd *cobra.Command, args []string) {
		threadRunNumber, _ := cmd.Flags().GetInt32("thread-run-number")
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		wfRunIdStr := args[0]

		req := &lhproto.ListNodeRunsRequest{
			WfRunId:  littlehorse.StrToWfRunId(wfRunIdStr),
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if threadRunNumber != -1 {
			req.ThreadRunNumber = &threadRunNumber
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListNodeRuns(
			requestContext(cmd),
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
	Args: cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		nodeTypeStr, statusStr := args[0], args[1]

		nodeTypeInt, ok := lhproto.SearchNodeRunRequest_NodeType_value[nodeTypeStr]
		if !ok {
			log.Fatal("Invalid value for nodeType: " + nodeTypeStr + ". See lhctl search nodeRun --help")
		}

		statusInt, ok := lhproto.TaskStatus_value[statusStr]
		if !ok {
			log.Fatal("Invalid value for status: " + statusStr + ". See lhctl search nodeRun --help")
		}

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")

		earliest, latest := loadEarliestAndLatestStart(cmd)

		search := &lhproto.SearchNodeRunRequest{
			EarliestStart: earliest,
			LatestStart:   latest,
			Bookmark:      bookmark,
			Limit:         &limit,
			NodeType:      lhproto.SearchNodeRunRequest_NodeType(nodeTypeInt),
			Status:        lhproto.LHStatus(statusInt),
		}

		littlehorse.PrintResp(getGlobalClient(cmd).SearchNodeRun(requestContext(cmd), search))

	},
}

func loadEarliestAndLatestStart(cmd *cobra.Command) (*timestamppb.Timestamp, *timestamppb.Timestamp) {
	earliestMinutesAgo, _ := cmd.Flags().GetInt("earliestMinutesAgo")
	latestMinutesAgo, _ := cmd.Flags().GetInt("latestMinutesAgo")
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
	listNodeRunCmd.Flags().Int32("threadRunNumber", -1, "Filter by ThreadRun Number")
}
