package internal

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/emptypb"
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
