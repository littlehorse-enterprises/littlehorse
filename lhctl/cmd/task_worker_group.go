package cmd

import (
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
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

		common.PrintResp(getGlobalClient(cmd).GetTaskWorkerGroup(
			requestContext(cmd),
			&model.TaskDefId{
				Name: args[0],
			},
		))
	},
}

func init() {
	getCmd.AddCommand(getTaskWorkerGroup)
}
