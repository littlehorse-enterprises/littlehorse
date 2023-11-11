package cmd

import (
	"log"
	"github.com/spf13/cobra"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

var putTenantCmd = &cobra.Command{
	Use:   "tenant",
	Short: "Create a Tenant. Currently, updating Tenants is not supported.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument")
		}
		common.PrintResp(getGlobalClient(cmd).PutTenant(
			requestContext(),
			&model.PutTenantRequest{
				Id: args[0],
			},
		))
	},
}
func init() {
	putCmd.AddCommand(putTenantCmd)
}
