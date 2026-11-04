package internal

import (
	"fmt"
	"log"
	"time"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
	"github.com/spf13/cobra"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var getBulkJobCmd = &cobra.Command{
	Use:   "bulkJob <id>",
	Short: "Get the status of a BulkJob.",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).GetBulkJob(
			requestContext(cmd),
			&lhproto.GetBulkJobRequest{
				Id: &lhproto.BulkJobId{
					Id: args[0],
				},
			},
		))
	},
}

var searchBulkJobCmd = &cobra.Command{
	Use:   "bulkJob",
	Short: "Search for BulkJob's, optionally filtering by status.",
	Long: `Search for BulkJob's. Returns a list of BulkJobId's.

Optional flags:
  --status  Only return BulkJob's with this status
            (BULK_JOB_RUNNING, BULK_JOB_COMPLETED, BULK_JOB_FAILED)
`,
	Args: cobra.ExactArgs(0),
	Run: func(cmd *cobra.Command, args []string) {
		bookmark, _ := cmd.Flags().GetBytesBase64("bookmark")
		limit, _ := cmd.Flags().GetInt32("limit")
		statusStr, _ := cmd.Flags().GetString("status")

		search := &lhproto.SearchBulkJobRequest{
			Bookmark: bookmark,
			Limit:    &limit,
		}

		if statusStr != "" {
			statusVal, ok := lhproto.BulkJobStatus_value[statusStr]
			if !ok {
				log.Fatalf("Invalid status: %s. Valid values: BULK_JOB_RUNNING, BULK_JOB_COMPLETED, BULK_JOB_FAILED", statusStr)
			}
			status := lhproto.BulkJobStatus(statusVal)
			search.Status = &status
		}

		littlehorse.PrintResp(getGlobalClient(cmd).SearchBulkJob(requestContext(cmd), search))
	},
}

var deleteBulkJobCmd = &cobra.Command{
	Use:   "bulkJob <id>",
	Short: "Delete a BulkJob that has finished (COMPLETED or FAILED).",
	Args:  cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		littlehorse.PrintResp(getGlobalClient(cmd).DeleteBulkJob(
			requestContext(cmd),
			&lhproto.DeleteBulkJobRequest{
				Id: &lhproto.BulkJobId{
					Id: args[0],
				},
			},
		))
	},
}

var bulkDeleteWfRunCmd = &cobra.Command{
	Use:   "wfRunBulk <wfSpecName>",
	Short: "Bulk delete WfRun's matching criteria (creates a BulkJob).",
	Long: `Bulk delete WfRun's matching criteria. Creates a BulkJob that deletes
WfRun's in the background.

Required flags:
  --from    Start of time range (inclusive), ISO 8601 format
  --to      End of time range (inclusive), ISO 8601 format

Optional flags:
  --status  Only delete WfRun's with this status (e.g., COMPLETED, ERROR)
  --id      Client-provided ID for idempotency
`,
	Args: cobra.ExactArgs(1),
	Run: func(cmd *cobra.Command, args []string) {
		wfSpecName := args[0]

		fromStr, _ := cmd.Flags().GetString("from")
		toStr, _ := cmd.Flags().GetString("to")
		statusStr, _ := cmd.Flags().GetString("status")
		idStr, _ := cmd.Flags().GetString("id")

		if fromStr == "" || toStr == "" {
			log.Fatal("Both --from and --to flags are required")
		}

		fromTime, err := time.Parse(time.RFC3339, fromStr)
		if err != nil {
			log.Fatalf("Invalid --from time format (use ISO 8601, e.g. 2025-01-01T00:00:00Z): %v", err)
		}

		toTime, err := time.Parse(time.RFC3339, toStr)
		if err != nil {
			log.Fatalf("Invalid --to time format (use ISO 8601, e.g. 2025-06-01T00:00:00Z): %v", err)
		}

		bulkDelete := &lhproto.BulkDeleteWfRun{
			WfSpecName:    wfSpecName,
			EarliestStart: timestamppb.New(fromTime),
			LatestStart:   timestamppb.New(toTime),
		}

		if statusStr != "" {
			statusVal, ok := lhproto.LHStatus_value[statusStr]
			if !ok {
				log.Fatalf("Invalid status: %s. Valid values: STARTING, RUNNING, COMPLETED, HALTING, HALTED, ERROR, EXCEPTION", statusStr)
			}
			status := lhproto.LHStatus(statusVal)
			bulkDelete.WfRunStatus = &status
		}

		req := &lhproto.CreateBulkJobRequest{
			Operation: &lhproto.CreateBulkJobRequest_BulkDeleteWfRun{
				BulkDeleteWfRun: bulkDelete,
			},
		}

		if idStr != "" {
			req.Id = &idStr
		}

		resp, err := getGlobalClient(cmd).CreateBulkJob(requestContext(cmd), req)
		if err != nil {
			log.Fatalf("Failed to create BulkJob: %v", err)
		}

		littlehorse.PrintResp(resp, nil)
		fmt.Printf("\nBulkJob created. Check progress with: lhctl get bulkJob %s\n", resp.Id.Id)
	},
}

func init() {
	getCmd.AddCommand(getBulkJobCmd)
	deleteCmd.AddCommand(bulkDeleteWfRunCmd)
	deleteCmd.AddCommand(deleteBulkJobCmd)
	searchCmd.AddCommand(searchBulkJobCmd)

	searchBulkJobCmd.Flags().String("status", "", "Only return BulkJob's with this status (BULK_JOB_RUNNING, BULK_JOB_COMPLETED, BULK_JOB_FAILED)")

	bulkDeleteWfRunCmd.Flags().String("from", "", "Start of time range (inclusive), ISO 8601 format (required)")
	bulkDeleteWfRunCmd.Flags().String("to", "", "End of time range (inclusive), ISO 8601 format (required)")
	bulkDeleteWfRunCmd.Flags().String("status", "", "Only delete WfRun's with this status (e.g., COMPLETED, ERROR)")
	bulkDeleteWfRunCmd.Flags().String("id", "", "Client-provided ID for idempotency")
}
