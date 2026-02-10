/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
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

var wfSpecMetrics = &cobra.Command{
	Use:   "wfSpecMetrics <wfSpecName> <wfSpecVersion> <windowType> <numWindows>",
	Short: "Get WfSpec Metrics.",
	Long: `You can get two types of metrics: 'task' Metrics, or 'wf' Metrics.

Metrics are aggregated by windows of three types: MINUTES_5, HOURS_2, and DAYS_1.

You can specify the max number of windows "ago" from now you want to get metrics. For
example, to get WfSpec metrics for each 5-minute time window, starting now and going
back an hour ago for WfSpec "my-workflow" version 4321 you can:

lhctl wfSpecMetrics my-workflow 4321 MINUTES_5 12

If you want to get cluster-level WfSpec metrics, you can use CLUSTER_LEVEL_METRIC
as your wfSpecName, and 0 as your wfSpecVersion.
`,
	Args: cobra.ExactArgs(4),
	Run: func(cmd *cobra.Command, args []string) {
		wfSpecName := args[0]
		wfSpecVersion, err := strconv.Atoi(args[1])
		if err != nil {
			log.Fatal(err)
		}
		windowTypeStr := args[2]
		windowType, isValid := lhproto.MetricsWindowLength_value[windowTypeStr]
		if !isValid {
			log.Fatal("Invalid window type! Supports only 'MINUTES_5', 'HOURS_2', 'DAYS_1'")
		}
		numWindows, err := strconv.Atoi(args[3])
		if err != nil {
			log.Fatal(err)
		}

		// Calculate window duration in seconds based on type
		var windowDurationSeconds int64
		switch lhproto.MetricsWindowLength(windowType) {
		case lhproto.MetricsWindowLength_MINUTES_5:
			windowDurationSeconds = 5 * 60
		case lhproto.MetricsWindowLength_HOURS_2:
			windowDurationSeconds = 2 * 60 * 60
		case lhproto.MetricsWindowLength_DAYS_1:
			windowDurationSeconds = 24 * 60 * 60
		default:
			log.Fatal("Invalid window type")
		}

		windowEnd := timestamppb.Now()
		windowStart := timestamppb.New(
			windowEnd.AsTime().Add(-time.Duration(windowDurationSeconds*int64(numWindows)) * time.Second),
		)

		littlehorse.PrintResp(getGlobalClient(cmd).ListWfMetrics(
			requestContext(cmd),
			&lhproto.ListWfMetricsRequest{
				WfSpec: &lhproto.WfSpecId{
					Name:         wfSpecName,
					MajorVersion: int32(wfSpecVersion),
				},
				WindowStart: windowStart,
				WindowEnd:   windowEnd,
			},
		))

	},
}

func init() {
	// Do not add this command until we re-implement metrics:
	rootCmd.AddCommand(wfSpecMetrics)
}
