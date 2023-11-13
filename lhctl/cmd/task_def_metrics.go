/*
Copyright © 2022 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"log"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var taskDefMetrics = &cobra.Command{
	Use:   "taskDefMetrics <taskDefName> <windowType> <numWindows>",
	Short: "Get TaskDef Metrics.",
	Long: `Get TaskDef Metrics for a specified TaskDef and a specified time range.

Metrics are aggregated by windows of three types: MINUTES_5, HOURS_2, and DAYS_1.

You can specify the max number of windows "ago" from now you want to get metrics. For
example, to get TaskDef metrics for each 5-minute time window, starting now and going
back an hour ago for TaskDef "my-task" you can:

lhctl taskDefMetrics my-task MINUTES_5 12

If you want to get taskDef Metrics at the cluster level, use CLUSTER_LEVEL_METRIC as your
taskDefName.
`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 3 {
			log.Fatal("Must provide three arguments: taskDefName, windowType, and numWindows.")
		}

		taskDefName := args[0]
		windowTypeStr := args[1]
		windowType, isValid := model.MetricsWindowLength_value[windowTypeStr]
		if !isValid {
			log.Fatal("Invalid window type! Supports only 'MINUTES_5', 'HOURS_2', 'DAYS_1'")
		}
		numWindows, err := strconv.Atoi(args[2])
		if err != nil {
			log.Fatal(err)
		}
		ts := timestamppb.Now()

		common.PrintResp(getGlobalClient(cmd).ListTaskDefMetrics(
			requestContext(),
			&model.ListTaskMetricsRequest{
				LastWindowStart: ts,
				WindowLength:    model.MetricsWindowLength(windowType),
				TaskDefName:     taskDefName,
				NumWindows:      int32(numWindows),
			},
		))
	},
}

func init() {
	rootCmd.AddCommand(taskDefMetrics)
}
