package internal

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/durationpb"
	"strings"
	"time"
)

var putMetricCmd = &cobra.Command{
	Use:   "metric <measurable> <type>",
	Short: "Creates a new metric",
	Run: func(cmd *cobra.Command, args []string) {
		measurable := args[0]
		metricType := args[1]
		duration := args[2]
		windowLength, _ := time.ParseDuration(duration)
		putMetricReq := &lhproto.PutMetricRequest{
			Measurable:   toMeasurable(measurable),
			Type:         toType(metricType),
			WindowLength: durationpb.New(windowLength),
		}

		response, err := getGlobalClient(cmd).PutMetric(requestContext(cmd), putMetricReq)
		littlehorse.PrintResp(response, err)
	},
}

func toMeasurable(measurable string) lhproto.MeasurableObject {
	if strings.ToLower(measurable) == "workflow" {
		return lhproto.MeasurableObject_WORKFLOW
	} else if strings.ToLower(measurable) == "task" {
		return lhproto.MeasurableObject_TASK
	} else {
		panic("Unrecognized measurable " + measurable)
	}
}

func toType(metricType string) lhproto.MetricType {
	if strings.ToLower(metricType) == "avg" {
		return lhproto.MetricType_AVG
	} else if strings.ToLower(metricType) == "count" {
		return lhproto.MetricType_COUNT
	} else if strings.ToLower(metricType) == "ratio" {
		return lhproto.MetricType_RATIO
	} else {
		panic("Unrecognized metric type " + metricType)
	}
}

var listMetricRuns = &cobra.Command{
	Use:   "metricRun <wfRunId>",
	Short: "List all MetricRun's for a given Metric Id.",
	Long:  ``,
	Args:  cobra.ExactArgs(2),
	Run: func(cmd *cobra.Command, args []string) {
		measurable := args[0]
		metricType := args[1]

		metricId := &lhproto.MetricId{
			Measurable: toMeasurable(measurable),
			Type:       toType(metricType),
		}

		req := &lhproto.ListMetricRunRequest{
			MetricId: metricId,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListMetricRuns(
			requestContext(cmd),
			req,
		))
	},
}

func init() {
	putCmd.AddCommand(putMetricCmd)
	listCmd.AddCommand(listMetricRuns)
}
