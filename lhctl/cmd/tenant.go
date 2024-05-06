package cmd

import (
	"log"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

var putTenantCmd = &cobra.Command{
	Use:   "tenant",
	Short: "Create a Tenant. Currently, updating Tenants is not supported.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument")
		}
		common.PrintResp(getGlobalClient(cmd).PutTenant(
			requestContext(cmd),
			&model.PutTenantRequest{
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
		common.PrintResp(getGlobalClient(cmd).SearchTenant(
			requestContext(cmd),
			&model.SearchTenantRequest{
				Bookmark: bookmark,
				Limit:    &limit,
			},
		))
	},
}

func init() {
	putCmd.AddCommand(putTenantCmd)
	searchCmd.AddCommand(searchTenantCmd)
}
