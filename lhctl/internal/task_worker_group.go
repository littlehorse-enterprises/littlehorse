package internal

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"log"
)

var getTaskWorkerGroup = &cobra.Command{
	Use:   "taskWorkerGroup <taskDefName>",
	Short: "",
	Long:  `Gets the registered task worker group associated with a specific TaskDef`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) != 1 {
			log.Fatal("Must provide 1 arguments. See 'lhctl get taskWorkerGroup -h'")
		}

		littlehorse.PrintResp(getGlobalClient(cmd).GetTaskWorkerGroup(
			requestContext(cmd),
			&lhproto.TaskDefId{
				Name: args[0],
			},
		))
	},
}

func init() {
	getCmd.AddCommand(getTaskWorkerGroup)
}
