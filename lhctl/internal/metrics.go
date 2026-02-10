/*
Copyright © 2025 Little Horse Enterprises
*/
package internal

/*
var listMetricsCmd = &cobra.Command{
	Use:   "metrics <wfSpecName>",
	Short: "List aggregated metrics for a workflow.",
	Long: `List all aggregated metrics (windows) for a given WfSpec.

By default, gets metrics from the last 2 hours. You can customize the time range with flags:

Optional flags:
- --startMinutesAgo: How many minutes ago to start from (default: 120 minutes = 2 hours)
- --endMinutesAgo: How many minutes ago to end (default: 0 = now)
- --startTime: Unix timestamp in milliseconds (overrides --startMinutesAgo)
- --endTime: Unix timestamp in milliseconds (overrides --endMinutesAgo)

Examples:
  # Get metrics from the last 2 hours (default)
  lhctl list metrics my-workflow

  # Get metrics from the last 4 hours
  lhctl list metrics my-workflow --startMinutesAgo 240

  # Get metrics from 3 hours ago to 1 hour ago
  lhctl list metrics my-workflow --startMinutesAgo 180 --endMinutesAgo 60

  # Get metrics using Unix timestamps in milliseconds
  lhctl list metrics my-workflow --startTime 1707000000000 --endTime 1707007000000
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfSpecName := args[0]

		// Parse time range from flags
		startMinutesAgo, _ := cmd.Flags().GetInt("startMinutesAgo")
		endMinutesAgo, _ := cmd.Flags().GetInt("endMinutesAgo")
		startTimeMs, _ := cmd.Flags().GetInt64("startTime")
		endTimeMs, _ := cmd.Flags().GetInt64("endTime")

		// Set defaults if not provided
		if startMinutesAgo == 0 && startTimeMs == 0 {
			startMinutesAgo = 120
		}
		if endMinutesAgo == 0 && endTimeMs == 0 {
			endMinutesAgo = 0
		}

		var windowStartTs *timestamppb.Timestamp
		var endTimeTs *timestamppb.Timestamp

		// Parse window start time
		if startTimeMs > 0 {
			// User provided Unix timestamp in milliseconds
			seconds := startTimeMs / 1000
			nanos := (startTimeMs % 1000) * 1000000
			windowStartTs = &timestamppb.Timestamp{
				Seconds: seconds,
				Nanos:   int32(nanos),
			}
		} else {
			// Use minutes ago
			windowStartTs = timestamppb.New(
				time.Now().UTC().Add(-1 * time.Duration(startMinutesAgo) * time.Minute),
			)
		}

		// Parse end time
		if endTimeMs > 0 {
			// User provided Unix timestamp in milliseconds
			seconds := endTimeMs / 1000
			nanos := (endTimeMs % 1000) * 1000000
			endTimeTs = &timestamppb.Timestamp{
				Seconds: seconds,
				Nanos:   int32(nanos),
			}
		} else if endMinutesAgo > 0 {
			endTimeTs = timestamppb.New(
				time.Now().UTC().Add(-1 * time.Duration(endMinutesAgo) * time.Minute),
			)
		}

		req := &lhproto.ListMetricsRequest{
			Id: &lhproto.MetricWindowId{
				Id: &lhproto.MetricWindowId_Workflow{
					Workflow: &lhproto.WorkflowMetricId{
						WfSpec: &lhproto.WfSpecId{
							Name:         wfSpecName,
							MajorVersion: 0,
						},
					},
				},
			},
			WindowStart: windowStartTs,
			WindowEnd:   endTimeTs,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListMetrics(
			requestContext(cmd),
			req,
		))
	},
}
*/

func init() {
	// Disabled: ListMetrics RPC not yet implemented
	// listCmd.AddCommand(listMetricsCmd)

	// listMetricsCmd.Flags().Int("startMinutesAgo", 120, "How many minutes ago to start fetching metrics (default: 120 = last 2 hours)")
	// listMetricsCmd.Flags().Int("endMinutesAgo", 0, "How many minutes ago to end fetching metrics (default: 0 = now)")
	// listMetricsCmd.Flags().Int64("startTime", 0, "Optional Unix timestamp in milliseconds for start time (overrides --startMinutesAgo)")
	// listMetricsCmd.Flags().Int64("endTime", 0, "Optional Unix timestamp in milliseconds for end time (overrides --endMinutesAgo)")
}
