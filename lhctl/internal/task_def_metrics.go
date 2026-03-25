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
	Use:   "taskMetrics [taskDefName]",
	Short: "List metrics for a TaskDef (omit name to list all)",
	Long: `List metrics for a TaskDef. Omitting the name lists metrics for all TaskDefs.

By default, returns metrics for the last 60 minutes.
You can use --earliestMinutesAgo and --latestMinutesAgo to specify a custom time window.

Examples:
  lhctl list taskMetrics
  lhctl list taskMetrics my-task
  lhctl list taskMetrics my-task --latestMinutesAgo 10
  lhctl list taskMetrics my-task --earliestMinutesAgo 120 --latestMinutesAgo 60
`,
	Args: cobra.RangeArgs(0, 1),
	Run: func(cmd *cobra.Command, args []string) {
		taskDefName := ""
		if len(args) > 0 {
			taskDefName = args[0]
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

		req := &lhproto.ListTaskMetricsRequest{
			WindowStart: windowStart,
			WindowEnd:   windowEnd,
		}

		if taskDefName != "" {
			req.TaskDef = &lhproto.TaskDefId{Name: taskDefName}
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListTaskMetrics(
			requestContext(cmd),
			req,
		))
	},
}

func init() {
	listCmd.AddCommand(listTaskMetricsCmd)
	listTaskMetricsCmd.Flags().Int("earliestMinutesAgo", -1, "Metrics for tasks that started no more than this number of minutes ago")
	listTaskMetricsCmd.Flags().Int("latestMinutesAgo", -1, "Metrics for tasks that started at least this number of minutes ago")
}
