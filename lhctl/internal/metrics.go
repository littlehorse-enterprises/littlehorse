package internal

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"strings"
)

var putMetricCmd = &cobra.Command{
	Use:   "metric <measurable> <type>",
	Short: "Creates a new metric",
	Run: func(cmd *cobra.Command, args []string) {
		measurable := args[0]
		metricType := args[1]
		putMetricReq := &lhproto.PutMetricRequest{
			Measurable: toMeasurable(measurable),
			Type:       toType(metricType),
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
func init() {
	putCmd.AddCommand(putMetricCmd)
}
