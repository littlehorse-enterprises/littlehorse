package internal

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
)

var getInlineWfSpecCmd = &cobra.Command{
	Use:   "inlineWfSpec <wfRunId>",
	Short: "Get the inline workflow definition owned by a Workflow Run.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).GetInlineWfSpec(
			requestContext(cmd),
			littlehorse.StrToWfRunId(args[0]),
		))
	},
}

func init() {
	getCmd.AddCommand(getInlineWfSpecCmd)
}
