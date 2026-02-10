/*
Copyright © 2024 LittleHorse Enterprises
*/
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

var listWfMetricsCmd = &cobra.Command{
	Use:   "wfMetrics <wfSpecName> [wfSpecVersion]",
	Short: "List WfSpec Metrics for the last 12 five-minute windows",
	Long: `List metrics for a given WfSpec.

By default, returns metrics for the last 12 five-minute windows (60 minutes total).

Examples:
  lhctl list wfMetrics my-workflow
  lhctl list wfMetrics my-workflow 1
`,
	Args: cobra.RangeArgs(1, 2),
	Run: func(cmd *cobra.Command, args []string) {
		wfSpecName := args[0]
		wfSpecVersion := int32(0)

		if len(args) > 1 {
			version, err := strconv.Atoi(args[1])
			if err != nil {
				log.Fatal("Invalid wfSpecVersion: " + err.Error())
			}
			wfSpecVersion = int32(version)
		}

		// Default to last 12 five-minute windows (60 minutes)
		windowDurationSeconds := int64(5 * 60)
		numWindows := int64(12)

		windowEnd := timestamppb.Now()
		windowStart := timestamppb.New(
			windowEnd.AsTime().Add(-time.Duration(windowDurationSeconds*numWindows) * time.Second),
		)

		littlehorse.PrintResp(getGlobalClient(cmd).ListWfMetrics(
			requestContext(cmd),
			&lhproto.ListWfMetricsRequest{
				WfSpec: &lhproto.WfSpecId{
					Name:         wfSpecName,
					MajorVersion: wfSpecVersion,
				},
				WindowStart: windowStart,
				WindowEnd:   windowEnd,
			},
		))
	},
}

func init() {
	listCmd.AddCommand(listWfMetricsCmd)
}
