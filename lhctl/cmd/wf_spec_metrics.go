/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"context"
	"log"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
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
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 4 {
			log.Fatal("Must provide four arguments: wfSpecName, windowType, numWindows, and wfSpecVersion.")
		}

		wfSpecName := args[0]
		wfSpecVersion, err := strconv.Atoi(args[1])
		if err != nil {
			log.Fatal(err)
		}
		windowTypeStr := args[2]
		windowType, isValid := model.MetricsWindowLength_value[windowTypeStr]
		if !isValid {
			log.Fatal("Invalid window type! Supports only 'MINUTES_5', 'HOURS_2', 'DAYS_1'")
		}
		numWindows, err := strconv.Atoi(args[3])
		if err != nil {
			log.Fatal(err)
		}
		ts := timestamppb.Now()

		common.PrintResp(getGlobalClient(cmd).ListWfSpecMetrics(
			context.Background(),
			&model.ListWfMetricsRequest{
				LastWindowStart: ts,
				WindowLength:    model.MetricsWindowLength(windowType),
				WfSpecName:      wfSpecName,
				WfSpecVersion:   int32(wfSpecVersion),
				NumWindows:      int32(numWindows),
			},
		))

	},
}

func init() {
	rootCmd.AddCommand(wfSpecMetrics)
}
