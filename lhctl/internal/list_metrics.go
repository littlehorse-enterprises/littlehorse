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
Use --minutes to specify a different time window.

Examples:
  lhctl list wfMetrics my-workflow
  lhctl list wfMetrics my-workflow 1
  lhctl list wfMetrics my-workflow --minutes 1
  lhctl list wfMetrics my-workflow 0 --minutes 5
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

		// Get minutes from flag, default to 60
		minutes, _ := cmd.Flags().GetInt("minutes")

		windowEnd := timestamppb.Now()
		windowStart := timestamppb.New(
			windowEnd.AsTime().Add(-time.Duration(minutes) * time.Minute),
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
	listWfMetricsCmd.Flags().Int("minutes", 60, "Number of minutes to look back for metrics")
}
