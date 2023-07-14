package cmd

import (
	"context"
	"log"
	"strings"

	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common"
	"bitbucket.org/littlehorse-core/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

// getNodeRunCmd represents the nodeRun command
var getTaskRunCmd = &cobra.Command{
	Use:   "taskRun <wfRunId> <taskRunGuid>",
	Short: "Get a TaskRun by WfRunId and Guid",
	Long: `TaskRun's are identified uniquely by the combination of the following:
	- Associated WfRun Id
	- A unique guid

	You may provide both identifiers as two separate arguments or you may provide
	them delimited by the '/' character.`,
	Run: func(cmd *cobra.Command, args []string) {
		needsHelp := false
		if len(args) == 1 {
			args = strings.Split(args[0], "/")
		}

		if len(args) != 2 {
			needsHelp = true
		}

		if needsHelp {
			log.Fatal("Must provide 1 or 2 arguments. See 'lhctl get taskRun -h'")
		}

		common.PrintResp(getGlobalClient(cmd).GetTaskRun(
			context.Background(),
			&model.TaskRunIdPb{
				PartitionKey: args[0],
				TaskGuid:     args[1],
			},
		))
	},
}

func init() {
	getCmd.AddCommand(getTaskRunCmd)
}
