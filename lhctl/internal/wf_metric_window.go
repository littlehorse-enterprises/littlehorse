package internal

import (
	"log"
	"strconv"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var getMetricWindowCmd = &cobra.Command{
	Use:   "wfMetricWindow <wfSpecName> <majorVersion> <revision> <windowStartMs>",
	Short: "Get a MetricWindow by its ID.",
	Long: `Get a MetricWindow by its full ID.

Arguments:
  <wfSpecName>     - Name of the WfSpec.
  <majorVersion>   - Major version of the WfSpec.
  <revision>       - Revision of the WfSpec.
  <windowStartMs>  - Window start time in milliseconds since epoch.
`,
	Args: cobra.ExactArgs(4),
	Run: func(cmd *cobra.Command, args []string) {
		majorVersion, err := strconv.Atoi(args[1])
		if err != nil {
			log.Fatal("Invalid majorVersion: ", err)
		}
		revision, err := strconv.Atoi(args[2])
		if err != nil {
			log.Fatal("Invalid revision: ", err)
		}
		windowStartMs, err := strconv.ParseInt(args[3], 10, 64)
		if err != nil {
			log.Fatal("Invalid windowStartMs: ", err)
		}

		windowStart := timestamppb.New(time.UnixMilli(windowStartMs))

		req := &lhproto.MetricWindowId{
			Id: &lhproto.MetricWindowId_WfSpecId{
				WfSpecId: &lhproto.WfSpecId{
					Name:         args[0],
					MajorVersion: int32(majorVersion),
					Revision:     int32(revision),
				},
			},
			WindowStart: windowStart,
		}

		littlehorse.PrintResp(
			getGlobalClient(cmd).GetMetricWindow(requestContext(cmd), req),
		)
	},
}

var searchWfMetricWindowCmd = &cobra.Command{
	Use:   "wfMetricWindow <wfSpecName>",
	Short: "Search for workflow metric windows by WfSpec name.",
	Long: `Search for MetricWindow IDs for a given WfSpec name.

Required arguments:
  <wfSpecName> - Name of the WfSpec to search metric windows for.

Optional flags:
  --earliestMinutesAgo  Return windows whose start time is no earlier than this many minutes ago.
  --latestMinutesAgo    Return windows whose start time is no later than this many minutes ago.
  --latestOnly          Return only the single most recent window.

Returns a list of MetricWindowId's.
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfSpecName := args[0]
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		latestOnly, _ := cmd.Flags().GetBool("latestOnly")

		req := &lhproto.SearchWfMetricWindowRequest{
			WfSpecName: wfSpecName,
			Bookmark:   bookmark,
			Limit:      &limit,
		}

		if latestOnly {
			req.LatestOnly = &latestOnly
		} else {
			req.EarliestStart, req.LatestStart = loadEarliestAndLatestStart(cmd)
		}

		resp, err := getGlobalClient(cmd).SearchWfMetricWindow(requestContext(cmd), req)
		if err != nil {
			log.Fatal("Failed to search WfMetric windows: ", err)
		}

		littlehorse.PrintResp(resp, err)
	},
}

func init() {
	getCmd.AddCommand(getMetricWindowCmd)
	searchCmd.AddCommand(searchWfMetricWindowCmd)
	searchWfMetricWindowCmd.Flags().Int("earliestMinutesAgo", -1, "Return windows starting no earlier than this many minutes ago")
	searchWfMetricWindowCmd.Flags().Int("latestMinutesAgo", -1, "Return windows starting no later than this many minutes ago")
	searchWfMetricWindowCmd.Flags().Bool("latestOnly", false, "Return only the single most recent window")
}
