package internal

import (
    "github.com/littlehorse-enterprises/littlehorse/sdk-go/lhproto"
    "github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"
    "github.com/spf13/cobra"
)

var getWfMetricCmd = &cobra.Command{
    Use:   "wfMetric <name>",
    Short: "Get latest workflow metric window for a WfSpec (by name, optional majorVersion and revision)",
    Args:  cobra.MinimumNArgs(1),
    Run: func(cmd *cobra.Command, args []string) {
        name := args[0]

        var majorVersion *int32
        if raw, _ := cmd.Flags().GetInt32("majorVersion"); raw == -1 {
            majorVersion = nil
        } else {
            majorVersion = &raw
        }

        var revision *int32
        if raw, _ := cmd.Flags().GetInt32("revision"); raw == -1 {
            revision = nil
        } else {
            revision = &raw
        }

        req := &lhproto.GetLatestWfMetricWindowRequest{
            WfSpecName:  name,
            MajorVersion: majorVersion,
            Revision:     revision,
        }

        littlehorse.PrintResp(
            getGlobalClient(cmd).GetLatestWfMetricWindow(requestContext(cmd), req),
        )
    },
}

func init() {
    getWfMetricCmd.Flags().Int32("majorVersion", -1, "Set specific WfSpec Major Version to get metrics for")
    getWfMetricCmd.Flags().Int32("revision", -1, "Set specific WfSpec Revision to get metrics for")
    getCmd.AddCommand(getWfMetricCmd)
}
