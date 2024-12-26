package internal

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

	"github.com/spf13/cobra"
)

var putTenantCmd = &cobra.Command{
	Use:   "tenant <id>",
	Short: "Create a Tenant. Currently, updating Tenants is not supported.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).PutTenant(
			requestContext(cmd),
			&lhproto.PutTenantRequest{
				Id: args[0],
			},
		))
	},
}

var searchTenantCmd = &cobra.Command{
	Use:   "tenant",
	Short: "Search for all available TenantIds for current Principal",
	Run: func(cmd *cobra.Command, args []string) {
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		littlehorse.PrintResp(getGlobalClient(cmd).SearchTenant(
			requestContext(cmd),
			&lhproto.SearchTenantRequest{
				Bookmark: bookmark,
				Limit:    &limit,
			},
		))
	},
}

var getTenantCmd = &cobra.Command{
	Use:   "tenant <id>",
	Short: "Get a Tenant",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).GetTenant(
			requestContext(cmd),
			&lhproto.TenantId{
				Id: args[0],
			},
		))
	},
}

func init() {
	putCmd.AddCommand(putTenantCmd)
	searchCmd.AddCommand(searchTenantCmd)
	getCmd.AddCommand(getTenantCmd)
}
