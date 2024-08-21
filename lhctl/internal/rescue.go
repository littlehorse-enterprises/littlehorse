package internal

import (
	"log"
	"strconv"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

var rescueCmd = &cobra.Command{
	Use:   "rescue <wfRunId> <threadRunNumber>",
	Short: "Rescues a ThreadRun in the ERROR state by manually restarting the execution.",
	Long: `
Rescues a ThreadRun in the ERROR state by manually restarting the execution. You can
specify whether to re-attempt the failed Node or to move on to the next Node in the WfRun.

The specified ThreadRun must be in the ERROR state, and if it has any parent ThreadRuns,
the failure (or propagated failure) must not have been handled by a Failure Handlder.
	`,
	Run: func(cmd *cobra.Command, args []string) {
		if len(args) < 1 {
			log.Fatal("You must provide at least 2 args: WfRunId and threadRunNumber.")
		}

		wfRunId := common.StrToWfRunId(args[0])
		trn, err := strconv.Atoi(args[1])
		if err != nil {
			log.Fatal("Couldn't convert threadRunNumber to int.")
		}

		skipCurrentNode, _ := cmd.Flags().GetBool("skipCurrentNode")
		rescueRequest := &model.RescueThreadRunRequest{
			WfRunId:         wfRunId,
			ThreadRunNumber: int32(trn),
			SkipCurrentNode: skipCurrentNode,
		}

		common.PrintResp(getGlobalClient(cmd).RescueThreadRun(requestContext(cmd), rescueRequest))
	},
}

func init() {
	rootCmd.AddCommand(rescueCmd)
	rescueCmd.Flags().Bool("skipCurrentNode", false, "Whether to skip the current node (true) or retry it (false)")
}
