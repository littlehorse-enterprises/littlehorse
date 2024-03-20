package cmd

import (
	emptypb "github.com/golang/protobuf/ptypes/empty"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/spf13/cobra"
)

// executeCmd represents the run command
var whoamiCmd = &cobra.Command{
	Use:   "whoami",
	Short: "Prints the current logged principal",
	Run: func(cmd *cobra.Command, args []string) {
		common.PrintResp(getGlobalClient(cmd).Whoami(requestContext(cmd), &emptypb.Empty{}))
	},
}

func init() {
	rootCmd.AddCommand(whoamiCmd)
}
