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
	Short: "List metrics for a given WfSpec",
	Long: `List metrics for a given WfSpec.

By default, returns metrics for the last 60 minutes.
You can use --earliestMinutesAgo and --latestMinutesAgo to specify a custom time window.

Examples:
  lhctl list wfMetrics my-workflow
  lhctl list wfMetrics my-workflow 1
  lhctl list wfMetrics my-workflow --latestMinutesAgo 10
  lhctl list wfMetrics my-workflow --earliestMinutesAgo 120 --latestMinutesAgo 60
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

		windowStart, windowEnd := loadEarliestAndLatestStart(cmd)

		if windowStart == nil && windowEnd == nil {
			windowEnd = timestamppb.Now()
			windowStart = timestamppb.New(
				windowEnd.AsTime().Add(-60 * time.Minute),
			)
		} else if windowEnd == nil {
			windowEnd = timestamppb.Now()
		} else if windowStart == nil {
			windowStart = timestamppb.New(
				time.Now().Add(-60 * time.Minute),
			)
		}

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
	listWfMetricsCmd.Flags().Int("earliestMinutesAgo", -1, "Metrics for wfRuns that started no more than this number of minutes ago")
	listWfMetricsCmd.Flags().Int("latestMinutesAgo", -1, "Metrics for wfRuns that started at least this number of minutes ago")
}
