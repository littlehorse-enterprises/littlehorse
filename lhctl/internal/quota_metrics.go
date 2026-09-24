package internal

import (
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var listQuotaMetricsCmd = &cobra.Command{
	Use:   "quotaMetrics <tenantId>",
	Short: "List quota usage metrics for a Tenant",
	Long: `List quota usage metrics for a Tenant.

Use --principal to list usage for a principal-specific quota. By default, returns
metrics for the last 60 minutes. You can use --earliestMinutesAgo and
--latestMinutesAgo to specify a custom time window.

Examples:
  lhctl list quotaMetrics my-tenant
  lhctl list quotaMetrics my-tenant --principal my-principal
  lhctl list quotaMetrics my-tenant --latestMinutesAgo 10
  lhctl list quotaMetrics my-tenant --earliestMinutesAgo 120 --latestMinutesAgo 60
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
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

		req := &lhproto.ListQuotaUsageMetricsRequest{
			QuotaId:     quotaIdFromTenantArg(cmd, args[0]),
			WindowStart: windowStart,
			WindowEnd:   windowEnd,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListQuotaUsageMetrics(
			requestContext(cmd),
			req,
		))
	},
}

func init() {
	listCmd.AddCommand(listQuotaMetricsCmd)
	listQuotaMetricsCmd.Flags().String("principal", "", "List usage for a principal-specific quota")
	listQuotaMetricsCmd.Flags().Int("earliestMinutesAgo", -1, "Quota metrics for requests no more than this number of minutes ago")
	listQuotaMetricsCmd.Flags().Int("latestMinutesAgo", -1, "Quota metrics for requests at least this number of minutes ago")
}
