package cmd

import (
	"context"
	"log"
	"github.com/spf13/cobra"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
)

var deployTenant = &cobra.Command{
	Use:   "tenant",
	Short: "Deploy a wfSpec from a JSON or Protobuf file.",
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("You must provide one argument")
		}
		common.PrintResp(getGlobalClient(cmd).PutTenant(
			context.Background(),
			&model.PutTenantRequest{
				Id: args[0],
			},
		))
	},
}

func init() {
	createCmd.AddCommand(deployTenant)
}
