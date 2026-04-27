package internal

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
)

func quotaIdFromTenantArg(cmd *cobra.Command, tenantId string) *lhproto.QuotaId {
	principal, _ := cmd.Flags().GetString("principal")
	quotaId := &lhproto.QuotaId{
		Tenant: &lhproto.TenantId{Id: tenantId},
	}
	if principal != "" {
		quotaId.Principal = &lhproto.PrincipalId{Id: principal}
	}
	return quotaId
}

var putQuotaCmd = &cobra.Command{
	Use:   "quota <tenantId>",
	Short: "Create or update a Quota.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		writeRequestsPerSecond, _ := cmd.Flags().GetInt32("writeRequestsPerSecond")
		quotaId := quotaIdFromTenantArg(cmd, args[0])
		putQuotaReq := &lhproto.PutQuotaRequest{
			Tenant:                 quotaId.Tenant,
			Principal:              quotaId.Principal,
			WriteRequestsPerSecond: writeRequestsPerSecond,
		}

		littlehorse.PrintResp(getGlobalClient(cmd).PutQuota(requestContext(cmd), putQuotaReq))
	},
}

var getQuotaCmd = &cobra.Command{
	Use:   "quota <tenantId>",
	Short: "Get a Quota",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).GetQuota(
			requestContext(cmd),
			quotaIdFromTenantArg(cmd, args[0]),
		))
	},
}

var searchQuotaCmd = &cobra.Command{
	Use:   "quota",
	Short: "Search for Quotas",
	Args:  cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		tenantId, _ := cmd.Flags().GetString("tenantId")
		principal, _ := cmd.Flags().GetString("principal")

		search := &lhproto.SearchQuotaRequest{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if tenantId != "" {
			search.TenantId = &tenantId
		}
		if principal != "" {
			search.Principal = &lhproto.PrincipalId{Id: principal}
		}

		littlehorse.PrintResp(getGlobalClient(cmd).SearchQuota(requestContext(cmd), search))
	},
}

var deleteQuotaCmd = &cobra.Command{
	Use:   "quota <tenantId>",
	Short: "Delete a Quota.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).DeleteQuota(
			requestContext(cmd),
			&lhproto.DeleteQuotaRequest{Id: quotaIdFromTenantArg(cmd, args[0])},
		))
	},
}

func init() {
	putCmd.AddCommand(putQuotaCmd)
	putQuotaCmd.Flags().String("principal", "", "Quota applies only to this principal within the tenant")
	putQuotaCmd.Flags().Int32("writeRequestsPerSecond", 0, "Maximum mutating unary gRPC requests allowed per second")
	putQuotaCmd.MarkFlagRequired("writeRequestsPerSecond")

	getCmd.AddCommand(getQuotaCmd)
	getQuotaCmd.Flags().String("principal", "", "Get only the Quota for this principal within the tenant")

	searchCmd.AddCommand(searchQuotaCmd)
	searchQuotaCmd.Flags().String("tenantId", "", "List Quotas associated with this Tenant ID")
	searchQuotaCmd.Flags().String("principal", "", "List only the Quota for this principal within the tenant")

	deleteCmd.AddCommand(deleteQuotaCmd)
	deleteQuotaCmd.Flags().String("principal", "", "Delete only the Quota for this principal within the tenant")
}
