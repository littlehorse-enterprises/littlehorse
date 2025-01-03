package internal

import (
	"log"
	"strconv"

	"github.com/spf13/cobra"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
)

// getWorkflowEventCmd represents the workflowEvent command
var getWorkflowEventCmd = &cobra.Command{
	Use:   "workflowEvent <wfRunId> <workflowEventDefName> <number>",
	Short: "Get an WorkflowEvent by identifiers.",
	Long: `WorkflowEvent's are identified uniquely by the combination of the following:
	- Associated WfRun Id
	- WorkflowEventDef name
	- int32 number

	You may provide all three identifiers as three separate arguments or you may provide
	them delimited by the '/' character, as returned in all 'search' command queries.
	`,
	Args: cobra.ExactArgs(3),
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId := args[0]
		workflowEventDefName := args[1]
		number, err := strconv.Atoi(args[2])
		if err != nil {
			log.Fatalf("Error converting string '%s' to integer:", args[2])
			return
		}
		ctx := requestContext(cmd)

		littlehorse.PrintResp(getGlobalClient(cmd).GetWorkflowEvent(
			ctx,
			&lhproto.WorkflowEventId{
				WfRunId:            littlehorse.StrToWfRunId(wfRunId),
				WorkflowEventDefId: &lhproto.WorkflowEventDefId{Name: workflowEventDefName},
				Number:             int32(number),
			},
		))
	},
}

var searchWorkflowEventCmd = &cobra.Command{
	Use:   "workflowEvent <workflowEventDefName>",
	Short: "Search for WorkflowEvent's by WorkflowEventDef Name",
	Long: `
Search for WorkflowEvent's by their WorkflowEventDef Name.

Returns a list of ObjectId's that can be passed into 'lhctl get workflowEvent'.

* Note: You may optionally use the earliesMinutesAgo and latestMinutesAgo
	options with this group to put a time bound on WorkflowEvents which are
	returned. The time bound applies to the time that the WorkflowEvents
	were created.
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {

		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		workflowEventDefName := args[0]

		earliest, latest := loadEarliestAndLatestStart(cmd)

		search := &lhproto.SearchWorkflowEventRequest{
			Bookmark:      bookmark,
			Limit:         &limit,
			EarliestStart: earliest,
			LatestStart:   latest,
			WorkflowEventDefId: &lhproto.WorkflowEventDefId{
				Name: workflowEventDefName,
			},
		}

		littlehorse.PrintResp(getGlobalClient(cmd).SearchWorkflowEvent(requestContext(cmd), search))
	},
}

var listWorkflowEventCmd = &cobra.Command{
	Use:   "workflowEvent <wfRunId>",
	Short: "List all WorkflowEvent's for a given WfRun Id.",
	Long: `
Lists all WorkflowEvent's for a given WfRun Id.
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfRunId := args[0]

		req := &lhproto.ListWorkflowEventsRequest{
			WfRunId: littlehorse.StrToWfRunId(wfRunId),
		}

		littlehorse.PrintResp(getGlobalClient(cmd).ListWorkflowEvents(
			requestContext(cmd),
			req,
		))
	},
}

func init() {
	getCmd.AddCommand(getWorkflowEventCmd)

	listCmd.AddCommand(listWorkflowEventCmd)

	searchCmd.AddCommand(searchWorkflowEventCmd)
	searchWorkflowEventCmd.Flags().Int("earliestMinutesAgo", -1, "Search only for Principals that were created no more than this number of minutes ago")
	searchWorkflowEventCmd.Flags().Int("latestMinutesAgo", -1, "Search only for Principals that were created at least this number of minutes ago")
}
