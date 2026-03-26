/*
Copyright © 2024 LittleHorse Enterprises
*/
package internal

import (
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var listTaskMetricsCmd = &cobra.Command{
	Use:   "taskMetrics <taskDefName>",
	Short: "List metrics for a given TaskDef",
	Long: `List metrics for a given TaskDef.

By default, returns metrics for the last 60 minutes.
You can use --earliestMinutesAgo and --latestMinutesAgo to specify a custom time window.

Examples:
  lhctl list taskMetrics my-task
  lhctl list taskMetrics my-task --latestMinutesAgo 10
  lhctl list taskMetrics my-task --earliestMinutesAgo 120 --latestMinutesAgo 60
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		taskDefName := args[0]

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

		littlehorse.PrintResp(getGlobalClient(cmd).ListTaskMetrics(
			requestContext(cmd),
			&lhproto.ListTaskMetricsRequest{
				TaskDef:     &lhproto.TaskDefId{Name: taskDefName},
				WindowStart: windowStart,
				WindowEnd:   windowEnd,
			},
		))
	},
}

func init() {
	listCmd.AddCommand(listTaskMetricsCmd)
	listTaskMetricsCmd.Flags().Int("earliestMinutesAgo", -1, "Metrics for tasks that started no more than this number of minutes ago")
	listTaskMetricsCmd.Flags().Int("latestMinutesAgo", -1, "Metrics for tasks that started at least this number of minutes ago")
}
