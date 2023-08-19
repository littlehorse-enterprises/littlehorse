/*
Copyright © 2023 NAME HERE <EMAIL ADDRESS>
*/
package cmd

import (
	"context"
	"log"
	"os"

	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common"
	"github.com/littlehorse-enterprises/littlehorse/sdk-go/common/model"
	"github.com/spf13/cobra"
)

// k8sHealthCmd represents the k8sHealth command
var k8sHealthCmd = &cobra.Command{
	Use:   "k8sHealth",
	Short: "Used by K8s to do healthprobe.",
	Long: `
Used by k8s to do a healthprobe on the pod. The default is to return
whether the pod is healthy and will later become ready to accept requests
(liveness); however, specifying the '--readiness' flag causes the command
to report whether the pod is currently ready to accept requests.`,

	Run: func(cmd *cobra.Command, args []string) {
		doReadiness, err := cmd.Flags().GetBool("readiness")
		if err != nil {
			log.Fatal(err)
		}

		resp, err := getGlobalClient(cmd).HealthCheck(
			context.Background(),
			&model.HealthCheckRequest{},
		)
		common.PrintResp(resp, err)

		if resp.CoreState == *model.LHHealthResult_LH_HEALTH_ERROR.Enum() {
			os.Exit(1)
		}
		if resp.TimerState == *model.LHHealthResult_LH_HEALTH_ERROR.Enum() {
			os.Exit(1)
		}

		if doReadiness {
			if resp.CoreState != *model.LHHealthResult_LH_HEALTH_RUNNING.Enum() {
				os.Exit(1)
			}
			if resp.TimerState != *model.LHHealthResult_LH_HEALTH_RUNNING.Enum() {
				os.Exit(1)
			}
		}
	},
}

func init() {
	k8sHealthCmd.Flags().Bool("readiness", false, "Get readiness rather than liveness")
	rootCmd.AddCommand(k8sHealthCmd)
}
