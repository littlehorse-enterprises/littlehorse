package cmd

import (
	"github.com/spf13/cobra"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	emptypb "github.com/golang/protobuf/ptypes/empty"
)

// executeCmd represents the run command
var whoamiCmd = &cobra.Command{
	Use:   "whoami",
	Short: "Prints the current logged principal",
	Run: func(cmd *cobra.Command, args []string) {
		common.PrintResp(getGlobalClient(cmd).Whoami(requestContext(), &emptypb.Empty{}))
	},
}

func init() {
	rootCmd.AddCommand(whoamiCmd)
}
