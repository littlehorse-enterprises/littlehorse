package internal

import (
	"strings"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/durationpb"
)

var putMetricCmd = &cobra.Command{
	Use:   "metric <measurable> <type>",
	Short: "Creates a new metric",
	Run: func(cmd *cobra.Command, args []string) {
		metricType := args[0]
		duration := args[1]
		windowLength, _ := time.ParseDuration(duration)
		defaultNodeType := "TaskNode"
		putMetricReq := &lhproto.PutMetricSpecRequest{
			AggregationType: toType(metricType),
			WindowLength:    durationpb.New(windowLength),
		}
		putMetricReq.Reference = &lhproto.PutMetricSpecRequest_Node{
			Node: &lhproto.NodeReference{
				NodeType: &defaultNodeType,
			},
		}

		response, err := getGlobalClient(cmd).PutMetricSpec(requestContext(cmd), putMetricReq)
		littlehorse.PrintResp(response, err)
	},
}

func toType(metricType string) lhproto.AggregationType {
	if strings.ToLower(metricType) == "avg" {
		return lhproto.AggregationType_AVG
	} else if strings.ToLower(metricType) == "count" {
		return lhproto.AggregationType_COUNT
	} else if strings.ToLower(metricType) == "ratio" {
		return lhproto.AggregationType_RATIO
	} else if strings.ToLower(metricType) == "latency" {
		return lhproto.AggregationType_LATENCY
	} else {
		panic("Unrecognized metric type " + metricType)
	}
}

func init() {
	putCmd.AddCommand(putMetricCmd)
}
